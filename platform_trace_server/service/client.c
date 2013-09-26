/* Modem Manager - client list source file
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

#include <errno.h>
#include <sys/types.h>
#include <sys/socket.h>

#include "errors.h"
#include "logs.h"
#include "data_to_msg.h"
#include "pts.h"
#include "client_cnx.h"
#include "client.h"

#define NEW_CLIENT_NAME "unknown"
#define NEW_CLIENT_PATH "unknown"

const char *g_pts_events[] = {
#undef X
#define X(a) #a
        PTS_EVENTS
};


static inline e_pts_errors_t init_client(client_t *client, int fd);
static e_pts_errors_t remove_from_list(client_list_t *clients, client_t *client);
static e_pts_errors_t set_client_signal_kill_thread(client_t *client);

/**
 * init current client
 *
 * @private
 *
 * @param [in] client current client
 * @param [in] fd client file descriptor
 *
 * @return E_PTS_ERR_BAD_PARAMETER if clients or/and client is/are NULL
 * @return E_PTS_ERR_FAILED if at least one client has not acknowledge
 * @return E_PTS_ERR_SUCCESS if successful
 */
static inline e_pts_errors_t init_client(client_t *client, int fd)
{
    e_pts_errors_t ret = E_PTS_ERR_SUCCESS;

    CHECK_PARAM(client, ret, out);

    client->fd = fd;
    client->cnx = E_CNX_NONE;
    /* users should be registered to these events */
    client->subscription = (0x1 << E_PTS_ACK) | (0x1 << E_PTS_NACK);
    strncpy(client->name, NEW_CLIENT_NAME, CLIENT_NAME_LEN);
    /* set trace configuration */
    strncpy(client->input_path, NEW_CLIENT_PATH, FILE_NAME_LEN);
    strncpy(client->output_path, NEW_CLIENT_PATH, FILE_NAME_LEN);
    client->rotate_size = CLIENT_ROTATE_SIZE;
    client->rotate_num = CLIENT_ROTATE_NUM;
    client->trace_mode = E_TRACE_MODE_NONE;

    client->fd_pipe[0] = CLOSED_FD;
    client->fd_pipe[1] = CLOSED_FD;
    client->thr_id = -1;

    clock_gettime(CLOCK_MONOTONIC, &client->time);

    out:
    return ret;
}

/**
 * remove client from client's list
 *
 * @private
 *
 * @param [in,out] clients list of clients
 * @param [in,out] client client to remove
 *
 * @return E_PTS_ERR_BAD_PARAMETER if clients or/and client is/are NULL
 * @return E_PTS_ERR_SUCCESS if successful
 */
static e_pts_errors_t remove_from_list(client_list_t *clients, client_t *client)
{
    e_pts_errors_t ret = E_PTS_ERR_SUCCESS;

    CHECK_PARAM(clients, ret, out);
    CHECK_PARAM(client, ret, out);

    clients->connected--;
    LOG_INFO("client (fd=%d name=%s) removed. still connected: %d",
            client->fd, client->name, clients->connected);

    if (client->thr_id != -1) {
        /* Send kill signal */
        ret = set_client_signal_kill_thread(client);
        client->fd = CLOSED_FD;
        client->thr_id = -1;
    }

    out:
    return ret;
}

static e_pts_errors_t set_client_signal_kill_thread(client_t *client) {

    e_pts_errors_t ret = E_PTS_ERR_SUCCESS;
    char msg = 'k';
    ssize_t size;

    CHECK_PARAM(client, ret, out);

    if (client->fd_pipe[1] != CLOSED_FD) {
        LOG_DEBUG("(fd=%d client=%s) writing signal KILL_THREAD", client->fd, client->name);
        if ((size = write(client->fd_pipe[1], &msg, sizeof(msg))) < -1) {
            LOG_ERROR("(fd=%d client=%s) write failed (%s)",
                    client->fd, client->name, strerror(errno));
            ret = E_PTS_ERR_FAILED;
        }
    }

    out:
    return ret;
}

