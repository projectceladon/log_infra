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
 * @file logreader.h
 * @brief logreader module generate crashtool events from log events
 *
 * This module scans events log buffer (/dev/log/events) in order to
 * extract defined events, specified by predefined tags in
 * g_eventTagRangeMap. Once extracted, it generates an appropriate
 * Crashtool Info/Error event. It is intended to be integrated in
 * crashlogd.
 *
 * Event matching is :
 *  Log            | Crashtool
 *  --------------------------
 *  EventType      | event
 *  Group (name)   | type
 *  TAG            | data0
 *  Message (data) | data1
 *
 * @see g_eventTagRangeMap
 */

/**
 * Define Events to match with crashtool events
 */
typedef enum {
    EVENT_INFO,
    EVENT_ERROR,
} EventType;

/**
 * EventTagRange to match tags with crashtool events.
 *
 * Represent a range of tags in the events logger buffer which are
 * associated with a name (or group), and an event @see EventType.
 */
typedef struct {
    const char* group; /**< FT or module name, reported as crashtool event type */
    EventType event; /**< The matching crashtool event */
    unsigned int rangeStart; /**< starting range value, inclusive */
    unsigned int rangeEnd; /**< ending range value, inclusive */
} EventTagRange;

/**
 * Map which represent groups event tag range for Info/Error events
 *
 * According to system/core/logcat/event.logtags file, range
 * 1000000-2000000 could be used to our needs without conflicting with
 * the core platform tags.
 *
 * Range values are inclusive.
 */
static EventTagRange g_eventTagRangeMap[] = {
    {"DnT", EVENT_INFO, 1010000, 1010199},
    {"DnT", EVENT_ERROR, 1020000, 1020199},
    {NULL, 0, 0, 0} /**< Terminating value */
};

/**
 * init function to setup the module.
 *
 * Opens the device, setup the communication FD as defined by
 * crashlogd source API, and opens the log events tag map.
 */
void logreader_init();

/**
 * Exit function to close the module.
 */
void logreader_exit();

/**
 * Return the communication FD.
 *
 * It allow to know if data is ready on the line with a select/poll
 * method.
 */
int logreader_get_fd();

/**
 * Processing function, parse logs and send event if event log is in a
 * defined range.
 */
void logreader_handle();
