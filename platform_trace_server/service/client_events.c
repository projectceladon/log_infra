/* Platform Trace Server - client events header file
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

#include <arpa/inet.h>
#include <errno.h>
#include <sys/epoll.h>

#include "errors.h"
#include "logs.h"
#include "pts_def.h"
#include "client.h"
#include "client_cnx.h"
#include "msg_to_data.h"
#include "client_events.h"


const char *g_pts_requests[] = {
#undef X
#define X(a) #a
        PTS_REQUESTS
};

/**
 * handle E_PTS_SET_NAME request
 *
 * @private
 *
 * @param [in,out] pts pts context
 *
 * @return E_PTS_ERR_BAD_PARAMETER if pts is NULL
 * @return E_PTS_ERR_SUCCESS if successful
 */
static e_pts_errors_t request_set_name(pts_data_t *pts)
{
    e_pts_errors_t ret;

    CHECK_PARAM(pts, ret, out);

    /* Set client name */
    ret = set_client_name(pts->request.client,
            pts->request.msg.data,
            pts->request.msg.hdr.len);

    if (ret != E_PTS_ERR_SUCCESS) {
        ret = E_PTS_ERR_DISCONNECTED;
        inform_client(pts->request.client, E_PTS_NACK, NULL);
    } else {
        inform_client(pts->request.client, E_PTS_ACK, NULL);
    }

    out:
    return ret;
}

/**
 * handle E_PTS_SET_INPUT_PATH_TRACE_CFG request
 *
 * @private
 *
 * @param [in,out] pts pts context
 *
 * @return E_PTS_ERR_BAD_PARAMETER if pts is NULL
 * @return E_PTS_ERR_SUCCESS if successful
 */
static e_pts_errors_t request_set_input_path(pts_data_t *pts)
{
    e_pts_errors_t ret;

    CHECK_PARAM(pts, ret, out);

    /* Set client input path */
    ret = set_client_input_path(pts->request.client,
            pts->request.msg.data,
            pts->request.msg.hdr.len);

    if (ret != E_PTS_ERR_SUCCESS) {
        ret = E_PTS_ERR_DISCONNECTED;
        inform_client(pts->request.client, E_PTS_NACK, NULL);
    } else {
        inform_client(pts->request.client, E_PTS_ACK, NULL);
    }

    out:
    return ret;
}

/**
 * handle E_PTS_SET_OUTPUT_PATH_TRACE_CFG request
 *
 * @private
 *
 * @param [in,out] pts pts context
 *
 * @return E_PTS_ERR_BAD_PARAMETER if pts is NULL
 * @return E_PTS_ERR_SUCCESS if successful
 */
static e_pts_errors_t request_set_output_path(pts_data_t *pts)
{
    e_pts_errors_t ret;

    CHECK_PARAM(pts, ret, out);

    /* Set client output path */
    ret = set_client_output_path(pts->request.client,
            pts->request.msg.data,
            pts->request.msg.hdr.len);

    if (ret != E_PTS_ERR_SUCCESS) {
        ret = E_PTS_ERR_DISCONNECTED;
        inform_client(pts->request.client, E_PTS_NACK, NULL);
    } else {
        inform_client(pts->request.client, E_PTS_ACK, NULL);
    }

    out:
    return ret;
}

/**
 * handle E_PTS_SET_ROTATE_SIZE_TRACE_CFG request
 *
 * @private
 *
 * @param [in,out] pts pts context
 *
 * @return E_PTS_ERR_BAD_PARAMETER if pts is NULL
 * @return E_PTS_ERR_SUCCESS if successful
 */
static e_pts_errors_t request_set_rotate_size(pts_data_t *pts)
{
    e_pts_errors_t ret = E_PTS_ERR_FAILED;
    uint32_t rotate_size;

    CHECK_PARAM(pts, ret, out);

    if (pts->request.msg.hdr.len == sizeof(uint32_t)) {
        memcpy(&rotate_size, pts->request.msg.data, sizeof(uint32_t));
        rotate_size = ntohl(rotate_size);
        /* Set client rotate size */
        ret = set_client_rotate_size(pts->request.client, rotate_size);

        if (ret != E_PTS_ERR_SUCCESS) {
            ret = E_PTS_ERR_DISCONNECTED;
            inform_client(pts->request.client, E_PTS_NACK, NULL);
        } else {
            inform_client(pts->request.client, E_PTS_ACK, NULL);
        }

    } else {
        LOG_ERROR("bad size of rotate size parameter");
        inform_client(pts->request.client, E_PTS_NACK, NULL);
    }

    out:
    return ret;
}