/**
 * Check if the client is fully registered
 *
 * @param [in] client
 * @param [out] state true if registered
 *
 * @return E_PTS_ERR_BAD_PARAMETER
 * @return E_PTS_ERR_SUCCESS
 */
e_pts_errors_t is_registered(client_t *client, bool *state)
{
    e_pts_errors_t ret = E_PTS_ERR_SUCCESS;

    CHECK_PARAM(client, ret, out);
    CHECK_PARAM(state, ret, out);

    /* Client is registered if requests SET_NAME,
     * SET_INPUT_PATH, SET_OUTPUT_PATH and SET_TRACE_MODE
     * have been received by PTS
     */
    *state = (client->cnx & E_CNX_NAME) &&
            (client->cnx & E_CNX_INPUT) &&
            (client->cnx & E_CNX_OUTPUT) &&
            (client->cnx & E_CNX_TRACE_MODE);

    out:
    return ret;
}

/**
 * initialize client list structure
 *
 * @param [in,out] clients list of clients
 * @param [in] list_size size of clients
 *
 * @return E_PTS_ERR_BAD_PARAMETER if clients is NULL
 * @return E_PTS_ERR_FAILED if failed
 * @return E_PTS_ERR_SUCCESS if successful
 */
e_pts_errors_t initialize_list(client_list_t *clients, int list_size)
{
    int i;
    e_pts_errors_t ret = E_PTS_ERR_FAILED;

    CHECK_PARAM(clients, ret, out);

    clients->list_size = list_size;
    clients->list = malloc(list_size * sizeof(client_t));
    if (clients->list != NULL) {
        for (i = 0; i < list_size; i++) {
            /* Initialize all clients parameters */
            init_client(&clients->list[i], CLOSED_FD);
            clients->list[i].set_data = clients->set_data;
        }
        clients->connected = 0;

        for (i = 0; i < E_PTS_NUM_EVENTS; i++)
            clients->set_data[i] = set_msg_empty;

        ret = E_PTS_ERR_SUCCESS;
    }

    out:
    return ret;
}

/**
 * add new client to list
 *
 * @param [in,out] clients list of clients
 * @param [in] fd client file descriptor
 * @param [in,out] client pointer to new client. NULL if failed
 *
 * @return E_PTS_ERR_BAD_PARAMETER if clients or/and client is/are NULL
 * @return E_PTS_ERR_FAILED no space
 * @return E_PTS_ERR_SUCCESS if successful
 */
e_pts_errors_t add_client(client_list_t *clients, int fd, client_t **pp_client)
{
    int i;
    e_pts_errors_t ret = E_PTS_ERR_FAILED;

    CHECK_PARAM(clients, ret, out);
    CHECK_PARAM(pp_client, ret, out);

    *pp_client = NULL;

    for (i = 0; i < clients->list_size; i++) {
        if (clients->list[i].fd == CLOSED_FD) {
            /* Initialize client structure */
            init_client(&clients->list[i], fd);
            /* Increment number of clients */
            clients->connected++;
            /* Initialize pointer of new client */
            *pp_client = &clients->list[i];

            LOG_DEBUG("client (fd=%d) added. connected: %d", fd, clients->connected);
            ret = E_PTS_ERR_SUCCESS;
            break;
        }
    }

    out:
    return ret;
}

/**
 * close connexion, remove connexion on epoll and remove client from list
 *
 * @param [in,out] clients list of clients
 * @param [in,out] client current client
 *
 * @return E_PTS_ERR_BAD_PARAMETER if pts is NULL
 * @return E_PTS_ERR_SUCCESS if successful
 * @return E_PTS_ERR_FAILED otherwise
 */
e_pts_errors_t remove_client(client_list_t *clients, client_t *client)
{
    e_pts_errors_t ret = E_PTS_ERR_SUCCESS;
    int fd;

    CHECK_PARAM(clients, ret, out);
    CHECK_PARAM(client, ret, out);

    /* No needs to unsubscribe the fd from epoll list. It's automatically done
     * when the fd is closed. See epoll man page. As remove_from_list set fd to
     * CLOSED_FD, do a backup to close it */
    fd = client->fd;
    ret = remove_from_list(clients, client);
    close_cnx(&fd);

    out:
    return ret;
}

