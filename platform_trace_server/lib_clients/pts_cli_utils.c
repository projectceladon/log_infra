/* Platform Trace Server library - utils source file
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
#include <stdlib.h>
#include <string.h>
#include <sys/types.h>
#include <unistd.h>
#include <errno.h>
#include <sys/epoll.h>
#include <cutils/sockets.h>
#include <limits.h>
#include <pthread.h>
#include <signal.h>
#include <stdbool.h>
#include <time.h>

#include "pts_cli_common.h"
#include "pts_cli_utils.h"

const char *g_pts_events[] = {
#undef X
#define X(a) #a
        PTS_EVENTS
};

const char *g_pts_requests[] = {
#undef X
#define X(a) #a
        PTS_REQUESTS
};

typedef struct request_list {
    e_pts_requests_t id;
    struct request_list * next;
} request_list_t;

request_list_t * gpts_request_list = NULL;

static request_list_t * add_request(const e_pts_requests_t id)
{
    request_list_t * temp;
    request_list_t * new_elmt = malloc(sizeof(request_list_t));

    new_elmt->id = id;
    new_elmt->next = NULL;

    if (gpts_request_list == NULL) {
        /* Empty list */
        gpts_request_list = new_elmt;
        return new_elmt;
    } else {
        temp = gpts_request_list;
        while (temp->next != NULL) {
            temp = temp->next;
        }
        temp->next = new_elmt;
        return gpts_request_list;
    }
}

static void rm_request(const e_pts_requests_t id)
{
    request_list_t * temp;
    request_list_t * temp1;
    request_list_t * temp2;

    temp = gpts_request_list;
    while (temp != NULL) {
        if (temp->id == id) {
            gpts_request_list = temp->next;
            free(temp);
            temp = NULL;
        } else {
            temp1 = temp->next;
            if (temp1->id == id) {
                /* temp1 contains element to be deleted */
                temp2 = temp1->next;
                temp->next = temp2;
                free(temp1);
                temp1 = NULL;
            }
            temp = temp->next;
        }
    }
}

static inline e_pts_events_t get_ack(pts_lib_context_t *p_lib)
{
    e_pts_events_t ack;
    pthread_mutex_lock(&p_lib->mtx);
    ack = p_lib->ack;
    pthread_mutex_unlock(&p_lib->mtx);

    return ack;
}

static inline void set_ack(pts_lib_context_t *p_lib, e_pts_events_t ack)
{
    pthread_mutex_lock(&p_lib->mtx);
    p_lib->ack = ack;
    pthread_mutex_unlock(&p_lib->mtx);
}

static e_err_pts_cli_t ev_ack(pts_lib_context_t *p_lib, e_pts_events_t id)
{
    e_err_pts_cli_t ret = E_ERR_PTS_CLI_FAILED;

    CHECK_CLI_PARAM(p_lib, ret, out);

    set_ack(p_lib, id);

    pthread_mutex_lock(&p_lib->mtx_signal);
    pthread_cond_signal(&p_lib->cond);
    pthread_mutex_unlock(&p_lib->mtx_signal);

    ret = E_ERR_PTS_CLI_SUCCEED;

    out:
    return ret;
}