/**
 * handle E_PTS_SET_ROTATE_NUM_TRACE_CFG request
 *
 * @private
 *
 * @param [in,out] pts pts context
 *
 * @return E_PTS_ERR_BAD_PARAMETER if pts is NULL
 * @return E_PTS_ERR_SUCCESS if successful
 */
static e_pts_errors_t request_set_rotate_num(pts_data_t *pts)
{
    e_pts_errors_t ret = E_PTS_ERR_FAILED;
    uint32_t rotate_num;

    CHECK_PARAM(pts, ret, out);

    if (pts->request.msg.hdr.len == sizeof(uint32_t)) {
        memcpy(&rotate_num, pts->request.msg.data, sizeof(uint32_t));
        rotate_num = ntohl(rotate_num);
        /* Set client rotate number */
        ret = set_client_rotate_num(pts->request.client, rotate_num);

        if (ret != E_PTS_ERR_SUCCESS) {
            ret = E_PTS_ERR_DISCONNECTED;
            inform_client(pts->request.client, E_PTS_NACK, NULL);
        } else {
            inform_client(pts->request.client, E_PTS_ACK, NULL);
        }

    } else {
        LOG_ERROR("bad size of rotate num parameter");
        inform_client(pts->request.client, E_PTS_NACK, NULL);
    }

    out:
    return ret;
}

/**
 * handle E_PTS_SET_TRACE_MODE request
 *
 * @private
 *
 * @param [in,out] pts pts context
 *
 * @return E_PTS_ERR_BAD_PARAMETER if pts is NULL
 * @return E_PTS_ERR_SUCCESS if successful
 */
static e_pts_errors_t request_set_trace_mode(pts_data_t *pts)
{
    e_pts_errors_t ret = E_PTS_ERR_FAILED;
    e_trace_mode_t trace_mode;
    e_pts_events_t notification = E_PTS_NUM_EVENTS;

    CHECK_PARAM(pts, ret, out);

    if (pts->request.msg.hdr.len == sizeof(e_trace_mode_t)) {
        memcpy(&trace_mode, pts->request.msg.data, sizeof(e_trace_mode_t));
        trace_mode = (e_trace_mode_t)ntohl((uint32_t)trace_mode);
        /* Set client trace mode */
        ret = set_client_trace_mode(pts->request.client, trace_mode);
        if (ret != E_PTS_ERR_SUCCESS) {
            ret = E_PTS_ERR_DISCONNECTED;
            inform_client(pts->request.client, E_PTS_NACK, NULL);
        } else {
            inform_client(pts->request.client, E_PTS_ACK, NULL);

            /* Client is registered and accepted. So, PTS should provide
             * its current status if client has subscribed to it */
            switch (pts->state) {
            case E_PTS_STATE_RESET:
            case E_PTS_STATE_OFF:
                notification = E_PTS_EVENT_PTS_DOWN;
                break;
            case E_PTS_STATE_UP:
                notification = E_PTS_EVENT_PTS_UP;
                break;
            default:
                break;
            }
            inform_client(pts->request.client, notification, NULL);
        }

    } else {
        LOG_ERROR("bad size of trace mode parameter");
        inform_client(pts->request.client, E_PTS_NACK, NULL);
    }

    out:
    return ret;
}

static e_pts_errors_t request_trace_start(pts_data_t *pts)
{
    e_pts_errors_t ret = E_PTS_ERR_SUCCESS;

    CHECK_PARAM(pts, ret, out);

    ret = set_client_signal_trace_start(pts->request.client);
    if (ret != E_PTS_ERR_SUCCESS) {
        ret = E_PTS_ERR_DISCONNECTED;
        inform_client(pts->request.client, E_PTS_NACK, NULL);
    } else {
        inform_client(pts->request.client, E_PTS_ACK, NULL);
    }

    out:
    return ret;
}

