/* Platform Trace Server - client library external include file
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

#ifndef __PTS_CLI__
#define __PTS_CLI__

#ifdef __cplusplus
extern "C" {
#endif

#include <sys/types.h>
#include "pts.h"

typedef enum e_err_pts_cli {
    E_ERR_PTS_CLI_SUCCEED,
    E_ERR_PTS_CLI_FAILED,
    E_ERR_PTS_CLI_BAD_HANDLE,
    E_ERR_PTS_CLI_TIMEOUT,
    E_ERR_PTS_CLI_REJECTED,
    E_ERR_PTS_CLI_BAD_CNX_STATE
} e_err_pts_cli_t;

typedef struct pts_cli_event {
    e_pts_events_t id;
    size_t len;
    void *data;
    void *context;
} pts_cli_event_t;

typedef struct pts_cli_requests {
    e_pts_requests_t id;
    size_t len;
    void *data;
} pts_cli_requests_t;

typedef int (*pts_event_handler) (pts_cli_event_t *);

typedef void *pts_cli_handle_t;

typedef enum e_cfg_trace_param_type {
    E_PTS_PARAM_INPUT_PATH_TRACE,
    E_PTS_PARAM_OUTPUT_PATH_TRACE,
    E_PTS_PARAM_ROTATE_SIZE,
    E_PTS_PARAM_ROTATE_NUM,
    E_PTS_PARAM_TRACE_MODE,
    E_PTS_NUM_PARAM
} e_cfg_trace_param_type_t;

typedef enum e_trace_mode {
    E_TRACE_MODE_NONE = 0,
    E_TRACE_MODE_FILE_SYSTEM = 0x01 << 0, /* Traces routed to File System */
    E_TRACE_MODE_LINE_DISCS = 0x01 << 1   /* Traces routed via ldiscs (PTI, USB CDC/ACM, ...) */
} e_trace_mode_t;

/**
 * Create PTS client library handle. This function shall be called first.
 * To avoid memory leaks *handle must be set to NULL by the caller.
 *
 * @param [out] handle library handle
 * @param [in] client_name name of the client
 * @param [in] context pointer to a struct that shall be passed to function
 *             context handle can be NULL if unused.
 *
 * @return E_ERR_PTS_CLI_FAILED if client_name is NULL or handle creation failed
 * @return E_ERR_PTS_CLI_BAD_HANDLE if handle is already created
 * @return E_ERR_PTS_CLI_SUCCEED
 */
e_err_pts_cli_t pts_cli_create_handle(pts_cli_handle_t **handle,
        const char *client_name,
        void *context);

/**
 * Delete PTS client library handle
 *
 * @param [in, out] handle library handle
 *
 * @return E_ERR_PTS_CLI_BAD_HANDLE if handle is invalid or handle already
 *         deleted
 * @return E_ERR_PTS_CLI_BAD_CNX_STATE if client is connected
 * @return E_ERR_PTS_CLI_SUCCEED
 */
e_err_pts_cli_t pts_cli_delete_handle(pts_cli_handle_t *handle);

/**
 * Subscribe to an event. This function shall only be invoked on a valid
 * unconnected handle. Clients must not block the callback. Callback must
 * be used only for a short processing time, otherwise we do not guarantee
 * the responsiveness of the library and events should be received with
 * delay. Also, in the callback function, the client should send no message
 * to the PTS. This has to be done in another thread/context.
 * NB: users can't subscribe to ACK or NACK events.
 *
 * @param [in,out] handle library handle
 * @param [in] func function pointer to the handle
 * @param [in] id event to subscribe to
 *
 * @return E_ERR_PTS_CLI_BAD_HANDLE if handle is invalid
 * @return E_ERR_PTS_CLI_BAD_CNX_STATE if connected
 * @return E_ERR_PTS_CLI_FAILED event already configured or func is NULL or
 *         unknown/invalid event
 * @return E_ERR_PTS_CLI_SUCCEED
 */
e_err_pts_cli_t pts_cli_subscribe_event(pts_cli_handle_t *handle,
        pts_event_handler func,
        e_pts_events_t id);