/**
 * Set client name
 *
 * @param [in,out] client client information
 * @param [in] name new client name
 * @param [in] len name length
 *
 * @return E_PTS_ERR_BAD_PARAMETER if client or name is/are NULL
 * @return E_PTS_ERR_SUCCESS if successful
 */
e_pts_errors_t set_client_name(client_t *client, char *name, size_t len)
{
    e_pts_errors_t ret = E_PTS_ERR_SUCCESS;

    CHECK_PARAM(client, ret, out);
    CHECK_PARAM(name, ret, out);

    if (len > CLIENT_NAME_LEN) {
        LOG_ERROR("client name too long");
        len = CLIENT_NAME_LEN;
    }
    strncpy(client->name, name, len);
    client->name[len] = '\0';
    LOG_DEBUG("client with fd=%d is called \"%s\"", client->fd, client->name);
    client->cnx |= E_CNX_NAME;

    out:
    return ret;
}

/**
 * Set client input path
 *
 * @param [in,out] client client information
 * @param [in] path new client input path
 * @param [in] len name length
 *
 * @return E_PTS_ERR_BAD_PARAMETER if client or name is/are NULL
 * @return E_PTS_ERR_SUCCESS if successful
 */
e_pts_errors_t set_client_input_path(client_t *client, char *name, size_t len)
{
    e_pts_errors_t ret = E_PTS_ERR_SUCCESS;

    CHECK_PARAM(client, ret, out);
    CHECK_PARAM(name, ret, out);

    if (len > FILE_NAME_LEN) {
        LOG_ERROR("client input file name too long");
        ret = E_PTS_ERR_BAD_PARAMETER;
        goto out;
    }
    strncpy(client->input_path, name, len);
    client->input_path[len] = '\0';
    LOG_DEBUG("client with fd=%d input trace file path is \"%s\"", client->fd, client->input_path);
    client->cnx |= E_CNX_INPUT;

    out:
    return ret;
}

/**
 * Set client output path
 *
 * @param [in,out] client client information
 * @param [in] path new client output path
 * @param [in] len name length
 *
 * @return E_PTS_ERR_BAD_PARAMETER if client or name is/are NULL
 * @return E_PTS_ERR_SUCCESS if successful
 */
e_pts_errors_t set_client_output_path(client_t *client, char *name, size_t len)
{
    e_pts_errors_t ret = E_PTS_ERR_SUCCESS;

    CHECK_PARAM(client, ret, out);
    CHECK_PARAM(name, ret, out);

    if (len > FILE_NAME_LEN) {
        LOG_ERROR("client output file name too long");
        ret = E_PTS_ERR_BAD_PARAMETER;
        goto out;
    }
    strncpy(client->output_path, name, len);
    client->output_path[len] = '\0';
    LOG_DEBUG("client with fd=%d output trace file path is \"%s\"",
            client->fd, client->output_path);
    client->cnx |= E_CNX_OUTPUT;

    out:
    return ret;
}

/**
 * set client filter events
 *
 * @param [in,out] client client information
 * @param [in] subscription client filter param
 *
 * @return E_PTS_ERR_BAD_PARAMETER if client or name is/are NULL
 * @return E_PTS_ERR_SUCCESS if successful
 */
e_pts_errors_t set_client_filter(client_t *client, uint32_t subscription)
{
    e_pts_errors_t ret = E_PTS_ERR_SUCCESS;

    CHECK_PARAM(client, ret, out);

    client->subscription = subscription;
    client->cnx |= E_CNX_FILTER;
    LOG_DEBUG("client (fd=%d name=%s) filter=0x%.8X", client->fd, client->name,
            client->subscription);

    out:
    return ret;
}

/**
 * set client rotate size
 *
 * @param [in,out] client client information
 * @param [in] rotate_size client rotate size param
 *
 * @return E_PTS_ERR_BAD_PARAMETER if client or name is/are NULL
 * @return E_PTS_ERR_SUCCESS if successful
 */