static e_pts_errors_t request_trace_stop(pts_data_t *pts)
{
    e_pts_errors_t ret = E_PTS_ERR_SUCCESS;

    CHECK_PARAM(pts, ret, out);

    ret = set_client_signal_trace_stop(pts->request.client);
    if (ret != E_PTS_ERR_SUCCESS) {
        ret = E_PTS_ERR_DISCONNECTED;
        inform_client(pts->request.client, E_PTS_NACK, NULL);
    } else {
        inform_client(pts->request.client, E_PTS_ACK, NULL);
    }

    out:
    return ret;
}

/**
 * handle E_PTS_SET_EVENTS request
 *
 * @private
 *
 * @param [in,out] pts pts context
 *
 * @return E_PTS_ERR_BAD_PARAMETER if pts is NULL
 * @return E_PTS_ERR_SUCCESS if successful
 */
static e_pts_errors_t request_set_events(pts_data_t *pts)
{
    e_pts_errors_t ret = E_PTS_ERR_SUCCESS;
    uint32_t filter;

    CHECK_PARAM(pts, ret, out);

    if (pts->request.msg.hdr.len == sizeof(uint32_t)) {
        memcpy(&filter, pts->request.msg.data, sizeof(uint32_t));
        filter = ntohl(filter);
        ret = set_client_filter(pts->request.client, filter);

        if (ret != E_PTS_ERR_SUCCESS) {
            ret = E_PTS_ERR_DISCONNECTED;
            inform_client(pts->request.client, E_PTS_NACK, NULL);
        } else {
            inform_client(pts->request.client, E_PTS_ACK, NULL);
        }

    } else {
        LOG_ERROR("bad filter size");
        inform_client(pts->request.client, E_PTS_NACK, NULL);
    }

    out:
    return ret;
}

/**
 * handle client request
 *
 * @private
 *
 * @param [in,out] pts pts context
 *
 * @return E_PTS_ERR_BAD_PARAMETER if pts is NULL
 * @return E_PTS_ERR_FAILED if an error occurs
 * @return E_PTS_ERR_SUCCESS if successful
 */
static e_pts_errors_t client_request(pts_data_t *pts)
{
    e_pts_errors_t ret = E_PTS_ERR_FAILED;
    size_t size;
    bool registered = false;

    CHECK_PARAM(pts, ret, out);

    pts->request.msg.data = NULL;
    size = pts->request.msg.hdr.len;

    /* Request is associated with data */
    if (size > 0) {
        pts->request.msg.data = calloc(size, sizeof(char));
        if (pts->request.msg.data == NULL) {
            /* Allocation error */
            goto out;
        }

        /* Read data message from UNIX socket */
        ret = read_cnx(pts->request.client->fd, pts->request.msg.data, &size);
        if ((ret != E_PTS_ERR_SUCCESS) || (size != pts->request.msg.hdr.len)) {
            /* Read error (or read size different from expected size message) */
            LOG_ERROR("Client (fd=%d name=%s) Failed to read data",
                    pts->request.client->fd,
                    pts->request.client->name);
            goto out_free;
        }
    }

    if (pts->request.msg.hdr.id < E_PTS_NUM_REQUESTS) {
        LOG_INFO("Request (%s) received from client (fd=%d name=%s)",
                g_pts_requests[pts->request.msg.hdr.id],
                pts->request.client->fd,
                pts->request.client->name);

        /* Check if client is in state registered */
        is_registered(pts->request.client, &registered);

        if (!registered &&
                (pts->request.msg.hdr.id != E_PTS_SET_NAME) &&
                (pts->request.msg.hdr.id != E_PTS_SET_EVENTS) &&
                (pts->request.msg.hdr.id != E_PTS_SET_INPUT_PATH_TRACE_CFG) &&
                (pts->request.msg.hdr.id != E_PTS_SET_OUTPUT_PATH_TRACE_CFG) &&
                (pts->request.msg.hdr.id != E_PTS_SET_ROTATE_SIZE_TRACE_CFG) &&
                (pts->request.msg.hdr.id != E_PTS_SET_ROTATE_NUM_TRACE_CFG) &&
                (pts->request.msg.hdr.id != E_PTS_SET_TRACE_MODE)) {
            /* Client is not yet registered and received request is different
             * from a connection request. Request shall be rejected
             */

            LOG_DEBUG("client not fully registered. Request rejected");
            inform_client(pts->request.client, E_PTS_NACK, NULL);
        } else {
            /* Call specific callback request */
            if (pts->hdler_client[pts->state][pts->request.msg.hdr.id] != NULL)
                ret = pts->hdler_client[pts->state][pts->request.msg.hdr.id](pts);
        }
    }

    out_free:
    if (pts->request.msg.data != NULL)
        free(pts->request.msg.data);

    out:
    return ret;
}