static e_err_pts_cli_t call_cli_callback(pts_lib_context_t *p_lib, msg_t *msg)
{
    e_err_pts_cli_t ret = E_ERR_PTS_CLI_FAILED;
    pts_cli_event_t event = {.context = p_lib->cli_ctx };
    e_pts_events_t id;
    struct timespec start, end;

    memcpy(&id, &msg->hdr.id, sizeof(e_pts_events_t));

    if (id < E_PTS_NUM_EVENTS) {
        LOG_DEBUG("(fd=%d client=%s) event (%s) received",
                p_lib->fd_socket, p_lib->cli_name, g_pts_events[id]);
        if ((id == E_PTS_ACK) || (id == E_PTS_NACK)) {
            ret = ev_ack(p_lib, id);
        } else if (p_lib->func[id] != NULL) {
            event.id = id;
            p_lib->set_data[id] (msg, &event);

            pthread_mutex_lock(&p_lib->mtx);
            p_lib->tid = gettid();
            pthread_mutex_unlock(&p_lib->mtx);

            clock_gettime(CLOCK_BOOTTIME, &start);
            /* Call client callback */
            p_lib->func[id] (&event);
            clock_gettime(CLOCK_BOOTTIME, &end);

            LOG_VERBOSE("(fd=%d client=%s) callback for event (%s) "
                    "handled in %ld ms",
                    p_lib->fd_socket, p_lib->cli_name, g_pts_events[id],
                    ((end.tv_sec - start.tv_sec) * 1000) +
                    ((end.tv_nsec - start.tv_nsec) / 1000000));

            p_lib->free_data[id] (&event);
            ret = E_ERR_PTS_CLI_SUCCEED;
        } else {
            LOG_ERROR("(fd=%d client=%s) func is NULL",
                    p_lib->fd_socket, p_lib->cli_name);
        }
    } else {
        LOG_DEBUG("(fd=%d client=%s) unkwnown event received (0x%.2X)",
                p_lib->fd_socket, p_lib->cli_name, msg->hdr.id);
    }

    return ret;
}

/**
 * Read a message in the socket
 *
 * @param[in] p_lib
 * @param[out] msg. It's user responsability to free this buffer
 *
 * @return E_ERR_PTS_CLI_FAILED
 * @return E_ERR_PTS_CLI_BAD_HANDLE if p_lib and/or msg is NULL
 * @return E_ERR_PTS_CLI_BAD_CNX_STATE if client is disconnected
 * @return E_ERR_PTS_CLI_SUCCEED
 */
static e_err_pts_cli_t read_msg(pts_lib_context_t *p_lib, msg_t *msg)
{
    e_err_pts_cli_t ret = E_ERR_PTS_CLI_FAILED;
    e_pts_errors_t err = E_PTS_ERR_FAILED;

    CHECK_CLI_PARAM(p_lib, ret, out);
    CHECK_CLI_PARAM(msg, ret, out);

    memset(msg, 0, sizeof(msg_t));

    /* Read msg data */
    err = get_header(p_lib->fd_socket, &msg->hdr);
    if (err == E_PTS_ERR_DISCONNECTED) {
        LOG_DEBUG("(fd=%d client=%s) connection closed by PTS",
                p_lib->fd_socket, p_lib->cli_name);

        ret = E_ERR_PTS_CLI_BAD_CNX_STATE;
    } else {
        e_pts_events_t id = E_PTS_NUM_EVENTS;
        size_t size = 0;

        memcpy(&id, &msg->hdr.id, sizeof(e_pts_events_t));
        memcpy(&size, &msg->hdr.len, sizeof(size_t));
        if (size != 0) {
            msg->data = calloc(size, sizeof(char));
            if (msg->data == NULL) {
                LOG_ERROR("memory allocation fails");
                goto out;
            }

            size_t read_size = size;
            err = read_cnx(p_lib->fd_socket, msg->data, &read_size);
            if ((err != E_PTS_ERR_SUCCESS) || (read_size != size)) {
                LOG_ERROR("Read error. Size: %d/%d", read_size, size);
            } else {
                ret = E_ERR_PTS_CLI_SUCCEED;
            }
        } else {
            ret = E_ERR_PTS_CLI_SUCCEED;
        }
    }

    out:
    return ret;
}

/**
 * function to handle cnx event
 *
 * @private
 *
 * @param [in] p_lib library handle
 *
 * @return E_ERR_PTS_CLI_BAD_HANDLE if p_lib is invalid
 * @return E_ERR_PTS_CLI_FAILED if not connected or invalid request id
 * @return E_ERR_PTS_CLI_BAD_CNX_STATE if client is disconnected
 * @return E_ERR_PTS_CLI_SUCCEED
 */
