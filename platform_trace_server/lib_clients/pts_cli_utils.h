/* Platform Trace Server library - utils header file
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

#ifndef __PTS_CLI_UTILS_H__
#define __PTS_CLI_UTILS_H__

#define DEF_PTS_RESPONSIVE_TIMEOUT 5 /* in second */

typedef enum e_send_method {
    E_SEND_PRE_CONNECTION,
    E_SEND_POST_CONNECTION,
} e_send_method_t;

e_err_pts_cli_t check_state(pts_cli_handle_t *handle,
        pts_lib_context_t **p_lib,
        bool connected);

e_err_pts_cli_t check_mandatory_parameters(pts_lib_context_t *p_lib);

e_err_pts_cli_t send_msg(pts_lib_context_t *p_lib,
        const pts_cli_requests_t *request,
        e_send_method_t method,
        int timeout);

e_err_pts_cli_t read_events(pts_lib_context_t *p_lib);

e_err_pts_cli_t cli_connect(pts_lib_context_t *p_lib);

e_err_pts_cli_t cli_disconnect(pts_lib_context_t *p_lib);

e_pts_events_t is_request_rejected(pts_lib_context_t *p_lib);

#endif                          /* __PTS_CLI_UTILS_H__ */