/**
 * add fd to epoll
 *
 * @private
 *
 * @param [out] epollfd epoll fd
 * @param [in] fd file descriptor
 * @param [in] events events to catch
 *
 * @return E_PTS_ERR_BAD_PARAMETER clients is NULL
 * @return E_PTS_ERR_FAILED initialization fails
 * @return E_PTS_ERR_SUCCESS if successful
 */
static e_pts_errors_t add_fd_ev(int epollfd, int fd, int events)
{
    e_pts_errors_t ret = E_PTS_ERR_SUCCESS;
    struct epoll_event ev;

    ev.events = events;
    ev.data.fd = fd;
    if (epoll_ctl(epollfd, EPOLL_CTL_ADD, fd, &ev) == -1) {
        LOG_ERROR("Failed to add fd: (%s)", strerror(errno));
        ret = E_PTS_ERR_FAILED;
    }

    return ret;
}

/**
 * initialize epoll fd
 *
 * @private
 *
 * @param [out] epollfd epoll fd
 *
 * @return E_PTS_ERR_BAD_PARAMETER epollfd is NULL
 * @return E_PTS_ERR_FAILED initialization fails
 * @return E_PTS_ERR_SUCCESS if successful
 */
static e_pts_errors_t init_ev_hdler(int *epollfd)
{
    e_pts_errors_t ret = E_PTS_ERR_SUCCESS;

    CHECK_PARAM(epollfd, ret, out);

    *epollfd = epoll_create(1);
    if (*epollfd == CLOSED_FD) {
        LOG_ERROR("epoll initialization failed");
        ret = E_PTS_ERR_FAILED;
    }

    out:
    return ret;
}

/**
 * handle known client request
 *
 * @param [in,out] pts pts context
 *
 * @return E_PTS_ERR_BAD_PARAMETER if pts is NULL
 * @return E_PTS_ERR_SUCCESS if successful
 * @return E_PTS_ERR_FAILED if client not found or cnx disconnection fails or client banned
 */
e_pts_errors_t known_client(pts_data_t *pts)
{
    e_pts_errors_t ret = E_PTS_ERR_FAILED;
    client_t *client = NULL;

    CHECK_PARAM(pts, ret, out);

    /* Find client in client list */
    ret = find_client(&pts->clients, pts->events.ev[pts->events.cur_ev].data.fd, &client);
    if ((ret != E_PTS_ERR_SUCCESS) || (client == NULL)) {

        LOG_ERROR("failed to find client (fd=%d)", pts->events.ev[pts->events.cur_ev].data.fd);
        /* Close file descriptor to avoid fake events */
        close(pts->events.ev[pts->events.cur_ev].data.fd);
        goto out;
    }

    /* Read header request */
    ret = get_header(pts->events.ev[pts->events.cur_ev].data.fd, &pts->request.msg.hdr);
    pts->request.client = client;
    if (ret == E_PTS_ERR_SUCCESS) {
        /* Handle client request */
        ret = client_request(pts);
    } else if (ret == E_PTS_ERR_DISCONNECTED) {
        /* Client disconnection */
        LOG_DEBUG("Client (fd=%d name=%s) is disconnected", client->fd, client->name);
        /* Remove client */
        ret = remove_client(&pts->clients, client);
    } else
        LOG_ERROR("Client (fd=%d name=%s) bad message", client->fd, client->name);

    out:
    return ret;
}

