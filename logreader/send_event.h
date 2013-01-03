/* Copyright (C) Intel 2013
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * @file send_event.h
 * @brief send_event provide an API to send event to crashtool server
 *
 * Available events are INFO, ERROR and STATS.
 */

#include "send_intent.h"

/**
 * Define the list of available intent elements, containing actions
 * and extras. Only actions are used in exported API as EXTRAS are
 * passed explicitely in send_event function arguments.
 */
enum {
    I_ACTION_INFO,
    I_ACTION_ERROR,
    I_ACTION_STATS,
    I_EXTRA_TYPE,
    I_EXTRA_DATA0,
    I_EXTRA_DATA1,
    I_EXTRA_DATA2,
    I_EXTRA_DATA3,
    I_EXTRA_DATA4,
    I_EXTRA_DATA5,
    I_EXTRA_FILE,
    I_COUNT
};

/**
 * Send event to crashtool server.
 *
 * Event are provided as action, could be I_ACTION_INFO,
 * I_ACTION_ERROR, I_ACTION_STATS. Type is mandatory, other arguments
 * not, NULL can be passed.
 *
 * @param action of the event, value are from the enum, mandatory.
 * @param type of the event, mandatory.
 * @param data0..5 data fields of the event, could be NULL.
 * @param file path of the attached file, could be NULL.
 */
void send_event(unsigned int action,
                const char* type,
                const char* data0,
                const char* data1,
                const char* data2,
                const char* data3,
                const char* data4,
                const char* data5,
                const char* file);

