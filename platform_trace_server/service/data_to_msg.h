/* Modem Manager - data to message header file
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

#ifndef __PTS_DATA_TO_MSG_HEADER__
#define __PTS_DATA_TO_MSG_HEADER__

#include "errors.h"
#include "pts_cli.h"
#include "client_cnx.h"

e_pts_errors_t delete_msg(msg_t *msg);

/* used by lib client: */
e_pts_errors_t set_msg_name(msg_t *msg, pts_cli_event_t *request);
e_pts_errors_t set_msg_filter(msg_t *msg, pts_cli_event_t *request);
e_pts_errors_t set_msg_path_cfg(msg_t *msg, pts_cli_event_t *request);
e_pts_errors_t set_msg_rotate_cfg(msg_t *msg, pts_cli_event_t *request);
e_pts_errors_t set_msg_trace_mode(msg_t *msg, pts_cli_event_t *request);
e_pts_errors_t set_msg_empty(msg_t *msg, pts_cli_event_t *request);
/* used by PTS: */

#endif                          /* __PTS_DATA_TO_MSG_HEADER__ */
