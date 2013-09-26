/* Platform Trace Server - client list header file
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

#ifndef __PTS_CLIENT_HEADER__
#define __PTS_CLIENT_HEADER__

#include <stdbool.h>
#include <time.h>

#include "data_to_msg.h"
#include "errors.h"
#include "pts.h"

#define PTS_MAX_CLIENT 5

typedef e_pts_errors_t (*set_msg) (msg_t *, pts_cli_event_t *);

typedef enum e_cnx_status {
    E_CNX_NONE = 0,
    E_CNX_NAME = 0x01 << 0,
    E_CNX_FILTER = 0x01 << 1,
    E_CNX_INPUT = 0x01 << 2,
    E_CNX_OUTPUT = 0x01 << 3,
    E_CNX_TRACE_MODE = 0x01 << 4,
    E_CNX_TRACE_READY = 0x01 << 5
} e_cnx_status_t;

typedef struct client {
    char name[CLIENT_NAME_LEN + 1];
    int fd;
    struct timespec time;
    e_pts_requests_t request;
    uint32_t subscription;
    e_cnx_status_t cnx;
    set_msg *set_data;
    e_trace_mode_t trace_mode;
    char input_path[FILE_NAME_LEN + 1];
    char output_path[FILE_NAME_LEN + 1];
    uint32_t rotate_size;
    uint32_t rotate_num;
    pthread_t thr_id;
    int fd_pipe[2];
    int epollfd;
    int outfd;
} client_t;

typedef struct client_list {
    int list_size;                      /* Number of maximum clients */
    int connected;                      /* Number of connected clients */
    client_t *list;                     /* Client list */
    set_msg set_data[E_PTS_NUM_EVENTS]; /* Array of event callbacks */
} client_list_t;

e_pts_errors_t is_registered(client_t *client, bool *state);
e_pts_errors_t initialize_list(client_list_t *clients, int list_size);
e_pts_errors_t add_client(client_list_t *clients, int fd, client_t **pp_client);
e_pts_errors_t remove_client(client_list_t *clients, client_t *client);
e_pts_errors_t set_client_name(client_t *client, char *name, size_t len);
e_pts_errors_t set_client_input_path(client_t *client, char *name, size_t len);
e_pts_errors_t set_client_output_path(client_t *client, char *name, size_t len);
e_pts_errors_t set_client_rotate_size(client_t *client, uint32_t rotate_size);
e_pts_errors_t set_client_rotate_num(client_t *client, uint32_t rotate_num);
e_pts_errors_t set_client_trace_mode(client_t *client, e_trace_mode_t trace_mode);
e_pts_errors_t set_client_signal_trace_start(client_t *client);
e_pts_errors_t set_client_signal_trace_stop(client_t *client);
e_pts_errors_t set_client_filter(client_t *client, uint32_t subscription);
e_pts_errors_t find_client(client_list_t *clients, int fd, client_t **pp_client);
e_pts_errors_t inform_all_clients(client_list_t *clients, e_pts_events_t state, void *data);
e_pts_errors_t inform_client(client_t *client, e_pts_events_t state, void *data);
e_pts_errors_t close_all_clients(client_list_t *clients);

#endif                          /* __PTS_CLIENT_HEADER__ */
