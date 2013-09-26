/* Platform Trace Server - events manager header file
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

#ifndef __PTS_EVENTS_MANAGER_HEADER__
#define __PTS_EVENTS_MANAGER_HEADER__


e_pts_errors_t events_manager(pts_data_t *pts);
e_pts_errors_t events_cleanup(pts_data_t *pts);
e_pts_errors_t events_init(pts_data_t *pts);
inline void set_pts_state(pts_data_t *pts, e_pts_state_t state);

#endif                          /* __PTS_EVENTS_MANAGER_HEADER__ */
