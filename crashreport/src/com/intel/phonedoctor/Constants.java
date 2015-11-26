/* INTEL CONFIDENTIAL
 * Copyright 2015 Intel Corporation
 *
 * The source code contained or described herein and all documents
 * related to the source code ("Material") are owned by Intel
 * Corporation or its suppliers or licensors. Title to the Material
 * remains with Intel Corporation or its suppliers and
 * licensors. The Material contains trade secrets and proprietary
 * and confidential information of Intel or its suppliers and
 * licensors. The Material is protected by worldwide copyright and
 * trade secret laws and treaty provisions. No part of the Material
 * may be used, copied, reproduced, modified, published, uploaded,
 * posted, transmitted, distributed, or disclosed in any way without
 * Intel's prior express written permission.
 *
 * No license under any patent, copyright, trade secret or other
 * intellectual property right is granted to or conferred upon you
 * by disclosure or delivery of the Materials, either expressly, by
 * implication, inducement, estoppel or otherwise. Any license under
 * such intellectual property rights must be express and approved by
 * Intel in writing.
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
    public static final String LOGS_DIR = SystemProperties.get("persist.crashlogd.root", "/logs");

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
     * Maximum crashlogs size to upload over 3G (10Mo)
     */
    public static final int WIFI_LOGS_SIZE = 10 * 1024 * 1024;

    /**
     * Array defining event type that are automatically defined as invalid
     * to prevent any log upload to crashtool server.
    */
    public static final String[] INVALID_EVENTS = new String [] { "KDUMP" };
}
