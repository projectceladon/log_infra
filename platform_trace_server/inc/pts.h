/* Platform Trace Server (PTS) - external include file
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

#ifndef __PTS_EXTERNAL_HEADER_FILE__
#define __PTS_EXTERNAL_HEADER_FILE__

#include <sys/types.h>

#define PTS_SOCKET_NAME    "pts"
#define CLIENT_NAME_LEN    64
#define FILE_NAME_LEN      64
#define CLIENT_ROTATE_SIZE 10*(1<<20)
#define CLIENT_ROTATE_NUM  4

/* Please read README file to have useful information about PTS requests */

#define PTS_REQUESTS \
        /* Client properties connection: Clients -> PTS */ \
        X(SET_NAME),\
        X(SET_EVENTS),\
        X(SET_INPUT_PATH_TRACE_CFG),\
        X(SET_OUTPUT_PATH_TRACE_CFG),\
        X(SET_ROTATE_SIZE_TRACE_CFG),\
        X(SET_ROTATE_NUM_TRACE_CFG),\
        X(SET_TRACE_MODE),\
        /* Requests trace activation/deactivation: Clients -> PTS */ \
        X(REQUEST_TRACE_START),\
        X(REQUEST_TRACE_STOP),\
        X(NUM_REQUESTS)

#define PTS_EVENTS \
        /* Events notification: PTS -> Clients */\
        X(EVENT_PTS_DOWN),\
        X(EVENT_PTS_UP),\
        /* Notifications: PTS -> Clients */\
        X(EVENT_FS_TRACE_PARAMS),\
        X(EVENT_TRACE_READY),\
        /* ACK: PTS -> Clients */\
        X(ACK),\
        X(NACK),\
        X(NUM_EVENTS)

typedef enum e_pts_requests {
#undef X
#define X(a) E_PTS_##a
            PTS_REQUESTS
} e_pts_requests_t;

typedef enum e_pts_events {
#undef X
#define X(a) E_PTS_##a
            PTS_EVENTS
} e_pts_events_t;

typedef struct pts_cli_error {
    int id;
    size_t len;
    char *reason;
} pts_cli_error_t;

#endif                          /* __PTS_EXTERNAL_HEADER_FILE__ */