/**
 * Unsubscribe to an event. This function shall only be invoked on a valid
 * unconnected handle.
 * NB: users can't subscribe to ACK or NACK events.
 *
 * @param [in, out] handle library handle
 * @param [in] id event to unsubscribe to
 *
 * @return E_ERR_PTS_CLI_BAD_HANDLE if handle is invalid
 * @return E_ERR_PTS_CLI_BAD_CNX_STATE if connected
 * @return E_ERR_PTS_CLI_FAILED unknown/invalid event
 * @return E_ERR_PTS_CLI_SUCCEED
 */
e_err_pts_cli_t pts_cli_unsubscribe_event(pts_cli_handle_t *handle,
        e_pts_events_t ev);

/**
 * Connect a previously not connected client to PTS.
 * This function returns when the connection is achieved successfully,
 * or fail on error, or fail after a timeout of 20s.
 * This function shall only be invoked on a valid unconnected handle.
 *
 * @param [in] handle library handle
 *
 * @return E_ERR_PTS_CLI_BAD_HANDLE if handle is invalid
 * @return E_ERR_PTS_CLI_BAD_CNX_STATE if connected
 * @return E_ERR_PTS_CLI_TIMEOUT if PTS is not responsive or after a timeout of 5s
 * @return E_ERR_PTS_CLI_FAILED internal error
 * @return E_ERR_PTS_CLI_SUCCEED
 */
e_err_pts_cli_t pts_cli_send_configuration(pts_cli_handle_t *handle);

/**
 * Disconnect from PTS. If a lock is set, the unlock is done automatically.
 *
 * @param [in] handle library handle
 *
 * @return E_ERR_PTS_CLI_BAD_HANDLE if handle is invalid
 * @return E_ERR_PTS_CLI_BAD_CNX_STATE if already disconnected
 * @return E_ERR_PTS_CLI_FAILED internal error
 * @return E_ERR_PTS_CLI_SUCCEED
 */
e_err_pts_cli_t pts_cli_remove_configuration(pts_cli_handle_t *handle);

/**
 * Send a request to PTS. This function returns when request is parsed and
 * queued by PTS (but not processed yet). It could return an error if PTS
 * is not responsive or after a timeout of 20s.
 *
 * @param [in] handle library handle
 * @param [in] request request to send to PTS
 *
 * @return E_ERR_PTS_CLI_BAD_HANDLE if handle is invalid
 * @return E_ERR_PTS_CLI_TIMEOUT if PTS is not responsive or after a timeout of 5s
 * @return E_ERR_PTS_CLI_REJECTED if this function is called under the client callback
 * @return E_ERR_PTS_CLI_BAD_CNX_STATE if not connected
 * @return E_ERR_PTS_CLI_FAILED internal error
 * @return E_ERR_PTS_CLI_FAILED if request is NULL or invalid request id
 * @return E_ERR_PTS_CLI_SUCCEED
 *
 * IMPORTANT REMARK: This interface shall not be called under client's callback.
 * Client's callback must be used only for a short processing time, otherwise responsiveness
 * is not guaranteed.
 * A mechanism is set to avoid the request sending under callback. Otherwise a deadlock
 * happens when a client tries to send a message under its callback.
 */
e_err_pts_cli_t pts_cli_send_request(pts_cli_handle_t *handle,
        const pts_cli_requests_t *request);

/**
 * Configure trace configuration parameters. This function shall only be invoked on a valid
 * unconnected handle.
 *
 * @param [in] handle library handle
 * @param [in] param_type Parameter type
 * @param [in] pv_param Address of parameter value
 *
 * @return E_ERR_PTS_CLI_BAD_HANDLE if handle is invalid
 * @return E_ERR_PTS_CLI_BAD_CNX_STATE if connected
 * @return E_ERR_PTS_CLI_SUCCEED
 */
e_err_pts_cli_t pts_cli_set_trace_parameters(pts_cli_handle_t *handle,
        const e_cfg_trace_param_type_t param_type,
        const void *pv_param);

/**
 * Example:
 *
 *   pts_create_handle
 *       pts_subscribe_event(E1)
 *       pts_subscribe_event(E2)
 *       pts_subscribe_event(E3)
 *           pts_cli_send_configuration (Listen E1, E2 and E3)
 *           pts_cli_remove_configuration (Stop listening)
 *       pts_unsubscribe_event(E3)
 *           pts_cli_send_configuration (Listen E1 and E2)
 *           pts_cli_remove_configuration (Stop listening)
 *   pts_delete_handle
 */

#ifdef __cplusplus
}
#endif

#endif                          /* __PTS_CLI__ */
