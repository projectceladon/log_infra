/* Platform Trace Server client library - interface source file
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
#include <pthread.h>
#include <stdbool.h>
#include <errno.h>

#include "pts_cli_common.h"
#include "pts_cli_utils.h"

#define DEFAULT_TID 1

#define CHECK_EVENT(id, err, out) do { \
        if (id >= E_PTS_NUM_EVENTS) { \
            LOG_ERROR("unknown event"); \
            ret = E_ERR_PTS_CLI_FAILED; \
            goto out; \
        } \
} while (0)

/**
 * @see pts_cli.h
 */
e_err_pts_cli_t pts_cli_send_request(pts_cli_handle_t *handle,
        const pts_cli_requests_t *request)
{
    e_err_pts_cli_t ret = E_ERR_PTS_CLI_FAILED;
    pts_lib_context_t *p_lib = NULL;

    ret = check_state(handle, &p_lib, true);
    if (ret != E_ERR_PTS_CLI_SUCCEED) {
        LOG_ERROR("request not sent");
    } else {
        /* Check if request is accepted or not */
        ret = is_request_rejected(p_lib);
        if (ret != E_ERR_PTS_CLI_REJECTED) {
            ret = send_msg(p_lib, request, E_SEND_POST_CONNECTION, DEF_PTS_RESPONSIVE_TIMEOUT);
        } else {
            LOG_ERROR("(fd=%d client=%s) request shall not be sent under client's callback",
                    p_lib->fd_socket, p_lib->cli_name);
        }
    }

    return ret;
}

/**
 * @see pts_cli.h
 */
e_err_pts_cli_t pts_cli_create_handle(pts_cli_handle_t **handle,
        const char *client_name, void *context)
{
    e_err_pts_cli_t ret = E_ERR_PTS_CLI_SUCCEED;
    pts_lib_context_t *p_lib = NULL;
    int i;

    CHECK_CLI_PARAM(handle, ret, out);

    if (*handle != NULL) {
        LOG_ERROR("*handle is not NULL");
        ret = E_ERR_PTS_CLI_BAD_HANDLE;
        goto out;
    }

    if (client_name == NULL) {
        LOG_ERROR("client_name is NULL");
        ret = E_ERR_PTS_CLI_FAILED;
        goto out;
    }

    p_lib = (pts_lib_context_t *)malloc(sizeof(pts_lib_context_t));
    if (p_lib == NULL) {
        LOG_ERROR("failed to allocate");
        ret = E_ERR_PTS_CLI_FAILED;
        goto out;
    }

    memset(p_lib, 0, sizeof(pts_lib_context_t));

    pthread_mutex_init(&p_lib->mtx, NULL);
    pthread_mutex_init(&p_lib->mtx_signal, NULL);
    pthread_cond_init(&p_lib->cond, NULL);
    /* Requests sent by a client are ALWAYS acknowledged by PTS */
    p_lib->events = (0x1 << E_PTS_ACK) | (0x1 << E_PTS_NACK);
    p_lib->cli_ctx = context;
    p_lib->fd_socket = CLOSED_FD;
    p_lib->fd_pipe[READ] = CLOSED_FD;
    p_lib->fd_pipe[WRITE] = CLOSED_FD;
    p_lib->connected = E_CNX_DISCONNECTED;
    p_lib->thr_id = -1;
    p_lib->tid = DEFAULT_TID;
    strncpy(p_lib->cli_name, client_name, CLIENT_NAME_LEN - 1);
#if DEBUG_PTS_CLI
    p_lib->init = INIT_CHECK;
#endif

    /* Set default trace configuration */
    p_lib->trace_mode = E_TRACE_MODE_NONE;
    p_lib->rotate_size = CLIENT_ROTATE_SIZE;
    p_lib->rotate_num = CLIENT_ROTATE_NUM;
    /* Input and output paths are set to NULL thanks to memset */

    for (i = 0; i < E_PTS_NUM_REQUESTS; i++)
        p_lib->set_msg[i] = set_msg_empty;

    for (i = 0; i < E_PTS_NUM_EVENTS; i++) {
        p_lib->set_data[i] = set_data_empty;
        p_lib->free_data[i] = free_data_empty;
    }

    for (i = 0; i < E_PTS_NUM_EVENTS; i++)
        p_lib->func[i] = NULL;

    /* Set specific request handler */
    p_lib->set_msg[E_PTS_SET_NAME] = set_msg_name;
    p_lib->set_msg[E_PTS_SET_EVENTS] = set_msg_filter;
    p_lib->set_msg[E_PTS_SET_INPUT_PATH_TRACE_CFG] = set_msg_path_cfg;
    p_lib->set_msg[E_PTS_SET_OUTPUT_PATH_TRACE_CFG] = set_msg_path_cfg;
    p_lib->set_msg[E_PTS_SET_ROTATE_SIZE_TRACE_CFG] = set_msg_rotate_cfg;
    p_lib->set_msg[E_PTS_SET_ROTATE_NUM_TRACE_CFG] = set_msg_rotate_cfg;
    p_lib->set_msg[E_PTS_SET_TRACE_MODE] = set_msg_trace_mode;

    *handle = (pts_cli_handle_t *)p_lib;
    LOG_DEBUG("handle created successfully");

    out:
    return ret;
}