static e_err_pts_cli_t handle_cnx_event(pts_lib_context_t *p_lib)
{
    e_err_pts_cli_t ret = E_ERR_PTS_CLI_FAILED;
    msg_t msg = {.data = NULL };

    CHECK_CLI_PARAM(p_lib, ret, out);

    ret = read_msg(p_lib, &msg);
    if (ret == E_ERR_PTS_CLI_SUCCEED)
        ret = call_cli_callback(p_lib, &msg);

    delete_msg(&msg);

    out:
    return ret;
}

/**
 * handle events provided by select
 *
 * @private
 *
 * @param [in] p_lib private structure
 * @param [in] rfds read events
 *
 * @return E_ERR_PTS_CLI_FAILED
 * @return E_ERR_PTS_CLI_BAD_HANDLE
 * @return E_ERR_PTS_CLI_SUCCEED
 * @return E_ERR_PTS_CLI_BAD_CNX_STATE
 */
static e_err_pts_cli_t handle_events(pts_lib_context_t *p_lib, fd_set *rfds)
{
    e_err_pts_cli_t ret = E_ERR_PTS_CLI_FAILED;
    char buffer[PIPE_BUF];

    CHECK_CLI_PARAM(p_lib, ret, out);
    CHECK_CLI_PARAM(rfds, ret, out);

    if (FD_ISSET(p_lib->fd_pipe[READ], rfds)) {
        read(p_lib->fd_pipe[READ], buffer, PIPE_BUF);
        LOG_DEBUG("(fd=%d client=%s) stopping thread",
                p_lib->fd_socket, p_lib->cli_name);
        ret = E_ERR_PTS_CLI_BAD_CNX_STATE;
    } else if (FD_ISSET(p_lib->fd_socket, rfds)) {
        ret = handle_cnx_event(p_lib);
    } else {
        LOG_DEBUG("event not handled");
    }

    out:
    return ret;
}

/**
 * handle client disconnection
 *
 * @private
 *
 * @param [in] p_lib library handle
 *
 * @return E_ERR_PTS_CLI_FAILED if client is disconnected
 * @return E_ERR_PTS_CLI_BAD_HANDLE if p_lib is NULL
 */
static e_err_pts_cli_t handle_disconnection(pts_lib_context_t *p_lib)
{
    e_err_pts_cli_t ret = E_ERR_PTS_CLI_FAILED;

    CHECK_CLI_PARAM(p_lib, ret, out);

    LOG_DEBUG("(fd=%d client=%s) disconnected", p_lib->fd_socket, p_lib->cli_name);

    pthread_mutex_lock(&p_lib->mtx);
    p_lib->connected = E_CNX_DISCONNECTED;
    close_cnx(&p_lib->fd_socket);
    close(p_lib->fd_pipe[READ]);
    close(p_lib->fd_pipe[WRITE]);
    p_lib->fd_pipe[READ] = CLOSED_FD;
    p_lib->fd_pipe[WRITE] = CLOSED_FD;
    pthread_mutex_unlock(&p_lib->mtx);

    out:
    return ret;
}

/**
 * check if client is connected or not. This is a sensitive data
 *
 * @param [in] p_lib private structure
 * @param [out] answer true if connected, false otherwise
 *
 * @private
 *
 * @return E_ERR_PTS_CLI_BAD_HANDLE if handle is invalid
 * @return E_ERR_PTS_CLI_SUCCEED
 */
static e_err_pts_cli_t is_connected(pts_lib_context_t *p_lib, bool *answer)
{
    e_err_pts_cli_t ret = E_ERR_PTS_CLI_SUCCEED;

    CHECK_CLI_PARAM(p_lib, ret, out);

    pthread_mutex_lock(&p_lib->mtx);
    *answer = (p_lib->connected == E_CNX_CONNECTED);
    pthread_mutex_unlock(&p_lib->mtx);

    out:
    return ret;
}

