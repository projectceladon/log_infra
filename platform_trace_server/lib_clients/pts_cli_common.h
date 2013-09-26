/* Platform Trace Server client library - common header
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

#ifndef __PTS_CLI_COMMON_H__
#define __PTS_CLI_COMMON_H__

#include <stdio.h>
#include <stdbool.h>

#include "errors.h"
#include "logs.h"
#include "pts_cli.h"
#include "client_cnx.h"
#include "msg_to_data.h"
#include "data_to_msg.h"

#undef LOG_TAG
#define LOG_TAG "PTS_CLI"

typedef e_pts_errors_t (*msg_handler) (msg_t *, pts_cli_event_t *);
typedef e_pts_errors_t (*free_handler) (pts_cli_event_t *);

#define CNX_STATES \
        X(DISCONNECTED), \
        X(CONNECTED)

typedef enum cnx_state {
#undef X
#define X(a) E_CNX_##a
    CNX_STATES
} cnx_state_t;

/**
 * internal structure for pts_cli
 *
 * @private
 */
typedef struct pts_lib_context {
    uint32_t events;
    pthread_t thr_id;
    pthread_mutex_t mtx;
    void *cli_ctx;
    cnx_state_t connected;
    int fd_socket;
    int fd_pipe[2];
    pts_event_handler func[E_PTS_NUM_EVENTS];
    char cli_name[CLIENT_NAME_LEN];
    e_trace_mode_t trace_mode;
    char input_path[FILE_NAME_LEN + 1];
    char output_path[FILE_NAME_LEN + 1];
    uint32_t rotate_size;
    uint32_t rotate_num;
    msg_handler set_msg[E_PTS_NUM_REQUESTS];
    msg_handler set_data[E_PTS_NUM_EVENTS];
    free_handler free_data[E_PTS_NUM_EVENTS];
#ifdef DEBUG_PTS_CLI
    /* the purpose of this variable is to check that this structure has
     * correctly been initialized */
    uint init;
#endif
    /* variables used for sync op: */
    pthread_mutex_t mtx_signal;
    pthread_cond_t cond;
    e_pts_events_t ack;
    pid_t tid;
} pts_lib_context_t;

#ifdef DEBUG_PTS_CLI
#define INIT_CHECK 0xCE5A12BB
#endif

#define CLOSED_FD -1
#define READ 0
#define WRITE 1

#define xstr(s) str(s)
#define str(s) #s

#define CHECK_CLI_PARAM(handle, err, out) do { \
        if (handle == NULL) { \
            LOG_ERROR(xstr(handle)" is NULL"); \
            err = E_ERR_PTS_CLI_BAD_HANDLE; \
            goto out; \
        } \
} while (0)

extern const char *g_pts_events[];

#endif                          /* __PTS_CLI_COMMON_H__ */
