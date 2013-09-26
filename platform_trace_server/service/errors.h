/* Platform Trace Server - errors header file
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

#ifndef __PTS_ERRORS_HEADER__
#define __PTS_ERRORS_HEADER__

#include <stdio.h>
#include <stdlib.h>

typedef enum e_pts_errors {
    /* General */
    E_PTS_ERR_BAD_PARAMETER,
    E_PTS_ERR_FAILED,
    E_PTS_ERR_SUCCESS,
    E_PTS_ERR_TIMEOUT,
    /* Client */
    E_PTS_ERR_DISCONNECTED
} e_pts_errors_t;

#define CLOSED_FD -1

#define xstr(s) str(s)
#define str(s) #s

#define CHECK_PARAM(param, err, label) do { \
        if (param == NULL) {                    \
            LOG_DEBUG(xstr(param)" is NULL");   \
            err = E_PTS_ERR_BAD_PARAMETER;      \
            goto label;                         \
        }                                       \
} while(0)

#endif                          /* __PTS_ERRORS_HEADER__ */