/**
 * send registration sequence to PTS.
 * send requests:
 * - E_PTS_SET_NAME
 * - E_PTS_SET_EVENTS (if necessary)
 * - E_PTS_SET_INPUT_PATH_TRACE_CFG
 * - E_PTS_SET_OUTPUT_PATH_TRACE_CFG
 * - E_PTS_SET_ROTATE_SIZE_TRACE_CFG (if necessary)
 * - E_PTS_SET_ROTATE_NUM_TRACE_CFG (if necessary)
 * - E_PTS_SET_TRACE_MODE
 *
 * @param [in] p_lib
 *
 * @private
 *
 * @return E_ERR_PTS_CLI_BAD_HANDLE if handle is invalid
 * @return E_ERR_PTS_CLI_FAILED if timeout or PTS not responsive
 * @return E_ERR_PTS_CLI_SUCCEED
 */
static e_err_pts_cli_t register_client(pts_lib_context_t *p_lib)
{
    e_err_pts_cli_t ret = E_ERR_PTS_CLI_FAILED;
    pts_cli_requests_t request;
    int timeout = DEF_PTS_RESPONSIVE_TIMEOUT;
    struct timespec start, ts;

    CHECK_CLI_PARAM(p_lib, ret, out);

    /* Build request Id sequence (optional requests could be not sent) */
    /* Mandatory request */
    add_request(E_PTS_SET_NAME);
    if (p_lib->events != ((0x1 << E_PTS_ACK) | (0x1 << E_PTS_NACK))) {
        /* Client has subscribed at least one event. Optional request shall be sent */
        add_request(E_PTS_SET_EVENTS);
    }
    /* Mandatory request */
    add_request(E_PTS_SET_INPUT_PATH_TRACE_CFG);
    /* Mandatory request */
    add_request(E_PTS_SET_OUTPUT_PATH_TRACE_CFG);
    if (p_lib->rotate_size != CLIENT_ROTATE_SIZE) {
        /* Rotate size value different from default value. Optional request shall be sent */
        add_request(E_PTS_SET_ROTATE_SIZE_TRACE_CFG);
    }
    if (p_lib->rotate_num != CLIENT_ROTATE_NUM) {
        /* Rotate number value different from default value. Optional request shall be sent */
        add_request(E_PTS_SET_ROTATE_NUM_TRACE_CFG);
    }
    /* IMPORTANT NOTE: E_PTS_SET_TRACE_MODE SHALL BE THE LAST REQUEST ADDED TO THE LINKED LIST
     * AS THE SERVER RECEIVES THIS REQUEST, PTS SERVER CONSIDERS
     * THE CLIENT REGISTERED AND ACCEPTED
     */
    /* Mandatory request */
    add_request(E_PTS_SET_TRACE_MODE);

    clock_gettime(CLOCK_REALTIME, &start);
    while (gpts_request_list != NULL) {
        /* Build request to be sent */
        request.id = gpts_request_list->id;
        switch (gpts_request_list->id) {
        case E_PTS_SET_NAME:
            request.len = strnlen(p_lib->cli_name, CLIENT_NAME_LEN);
            request.data = &p_lib->cli_name;
            break;

        case E_PTS_SET_EVENTS:
            request.len = sizeof(uint32_t);
            request.data = &p_lib->events;
            break;

        case E_PTS_SET_INPUT_PATH_TRACE_CFG:
            request.len = strnlen(p_lib->input_path, FILE_NAME_LEN);
            request.data = &p_lib->input_path;
            break;

        case E_PTS_SET_OUTPUT_PATH_TRACE_CFG:
            request.len = strnlen(p_lib->output_path, FILE_NAME_LEN);
            request.data = &p_lib->output_path;
            break;

        case E_PTS_SET_ROTATE_SIZE_TRACE_CFG:
            request.len = sizeof(uint32_t);
            request.data = &p_lib->rotate_size;
            break;

        case E_PTS_SET_ROTATE_NUM_TRACE_CFG:
            request.len = sizeof(uint32_t);
            request.data = &p_lib->rotate_num;
            break;

        case E_PTS_SET_TRACE_MODE:
            request.len = sizeof(uint32_t);
            request.data = &p_lib->trace_mode;
            break;

        default:
            LOG_ERROR("(fd=%d client=%s) failed to register (unknown request)",
                    p_lib->fd_socket, p_lib->cli_name);
            break;
        }

        /* Send request to establish connection sequence */
        if ((ret = send_msg(p_lib, &request, E_SEND_PRE_CONNECTION, timeout))
                != E_ERR_PTS_CLI_SUCCEED)
            break;

        rm_request(request.id);

        clock_gettime(CLOCK_REALTIME, &ts);
        timeout = DEF_PTS_RESPONSIVE_TIMEOUT - (ts.tv_sec - start.tv_sec);
    }

    if (ret == E_ERR_PTS_CLI_SUCCEED) {
        LOG_DEBUG("(fd=%d client=%s) connected successfully", p_lib->fd_socket, p_lib->cli_name);
    } else {
        LOG_ERROR("(fd=%d client=%s) failed to connect", p_lib->fd_socket, p_lib->cli_name);
    }

    out:
    return ret;
}