e_pts_errors_t set_client_rotate_size(client_t *client, uint32_t rotate_size)
{
    e_pts_errors_t ret = E_PTS_ERR_SUCCESS;

    CHECK_PARAM(client, ret, out);

    client->rotate_size = rotate_size;
    LOG_DEBUG("client (fd=%d name=%s) rotate size=%d", client->fd, client->name,
            client->rotate_size);

    out:
    return ret;
}

/**
 * set client rotate num
 *
 * @param [in,out] client client information
 * @param [in] rotate_num client rotate num param
 *
 * @return E_PTS_ERR_BAD_PARAMETER if client or name is/are NULL
 * @return E_PTS_ERR_SUCCESS if successful
 */
e_pts_errors_t set_client_rotate_num(client_t *client, uint32_t rotate_num)
{
    e_pts_errors_t ret = E_PTS_ERR_SUCCESS;

    CHECK_PARAM(client, ret, out);

    client->rotate_num = rotate_num;
    LOG_DEBUG("client (fd=%d name=%s) rotate num=%d", client->fd, client->name,
            client->rotate_num);

    out:
    return ret;
}

/**
 * set client trace mode
 *
 * @param [in,out] client client information
 * @param [in] trace_mode client trace mode param
 *
 * @return E_PTS_ERR_BAD_PARAMETER if client or name is/are NULL
 * @return E_PTS_ERR_SUCCESS if successful
 */
e_pts_errors_t set_client_trace_mode(client_t *client, e_trace_mode_t trace_mode)
{
    e_pts_errors_t ret = E_PTS_ERR_SUCCESS;

    CHECK_PARAM(client, ret, out);

    client->trace_mode = trace_mode;
    client->cnx |= E_CNX_TRACE_MODE;
    LOG_DEBUG("client (fd=%d name=%s) trace mode=%d", client->fd, client->name,
            client->trace_mode);

    out:
    return ret;
}

e_pts_errors_t set_client_signal_trace_start(client_t *client) {

    e_pts_errors_t ret = E_PTS_ERR_SUCCESS;
    char msg = '1';
    ssize_t size;

    CHECK_PARAM(client, ret, out);

    if (client->fd_pipe[1] != CLOSED_FD) {
        LOG_DEBUG("(fd=%d client=%s) writing signal TRACE_START", client->fd, client->name);
        if ((size = write(client->fd_pipe[1], &msg, sizeof(msg))) < -1) {
            LOG_ERROR("(fd=%d client=%s) write failed (%s)",
                    client->fd, client->name, strerror(errno));
            ret = E_PTS_ERR_FAILED;
        }
    }

    out:
    return ret;
}

e_pts_errors_t set_client_signal_trace_stop(client_t *client) {

    e_pts_errors_t ret = E_PTS_ERR_SUCCESS;
    char msg = '0';
    ssize_t size;

    CHECK_PARAM(client, ret, out);

    if (client->fd_pipe[1] != CLOSED_FD) {
        LOG_DEBUG("(fd=%d client=%s) writing signal TRACE_STOP", client->fd, client->name);
        if ((size = write(client->fd_pipe[1], &msg, sizeof(msg))) < -1) {
            LOG_ERROR("(fd=%d client=%s) write failed (%s)",
                    client->fd, client->name, strerror(errno));
            ret = E_PTS_ERR_FAILED;
        }
    }

    out:
    return ret;
}

/**
 * find the client on client list
 *
 * @param [in] clients list of clients
 * @param [in] fd client's file descriptor
 * @param[out] client client found
 *
 * @return E_PTS_ERR_BAD_PARAMETER clients or/and client is/are NULL
 * @return E_PTS_ERR_FAILED if not found
 * @return E_PTS_ERR_SUCCESS if successful
 */
e_pts_errors_t find_client(client_list_t *clients, int fd, client_t **pp_client)
{
    e_pts_errors_t ret = E_PTS_ERR_FAILED;
    int i;

    CHECK_PARAM(clients, ret, out);
    CHECK_PARAM(pp_client, ret, out);

    *pp_client = NULL;

    for (i = 0; i < clients->list_size; i++) {
        if (fd == clients->list[i].fd) {
            *pp_client = &clients->list[i];
            ret = E_PTS_ERR_SUCCESS;
            break;
        }
    }

    out:
    return ret;
}

