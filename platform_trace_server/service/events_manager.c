/* Platform Trace Server - events manager source file
 **
 ** Copyright (C) Intel 2012
 **
 ** Licensed under the Apache License, Version 2.0 (the "License");
 ** you may not use this file except in compliance with the License.
 ** You may obtain a copy of the License at
 **
 **     http://www.apache.org/licenses/LICENSE-2.0
 **
 ** Unless required by applicable law or agreed to in writing, software
 ** distributed under the License is distributed on an "AS IS" BASIS,
 ** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 ** See the License for the specific language governing permissions and
 ** limitations under the License.
 **
 */

#include <stdio.h>
#include <unistd.h>
#include <errno.h>
#include <dlfcn.h>
#include <fcntl.h>
#include <stdbool.h>
#include <sys/types.h>
#include <sys/epoll.h>

#include "errors.h"
#include "logs.h"
#include "pts_def.h"
#include "client.h"
#include "client_cnx.h"
#include "client_events.h"
#include "client_trace.h"
#include "events_manager.h"


#define FIRST_EVENT -1

const char *g_pts_st[] = {
#undef X
#define X(a) #a
        PTS_STATE
};

inline void set_pts_state(pts_data_t *pts, e_pts_state_t state)
{
    pts->state = state;
    LOG_VERBOSE("new STATE: %s", g_pts_st[pts->state]);
}

/**
 * close all connections
 *
 * @param [in,out] pts pts context
 *
 * @return E_PTS_ERR_BAD_PARAMETER pts is NULL
 * @return E_PTS_ERR_SUCCESS
 */
e_pts_errors_t events_cleanup(pts_data_t *pts)
{
    e_pts_errors_t ret = E_PTS_ERR_SUCCESS;

    CHECK_PARAM(pts, ret, out);

    set_pts_state(pts, E_PTS_STATE_OFF);
    free(pts->events.ev);
    /* Close all client connections */
    close_all_clients(&pts->clients);
    /* Close server connection */
    if (pts->fd_cnx != CLOSED_FD)
        close_cnx(&pts->fd_cnx);
    /* Close epoll file descriptor */
    if (pts->epollfd != CLOSED_FD)
        close(pts->epollfd);
    /* Desallocate memory for clients list */
    if (pts->clients.list != NULL) {
        free(pts->clients.list);
        pts->clients.list = NULL;
    }

    out:
    return ret;
}

/**
 * initialize PTS structure
 *
 * @param [in,out] pts PTS context
 *
 * @return E_PTS_ERR_BAD_PARAMETER pts is NULL
 * @return E_PTS_ERR_FAILED if failed
 * @return E_PTS_ERR_SUCCESS if successful
 */
e_pts_errors_t events_init(pts_data_t *pts)
{
    e_pts_errors_t ret = E_PTS_ERR_SUCCESS;

    CHECK_PARAM(pts, ret, out);

    pts->fd_cnx = CLOSED_FD;

    /* Begin PTS initialization, state mode is set to E_PTS_MODE_OFF */
    set_pts_state(pts, E_PTS_STATE_OFF);

    pts->events.nfds = 0;
    pts->events.ev = malloc(sizeof(struct epoll_event) * (PTS_MAX_CLIENT + 1));
    pts->events.cur_ev = FIRST_EVENT;

    if (pts->events.ev == NULL) {
        LOG_ERROR("Unable to initialize event structure");
        ret = E_PTS_ERR_BAD_PARAMETER;
        goto out;
    }

    /* Initialize list of PTS clients */
    if ((ret = initialize_list(&pts->clients, PTS_MAX_CLIENT)) != E_PTS_ERR_SUCCESS) {
        LOG_ERROR("Client list initialisation failed");
        goto out;
    }

    /* Open UNIX socket connection */
    ret = open_cnx(&pts->fd_cnx);
    if (ret != E_PTS_ERR_SUCCESS) {
        goto out;
    }

    /* Configure events handlers */
    pts->hdler_events[E_EVENT_NEW_CLIENT] = new_client;
    pts->hdler_events[E_EVENT_CLIENT] = known_client;

    /* Initialize client events management */
    if ((ret = client_events_init(pts)) != E_PTS_ERR_SUCCESS) {
        LOG_ERROR("unable to configure client events handlers");
        goto out;
    }

    out:
    return ret;
}