/**
 * wait for a PTS acknowledge
 *
 * @param [in] fd file descriptor
 * @param [in] timeout timeout (in milliseconds)
 *
 * @return E_ERR_PTS_CLI_TIMEOUT if timeout occurs
 * @return E_ERR_PTS_CLI_FAILED if epoll create fails
 * @return E_ERR_PTS_CLI_SUCCEED if successful
 */
static e_err_pts_cli_t wait_for_ack_event(int fd, int timeout)
{
    struct epoll_event ev;
    int epollfd = CLOSED_FD;
    int err;
    e_err_pts_cli_t ret = E_ERR_PTS_CLI_SUCCEED;

    epollfd = epoll_create(1);
    if (epollfd == CLOSED_FD) {
        LOG_ERROR("Failed to initialize epollfd: (%s)", strerror(errno));
        ret = E_ERR_PTS_CLI_FAILED;
        goto out;
    }

    ev.events = EPOLLIN | EPOLLHUP;
    ev.data.fd = fd;
    if (epoll_ctl(epollfd, EPOLL_CTL_ADD, fd, &ev) == -1) {
        LOG_ERROR("Failed to add fd: (%s)", strerror(errno));
        ret = E_ERR_PTS_CLI_FAILED;
        goto out;
    }

    err = epoll_wait(epollfd, &ev, 1, timeout);
    if (err > 0) {
        if (ev.events & EPOLLHUP) {
            LOG_VERBOSE("POLLHUP received");
            ret = E_ERR_PTS_CLI_SUCCEED;
        } else if (ev.events & EPOLLIN) {
            LOG_VERBOSE("Received response data");
            ret = E_ERR_PTS_CLI_SUCCEED;
        } else {
            LOG_ERROR("Unexpected event (%d)", ev.events);
            ret = E_ERR_PTS_CLI_FAILED;
        }
    } else if (err == 0) {
        LOG_ERROR("Wait answer timeout");
        ret = E_ERR_PTS_CLI_TIMEOUT;
    } else {
        LOG_ERROR("Poll failed (%s)", strerror(errno));
        ret = E_ERR_PTS_CLI_FAILED;
    }

    out:
    if (epollfd != CLOSED_FD)
        close(epollfd);
    return ret;
}

/**
 * check current library state
 *
 * @param [in] handle library handle
 * @param [out] p_lib library handle with correct cast
 * @param [in] connected check if client is connected or not
 *
 * @return E_ERR_PTS_CLI_BAD_HANDLE if handle is invalid
 * @return E_ERR_PTS_CLI_BAD_CNX_STATE if bad cnx state
 * @return E_ERR_PTS_CLI_SUCCEED
 */