/**
 * handle new cnx connection and add client in client list
 *
 * @param [in,out] pts pts context
 *
 * @return E_PTS_ERR_BAD_PARAMETER if pts is NULL
 * @return E_PTS_ERR_FAILED if cnx connection fails or client rejected
 * @return E_PTS_ERR_SUCCESS if successful
 */
e_pts_errors_t new_client(pts_data_t *pts)
{
    e_pts_errors_t ret = E_PTS_ERR_FAILED;
    int conn_sock;
    client_t *client = NULL;
    int fd;

    CHECK_PARAM(pts, ret, out);

    fd = pts->events.ev[pts->events.cur_ev].data.fd;

    if (pts->clients.connected <= PTS_MAX_CLIENT) {
        LOG_DEBUG("try to subscribe new client fd=%d", fd);
        conn_sock = accept_cnx(fd);
        if (conn_sock < 0) {
            LOG_ERROR("Error during accept (%s)", strerror(errno));
        } else {
            if (add_fd_ev(pts->epollfd, conn_sock, EPOLLIN) == E_PTS_ERR_SUCCESS) {
                ret = add_client(&pts->clients, conn_sock, &client);
                if (ret != E_PTS_ERR_SUCCESS)
                    LOG_ERROR("failed to add new client");
            }
        }
    } else {
        LOG_INFO("client rejected: max client reached");
    }

    out:
    return ret;
}

e_pts_errors_t client_nack(pts_data_t *pts)
{
    e_pts_errors_t ret = E_PTS_ERR_SUCCESS;

    CHECK_PARAM(pts, ret, out);
    inform_client(pts->request.client, E_PTS_NACK, NULL);

    out:
    return ret;
}

/**
 * initialize the client events handlers
 *
 * @param [in,out] pts pts context
 *
 * @return E_PTS_ERR_BAD_PARAMETER if pts is NULL
 * @return E_PTS_ERR_SUCCESS if successful
 */
e_pts_errors_t client_events_init(pts_data_t *pts)
{
    e_pts_errors_t ret = E_PTS_ERR_SUCCESS;
    int i, j;

    CHECK_PARAM(pts, ret, out);

    pts->epollfd = CLOSED_FD;
    if ((ret = init_ev_hdler(&pts->epollfd)) != E_PTS_ERR_SUCCESS) {
        goto out;
    }

    ret = add_fd_ev(pts->epollfd, pts->fd_cnx, EPOLLIN);
    if (ret != E_PTS_ERR_SUCCESS) {
        goto out;
    }

    /* set default behavior */
    for (i = 0; i < E_PTS_STATE_NUM; i++)
        for (j = 0; j < E_PTS_NUM_REQUESTS; j++)
            pts->hdler_client[i][j] = client_nack;

    /* A client is ALWAYS able to establish a connection, except during
     * MDM_RESET and MDM_CONF_ONGOING. fake commands shall be accepted too */
    for (i = 0; i < E_PTS_STATE_NUM; i++) {
        pts->hdler_client[i][E_PTS_SET_NAME] = request_set_name;
        pts->hdler_client[i][E_PTS_SET_EVENTS] = request_set_events;
        pts->hdler_client[i][E_PTS_SET_INPUT_PATH_TRACE_CFG] = request_set_input_path;
        pts->hdler_client[i][E_PTS_SET_OUTPUT_PATH_TRACE_CFG] = request_set_output_path;
        pts->hdler_client[i][E_PTS_SET_ROTATE_SIZE_TRACE_CFG] = request_set_rotate_size;
        pts->hdler_client[i][E_PTS_SET_ROTATE_NUM_TRACE_CFG] = request_set_rotate_num;
        pts->hdler_client[i][E_PTS_SET_TRACE_MODE] = request_set_trace_mode;
        pts->hdler_client[i][E_PTS_REQUEST_TRACE_START] = request_trace_start;
        pts->hdler_client[i][E_PTS_REQUEST_TRACE_STOP] = request_trace_stop;
    }

    out:
    return ret;
}