/**
 * this function is an event dispatcher. it waits for a new event if event list
 * is empty. otherwise, it sets the current event type
 *
 * @param [in,out] pts pts context
 *
 * @return E_PTS_ERR_BAD_PARAMETER pts is NULL
 * @return E_PTS_ERR_FAILED if epoll_wait fails
 * E_PTS_ERR_SUCCESS: if successful
 */
static e_pts_errors_t wait_for_event(pts_data_t *pts)
{
    e_pts_errors_t ret = E_PTS_ERR_SUCCESS;
    int fd;

    CHECK_PARAM(pts, ret, out);

    if (pts->events.cur_ev + 1 >= pts->events.nfds) {
        do {
            pts->events.cur_ev = FIRST_EVENT;
            LOG_INFO("%s STATE: waiting for a new event", MODULE_NAME);
            pts->events.nfds = epoll_wait(pts->epollfd, pts->events.ev,
                    PTS_MAX_CLIENT + 1,
                    -1);
            if (pts->events.nfds == -1) {
                if ((errno == EBADF) || (errno == EINVAL)) {
                    LOG_ERROR("Bad configuration");
                    ret = E_PTS_ERR_FAILED;
                    goto out;
                }
            }
        } while (pts->events.nfds == -1);
    }

    pts->events.cur_ev++;
    if (pts->events.nfds == 0) {
        pts->events.state = E_EVENT_TIMEOUT;
    } else {
        fd = pts->events.ev[pts->events.cur_ev].data.fd;
        if (fd == pts->fd_cnx) {
            /* fd_cnx is polled while a new client establishes a first connection with PTS server.
             * Typically when a client sends E_PTS_SET_NAME request (first request in connection sequence).
             */
            pts->events.state = E_EVENT_NEW_CLIENT;
        } else {
            /* Otherwise, the specific fd which has been accepted in new_client service is polled.
             * This case occurs whenever a known client sends a request.
             */
            pts->events.state = E_EVENT_CLIENT;
        }
    }

    out:
    return ret;
}

/**
 * events manager: manage cnx events
 * Instead of a state machine, an event dispatcher is used here.
 * A state machine is not usefull here as the protocol
 * is stateless.
 *
 * @param [in,out] pts pts context
 *
 * @return E_PTS_ERR_BAD_PARAMETER pts is NULL
 * @return E_PTS_ERR_SUCCESS
 */
e_pts_errors_t events_manager(pts_data_t *pts)
{
    e_pts_errors_t ret = E_PTS_ERR_FAILED;

    char *events_str[] = {
#undef X
#define X(a) #a
            EVENTS
    };

    CHECK_PARAM(pts, ret, out);

    /* Begin PTS treatment loop, state mode is set to E_PTS_STATE_UP */
    set_pts_state(pts, E_PTS_STATE_UP);
    inform_all_clients(&pts->clients, E_PTS_EVENT_PTS_UP, NULL);

    for (;;) {
        if ((ret = wait_for_event(pts)) != E_PTS_ERR_SUCCESS)
            goto out;

        LOG_DEBUG("event type: %s", events_str[pts->events.state]);
        if (pts->hdler_events[pts->events.state] != NULL) {
            if ((ret = pts->hdler_events[pts->events.state](pts)) == E_PTS_ERR_BAD_PARAMETER)
                goto out;
        }

        if ((ret = trace_manager(pts)) != E_PTS_ERR_SUCCESS)
            goto out;
    }

    out:
    return ret;
}