e_err_pts_cli_t check_state(pts_cli_handle_t *handle,
        pts_lib_context_t **p_lib, bool connected)
{
    e_err_pts_cli_t ret = E_ERR_PTS_CLI_SUCCEED;
    char *state_str[] = { "disconnected", "connected" };
    bool state = false;

    CHECK_CLI_PARAM(handle, ret, out);
    CHECK_CLI_PARAM(p_lib, ret, out);

    *p_lib = (pts_lib_context_t *)handle;

#ifdef DEBUG_PTS_CLI
    if ((*p_lib)->init != INIT_CHECK) {
        LOG_ERROR("handle is not configured");
        ret = E_ERR_PTS_CLI_BAD_HANDLE;
        goto out;
    }
#endif

    is_connected(*p_lib, &state);
    if (state != connected) {
        ret = E_ERR_PTS_CLI_BAD_CNX_STATE;
        LOG_ERROR("(fd=%d client=%s) WRONG STATE: client is %s instead of %s",
                (*p_lib)->fd_socket, (*p_lib)->cli_name,
                state_str[state], state_str[connected]);
    }

    out:
    return ret;
}

/**
 * check mandatory trace parameters
 *
 * @param [in] p_lib library handle
 *
 * @return E_ERR_PTS_CLI_FAILED if a required parameter is not set
 * @return E_ERR_PTS_CLI_SUCCEED
 */
e_err_pts_cli_t check_mandatory_parameters(pts_lib_context_t *p_lib)
{
    e_err_pts_cli_t ret = E_ERR_PTS_CLI_FAILED;

    if (p_lib->input_path[0] == 0) {
        LOG_ERROR("(client=%s) WRONG TRACE PARAMETER: Input path is not set", p_lib->cli_name);
        goto out;
    }

    if (p_lib->output_path[0] == 0) {
        LOG_ERROR("(client=%s) WRONG TRACE PARAMETER: Output path is not set", p_lib->cli_name);
        goto out;
    }

    if (p_lib->trace_mode == E_TRACE_MODE_NONE) {
        LOG_ERROR("(client=%s) WRONG TRACE PARAMETER: Trace mode is not set", p_lib->cli_name);
        goto out;
    }

    ret = E_ERR_PTS_CLI_SUCCEED;

    out:
    return ret;
}

/**
 * send an PTS request. This function uses the reader thread to wait for PTS's
 * answer or wait for an event on the link. It depends of the chosen method.
 *
 * @param [in] p_lib library handle
 * @param [in] request request to send to the PTS
 * @param [in] method. if this function is called before client is registered, you should use
 *        the E_SEND_PRE_CONNECTION method, E_SEND_POST_CONNECTION method is used otherwise
 *        (typically when a client sends a request)
 * @param [in] timeout (in seconds)
 *
 * @return E_ERR_PTS_CLI_TIMEOUT if PTS is not responsive or after a timeout of 5s
 * @return E_ERR_PTS_CLI_FAILED if request is NULL or invalid request id or invalid timeout
 * @return E_ERR_PTS_CLI_SUCCEED if message accepted (ACK received)
 */