/**
 * send message with data to client
 *
 * @param [in] client client to inform
 * @param [in] state state to provide
 * @param [in] data data to send
 *
 * @return E_PTS_ERR_BAD_PARAMETER clients or/and client is/are NULL
 * @return E_PTS_ERR_SUCCESS if not found
 * @return E_PTS_ERR_SUCCESS if successful
 */
e_pts_errors_t inform_client(client_t *client, e_pts_events_t event, void *data)
{
    size_t size;
    size_t write_size;
    e_pts_errors_t ret = E_PTS_ERR_SUCCESS;
    pts_cli_event_t evt = {.id = event,.data = data };
    msg_t msg = {.data = NULL };

    CHECK_PARAM(client, ret, out);
    /* do not check data because it can be NULL on purpose */

    if (client->set_data[event] == NULL) {
        LOG_ERROR("function is NULL");
        ret = E_PTS_ERR_FAILED;
        goto out;
    }

    client->set_data[event] (&msg, &evt);

    size = SIZE_HEADER + msg.hdr.len;
    write_size = size;
    if ((0x01 << event) & client->subscription) {
        if ((ret = write_cnx(client->fd, msg.data, &write_size)) !=
                E_PTS_ERR_SUCCESS)
            goto out;

        if (size != write_size) {
            LOG_ERROR("send failed for client (fd=%d name=%s) send=%d/%d",
                    client->fd, client->name, write_size, size);
            ret = E_PTS_ERR_FAILED;
        } else {
            LOG_DEBUG("Client (fd=%d name=%s) informed of: %s", client->fd,
                    client->name, g_pts_events[event]);
        }
    } else {
        LOG_DEBUG("Client (fd=%d name=%s) NOT informed of: %s",
                client->fd, client->name, g_pts_events[event]);
    }

    out:
    delete_msg(&msg);
    return ret;
}

/**
 * inform all clients of PTS state
 *
 * @param [in,out] clients list of clients
 * @param [in] state current modem state
 *
 * @return E_PTS_ERR_BAD_PARAMETER if clients is NULL
 * @return E_PTS_ERR_SUCCESS if successful
 * @return E_PTS_ERR_FAILED otherwise
 */
e_pts_errors_t inform_all_clients(client_list_t *clients,
        e_pts_events_t state, void *data)
{
    e_pts_errors_t ret = E_PTS_ERR_SUCCESS;
    int i;

    CHECK_PARAM(clients, ret, out);
    /* Do not check data because it can be NULL on purpose */

    for (i = 0; i < clients->list_size; i++) {
        if (clients->list[i].fd != CLOSED_FD)
            ret = inform_client(&clients->list[i], state, data);
    }

    out:
    return ret;
}

/**
 * terminate all client threads and close all connexion's client fd
 *
 * @param [in,out] clients list of clients
 *
 * @return E_PTS_ERR_BAD_PARAMETER if clients is NULL
 * @return E_PTS_ERR_SUCCESS if successful
 */
e_pts_errors_t close_all_clients(client_list_t *clients)
{
    e_pts_errors_t ret = E_PTS_ERR_SUCCESS;
    int i;

    CHECK_PARAM(clients, ret, out);

    /* Inform all clients of PTS termination */
    ret = inform_all_clients(clients, E_PTS_EVENT_PTS_DOWN, NULL);

    for (i = 0; i < clients->list_size; i++) {
        if (clients->list[i].fd != CLOSED_FD) {
            LOG_DEBUG("i=%d fd=%d", i, clients->list[i].fd);
            /* Send client's thread kill signal */
            ret = set_client_signal_kill_thread(&clients->list[i]);
            clients->list[i].thr_id = -1;
            /* Close client connection */
            close_cnx(&clients->list[i].fd);
        }
    }

    out:
    return ret;
}