/**
 * @see pts_cli.h
 */
e_err_pts_cli_t pts_cli_delete_handle(pts_cli_handle_t *handle)
{
    e_err_pts_cli_t ret = E_ERR_PTS_CLI_SUCCEED;
    pts_lib_context_t *p_lib = NULL;

    CHECK_CLI_PARAM(handle, ret, out);

    ret = check_state(handle, &p_lib, false);
    if (ret == E_ERR_PTS_CLI_SUCCEED) {
        if (p_lib != NULL) {
            free(p_lib);
            p_lib = NULL;
            LOG_DEBUG("handle freed successfully");
        }
    } else {
        LOG_ERROR("handle not freed");
    }

    out:
    return ret;
}

/**
 * @see pts_cli.h
 */
e_err_pts_cli_t pts_cli_subscribe_event(pts_cli_handle_t *handle,
        pts_event_handler func,
        e_pts_events_t id)
{
    e_err_pts_cli_t ret = E_ERR_PTS_CLI_SUCCEED;
    pts_lib_context_t *p_lib = NULL;

    ret = check_state(handle, &p_lib, false);
    if (ret != E_ERR_PTS_CLI_SUCCEED) {
        LOG_ERROR("To subscribe to an event, you should provide a valid handle"
                " and be disconnected");
        goto out;
    }

    CHECK_EVENT(id, ret, out);
    if ((id == E_PTS_ACK) || (id == E_PTS_NACK)) {
        /* E_PTS_ACK and E_PTS_NACK are automatically subscribed by default */
        ret = E_ERR_PTS_CLI_FAILED;
        goto out;
    }

    if (func == NULL) {
        LOG_ERROR("function is NULL");
        ret = E_ERR_PTS_CLI_FAILED;
        goto out;
    }

    pthread_mutex_lock(&p_lib->mtx);
    if (p_lib->func[id] != NULL) {
        ret = E_ERR_PTS_CLI_FAILED;
    } else {
        p_lib->events |= (0x01 << id);
        p_lib->func[id] = func;
    }
    pthread_mutex_unlock(&p_lib->mtx);

    if (ret == E_ERR_PTS_CLI_SUCCEED) {
        LOG_DEBUG("(fd=%d client=%s) event (%s) subscribed successfully",
                p_lib->fd_socket, p_lib->cli_name, g_pts_events[id]);
    } else {
        LOG_ERROR("(fd=%d client=%s) event (%s) already configured",
                p_lib->fd_socket, p_lib->cli_name, g_pts_events[id]);
    }

    out:
    return ret;
}

/**
 * @see pts_cli.h
 */
e_err_pts_cli_t pts_cli_unsubscribe_event(pts_cli_handle_t *handle,
        e_pts_events_t id)
{
    e_err_pts_cli_t ret = E_ERR_PTS_CLI_SUCCEED;
    pts_lib_context_t *p_lib = NULL;

    ret = check_state(handle, &p_lib, false);
    if (ret != E_ERR_PTS_CLI_SUCCEED) {
        LOG_ERROR("To subscribe to an event, you should be disconnected");
        goto out;
    }

    CHECK_EVENT(id, ret, out);
    if ((id == E_PTS_ACK) || (id == E_PTS_NACK)) {
        /* E_PTS_ACK and E_PTS_NACK cannot be unsubscribed */
        ret = E_ERR_PTS_CLI_FAILED;
        goto out;
    }

    pthread_mutex_lock(&p_lib->mtx);
    p_lib->events &= ~(0x01 << id);
    p_lib->func[id] = NULL;
    pthread_mutex_unlock(&p_lib->mtx);

    LOG_DEBUG("(fd=%d client=%s) event (%s) unsubscribed successfully",
            p_lib->fd_socket, p_lib->cli_name, g_pts_events[id]);

    out:
    return ret;
}

/**
 * @see pts_cli.h
 */