e_err_pts_cli_t send_msg(pts_lib_context_t *p_lib,
        const pts_cli_requests_t *request,
        e_send_method_t method, int timeout)
{
    e_err_pts_cli_t ret = E_ERR_PTS_CLI_FAILED;
    e_pts_errors_t ret_tmp = E_PTS_ERR_SUCCESS;
    e_err_pts_cli_t ret_wait = E_ERR_PTS_CLI_FAILED;
    msg_t msg = {.data = NULL };
    size_t size = 0;
    struct timespec start, ts;
    int sleep_duration = 1;
    msg_t answer;

    if (request == NULL) {
        LOG_ERROR("request is NULL");
        goto out;
    }

    if (request->id >= E_PTS_NUM_REQUESTS) {
        LOG_ERROR("bad request");
        goto out;
    }

    if (timeout <= 0)
        goto timeout;

    set_ack(p_lib, E_PTS_NUM_EVENTS);
    p_lib->set_msg[request->id] (&msg, (void *)request);

    clock_gettime(CLOCK_REALTIME, &start);
    /* The loop ends after PTS approval or timeout */
    while (true) {
        /* Lock the mutex before sending the request. Otherwise, the answer can
         * be handled before waiting for the signal */
        pthread_mutex_lock(&p_lib->mtx_signal);

        size = SIZE_HEADER + msg.hdr.len;
        ret_tmp = write_cnx(p_lib->fd_socket, msg.data, &size);
        if ((ret_tmp != E_PTS_ERR_SUCCESS) || (size != (SIZE_HEADER + msg.hdr.len))) {
            LOG_ERROR("write failed");
            break;
        }

        LOG_DEBUG("(fd=%d client=%s) request (%s) sent successfully",
                p_lib->fd_socket, p_lib->cli_name,
                g_pts_requests[request->id]);

        LOG_DEBUG("(fd=%d client=%s) Waiting for answer", p_lib->fd_socket,
                p_lib->cli_name);

        if (method == E_SEND_PRE_CONNECTION) {
            ret_wait = wait_for_ack_event(p_lib->fd_socket, timeout * 1000);
            if (ret_wait == E_ERR_PTS_CLI_TIMEOUT) {
                break;
            }
            memset(&answer, 0, sizeof(msg_t));
            if (read_msg(p_lib, &answer) == E_ERR_PTS_CLI_SUCCEED)
                set_ack(p_lib, answer.hdr.id);
            delete_msg(&answer);
        } else {
            clock_gettime(CLOCK_REALTIME, &ts);
            ts.tv_sec += timeout;
            if (pthread_cond_timedwait(&p_lib->cond, &p_lib->mtx_signal, &ts) == ETIMEDOUT) {
                ret_tmp = E_PTS_ERR_TIMEOUT;
                break;
            } else {
                ret_tmp = E_PTS_ERR_SUCCESS;
            }
        }

        if (get_ack(p_lib) == E_PTS_ACK) {
            ret = E_ERR_PTS_CLI_SUCCEED;
            break;
        }
        pthread_mutex_unlock(&p_lib->mtx_signal);

        clock_gettime(CLOCK_REALTIME, &ts);
        timeout = DEF_PTS_RESPONSIVE_TIMEOUT - (ts.tv_sec - start.tv_sec);
        if ((timeout > 0) && (++sleep_duration <= timeout))
            sleep(sleep_duration);
        else
            break; /* timeout expired */
    }

    timeout:
    if (ret_tmp == E_PTS_ERR_TIMEOUT) {
        /* This happens if: PTS is not responsive OR if client's callback
         * takes too much time (E_SEND_THREAD only). Indeed, the callback is
         * called by the consumer thread. */
        LOG_DEBUG("(fd=%d client=%s) timeout for request (%s)",
                p_lib->fd_socket, p_lib->cli_name,
                g_pts_requests[request->id]);
        ret = E_ERR_PTS_CLI_TIMEOUT;
    }
    out:
    /* when we break the do {} while loop, the mutex is not ALWAYS unlocked.
     * To be safe, try to lock it before unlocking it
     */
    pthread_mutex_trylock(&p_lib->mtx_signal);
    pthread_mutex_unlock(&p_lib->mtx_signal);
    delete_msg(&msg);

    return ret;
}

/**
 * handle PTS events and dispatch them
 *
 * @param [in] p_lib private structure
 *
 * @return E_ERR_PTS_CLI_BAD_HANDLE if p_lib is NULL
 * @return E_ERR_PTS_CLI_SUCCEED at the end
 */
e_err_pts_cli_t read_events(pts_lib_context_t *p_lib)
{
    e_err_pts_cli_t ret = E_ERR_PTS_CLI_SUCCEED;
    fd_set rfds;
    int fd_max;

    CHECK_CLI_PARAM(p_lib, ret, out);

    if (p_lib->fd_socket > p_lib->fd_pipe[READ])
        fd_max = p_lib->fd_socket;
    else
        fd_max = p_lib->fd_pipe[READ];

    do {
        FD_ZERO(&rfds);
        FD_SET(p_lib->fd_pipe[READ], &rfds);
        FD_SET(p_lib->fd_socket, &rfds);

        if (select(fd_max + 1, &rfds, NULL, NULL, NULL) < 0) {
            LOG_ERROR("select failed (%s)", strerror(errno));
            break;
        }

        ret = handle_events(p_lib, &rfds);
        if (ret == E_ERR_PTS_CLI_BAD_CNX_STATE)
            ret = handle_disconnection(p_lib);

    } while (ret != E_ERR_PTS_CLI_FAILED);

    out:
    LOG_DEBUG("(client=%s) end of thread", p_lib->cli_name);
    pthread_exit(&ret);
    return ret;
}

