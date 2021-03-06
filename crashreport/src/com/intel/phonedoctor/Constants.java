/* Copyright (C) 2019 Intel Corporation
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

package com.intel.phonedoctor;

import android.os.SystemProperties;

/**
 * This class provide constants for the application.
 */
public class Constants {

    /**
     * PhoneDoctor log tag
     */
    public static final String TAG = "PhoneDoctor";

    /**
     * Base folder for all logs and DnT specific files
     */
    public static final String LOGS_DIR = SystemProperties.get("persist.vendor.crashlogd.root", "/logs");

    /**
     * Folder used by EventGenerator to store event data folders
     */
    public static final String PD_EVENT_DATA_DIR = LOGS_DIR + "/pd_events";

    /**
     * Folder root used by EventGenerator to store provided files of each event
     *
     * A number is added at the end of the directory name to distinguish each event.
     */
    public static final String PD_EVENT_DATA_DIR_ELEMENT_ROOT = "/event_";

    /**
     * Critical size in bytes for the logs partition
     * */
    public static final int LOGS_CRITICAL_SIZE = 20000000;

    /**
     * Percentage representing the critical size for the logs partition,
     * before launching the cleanup of uploaded logs.
     * */
    public static final int LOGS_CRITICAL_SIZE_STAGE1 = 20;

    /**
     * Percentage representing the critical size for the logs partition,
     * before launching the cleanup of oldest logs.
     * */
    public static final int LOGS_CRITICAL_SIZE_STAGE2 = 10;

    /**
     * crash delay postpone in sec
     */
    public static final int CRASH_POSTPONE_DELAY = 120;

    /**
     * Array defining event type that are automatically defined as invalid
     * to prevent any log upload to crashtool server.
    */
    public static final String[] INVALID_EVENTS = new String [] { "KDUMP" };
}