e_err_pts_cli_t pts_cli_send_configuration(pts_cli_handle_t *handle)
{
    e_err_pts_cli_t ret;
    pts_lib_context_t *p_lib = NULL;
    int err = 0;

    ret = check_state(handle, &p_lib, false);
    if (ret != E_ERR_PTS_CLI_SUCCEED)
        goto out;

    /* Check if required trace parameters are set */
    ret = check_mandatory_parameters(p_lib);
    if (ret != E_ERR_PTS_CLI_SUCCEED)
        goto out;

    /* Pipe creation for communication with read_events thread */
    if (pipe(p_lib->fd_pipe) < 0) {
        LOG_ERROR("(client=%s) failed to create pipe (%s)", p_lib->cli_name, strerror(errno));
        ret = E_ERR_PTS_CLI_FAILED;
    } else {
        ret = cli_connect(p_lib);
    }

    if (ret == E_ERR_PTS_CLI_SUCCEED) {
        err = pthread_create(&p_lib->thr_id, NULL, (void *)read_events, p_lib);
        if (err != 0) {
            LOG_ERROR("(fd=%d client=%s) failed to create the reader thread. "
                    "Disconnect the client", p_lib->fd_socket,
                    p_lib->cli_name);
            ret = E_ERR_PTS_CLI_FAILED;
        } else {
            pthread_mutex_lock(&p_lib->mtx);
            p_lib->connected = E_CNX_CONNECTED;
            pthread_mutex_unlock(&p_lib->mtx);
        }
    }

    out:
    return ret;
}

/**
 * @see pts_cli.h
 */
e_err_pts_cli_t pts_cli_remove_configuration(pts_cli_handle_t *handle)
{
    e_err_pts_cli_t ret = E_ERR_PTS_CLI_FAILED;
    pts_lib_context_t *p_lib = NULL;

    ret = check_state(handle, &p_lib, true);
    if (ret == E_ERR_PTS_CLI_SUCCEED)
        ret = cli_disconnect(p_lib);

    return ret;
}

/**
 * @see pts_cli.h
 */
e_err_pts_cli_t pts_cli_set_trace_parameters(pts_cli_handle_t *handle,
        const e_cfg_trace_param_type_t param_type,
        const void *pv_param) {

    e_err_pts_cli_t ret = E_ERR_PTS_CLI_SUCCEED;
    pts_lib_context_t *p_lib = NULL;

    ret = check_state(handle, &p_lib, false);
    if (ret != E_ERR_PTS_CLI_SUCCEED)
        goto out;

    if (pv_param == NULL) {
        LOG_ERROR("parameter is NULL");
        ret = E_ERR_PTS_CLI_FAILED;
        goto out;
    }

    pthread_mutex_lock(&p_lib->mtx);
    switch (param_type) {
    case E_PTS_PARAM_INPUT_PATH_TRACE:
        /* Check output path trace parameter length validity */
        if (strlen(pv_param) > FILE_NAME_LEN) {
            LOG_ERROR("(fd=%d client=%s) Input path too long (max length %d)",
                    p_lib->fd_socket,
                    p_lib->cli_name, FILE_NAME_LEN);
            ret = E_ERR_PTS_CLI_FAILED;
        } else {
            /* Set input path trace parameter */
            strncpy(p_lib->input_path, pv_param, FILE_NAME_LEN);
        }
        break;

    case E_PTS_PARAM_OUTPUT_PATH_TRACE:
        /* Check output path trace parameter length validity */
        if (strlen(pv_param) > FILE_NAME_LEN) {
            LOG_ERROR("(fd=%d client=%s) Output path too long (max length %d)",
                    p_lib->fd_socket,
                    p_lib->cli_name, FILE_NAME_LEN);
            ret = E_ERR_PTS_CLI_FAILED;
        } else {
            /* Set output path trace parameter */
            strncpy(p_lib->output_path, pv_param, FILE_NAME_LEN);
        }
        break;

    case E_PTS_PARAM_ROTATE_SIZE:
        /* Set rotate size file trace parameter */
        p_lib->rotate_size = (uint32_t)(*(uint32_t *)pv_param);
        break;

    case E_PTS_PARAM_ROTATE_NUM:
        /* Set rotate number file trace parameter */
        p_lib->rotate_num = (uint32_t)(*(uint32_t *)pv_param);
        break;

    case E_PTS_PARAM_TRACE_MODE:
        /* Check trace mode parameter value */
        if ((*(e_trace_mode_t *)pv_param == E_TRACE_MODE_FILE_SYSTEM) ||
                (*(e_trace_mode_t *)pv_param == E_TRACE_MODE_LINE_DISCS)) {
            /* Set trace mode parameter value */
            p_lib->trace_mode = (e_trace_mode_t)(*(e_trace_mode_t *)pv_param);
        }  else {
             LOG_ERROR("(fd=%d client=%s) Wrong trace mode value",
                    p_lib->fd_socket, p_lib->cli_name);
             ret = E_ERR_PTS_CLI_FAILED;
        }
        break;

    default:
        LOG_ERROR("(fd=%d client=%s) Parameter type unknown",
                p_lib->fd_socket, p_lib->cli_name);
        ret = E_ERR_PTS_CLI_FAILED;
        break;
    }
    pthread_mutex_unlock(&p_lib->mtx);

    out:
    return ret;
}