/**
 * connect the client to PTS
 *
 * @param [in] p_lib library handle
 *
 * @return E_ERR_PTS_CLI_FAILED
 * @return E_ERR_PTS_CLI_BAD_HANDLE
 * @return E_ERR_PTS_CLI_SUCCEED
 */
e_err_pts_cli_t cli_connect(pts_lib_context_t *p_lib)
{
    e_err_pts_cli_t ret = E_ERR_PTS_CLI_FAILED;
    int fd = CLOSED_FD;

    fd = socket_local_client(PTS_SOCKET_NAME, ANDROID_SOCKET_NAMESPACE_RESERVED, SOCK_STREAM);
    if (fd < 0) {
        LOG_ERROR("(client=%s) failed to open socket", p_lib->cli_name);
        goto out;
    }

    pthread_mutex_lock(&p_lib->mtx);
    p_lib->fd_socket = fd;
    pthread_mutex_unlock(&p_lib->mtx);

    ret = register_client(p_lib);

    out:
    return ret;
}

/**
 * disconnect the client
 *
 * @param p_lib library handle
 *
 * @return E_ERR_PTS_CLI_BAD_HANDLE
 * @return E_ERR_PTS_CLI_SUCCEED
 * @return E_ERR_PTS_CLI_FAILED if already disconnected
 */
e_err_pts_cli_t cli_disconnect(pts_lib_context_t *p_lib)
{
    e_err_pts_cli_t ret = E_ERR_PTS_CLI_FAILED;
    bool connected = false;
    char msg = 0;
    ssize_t size;

    is_connected(p_lib, &connected);
    if (connected) {
        LOG_DEBUG("(fd=%d client=%s) writing signal", p_lib->fd_socket,
                p_lib->cli_name);
        if ((size = write(p_lib->fd_pipe[WRITE], &msg, sizeof(msg))) < -1) {
            LOG_ERROR("(fd=%d client=%s) write failed (%s)",
                    p_lib->fd_socket, p_lib->cli_name, strerror(errno));
        }
    }

    LOG_DEBUG("(fd=%d client=%s) waiting for end of reading thread",
            p_lib->fd_socket, p_lib->cli_name);
    if (p_lib->thr_id != -1) {
        pthread_join(p_lib->thr_id, NULL);
        p_lib->thr_id = -1;
    }

    LOG_DEBUG("(fd=%d client=%s) reading thread stopped",
            p_lib->fd_socket, p_lib->cli_name);

    is_connected(p_lib, &connected);
    if (!connected) {
        LOG_DEBUG("(fd=%d client=%s) is disconnected", p_lib->fd_socket,
                p_lib->cli_name);
        ret = E_ERR_PTS_CLI_SUCCEED;
    } else {
        LOG_ERROR("(fd=%d client=%s) failed to disconnect",
                p_lib->fd_socket, p_lib->cli_name);
    }

    return ret;
}

/**
 * check if request shall be rejected
 *
 * @param p_lib library handle
 *
 * @return E_ERR_PTS_CLI_SUCCEED
 * @return E_ERR_PTS_CLI_REJECTED if request shall be rejected
 */
e_pts_events_t is_request_rejected(pts_lib_context_t *p_lib)
{
    e_err_pts_cli_t ret = E_ERR_PTS_CLI_SUCCEED;

    pthread_mutex_lock(&p_lib->mtx);
    if (p_lib->tid == gettid())
        ret = E_ERR_PTS_CLI_REJECTED;
    pthread_mutex_unlock(&p_lib->mtx);

    return ret;
}
