/*
 * Copyright (c) Intel 2012
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.intel.phonedoctor;

/**
 * Info/Error intent constants used to send intent to PhoneDoctor in
 * order to report Info/Error events to Crashtool server.
 */
public final class PDIntentConstants {

    /**
     * Intent action to send in order to report an "Info" event to
     * Crashtool. Extras "Type", "Data0", "Data1" and "Data2" form the
     * event signature on Crashtool and should be filled
     * appropriately.
     */
    public static final String INTENT_REPORT_INFO =
        "intel.intent.action.phonedoctor.REPORT_INFO";

    /**
     * Intent action to send in order to report an "Error" event to
     * Crashtool. Extras "Type", "Data0", "Data1" and "Data2" form the
     * event signature on Crashtool and should be filled
     * appropriately.
     */
    public static final String INTENT_REPORT_ERROR =
        "intel.intent.action.phonedoctor.REPORT_ERROR";

    /**
     * Intent action to send in order to report a "Stats" event to
     * Crashtool. Extras "Type", "Data0", "Data1" and "Data2" form the
     * event signature on Crashtool and should be filled
     * appropriately.
     */
    public static final String INTENT_REPORT_STATS =
        "intel.intent.action.phonedoctor.REPORT_STATS";

    /**
     * "Type" of the event. It should be constant across a same
     * component to ease identification of issue from non FT
     * members. Part of the event signature on Crashtool server.
     */
    public static final String EXTRA_TYPE =
        "intel.intent.extra.phonedoctor.TYPE";

    /**
     * "Data0" of the event. Part of the event signature on Crashtool
     * server.
     */
    public static final String EXTRA_DATA0 =
        "intel.intent.extra.phonedoctor.DATA0";

    /**
     * "Data1" of the event. Part of the event signature on Crashtool
     * server.
     */
    public static final String EXTRA_DATA1 =
        "intel.intent.extra.phonedoctor.DATA1";

    /**
     * "Data2" of the event. Part of the event signature on Crashtool
     * server.
     */
    public static final String EXTRA_DATA2 =
        "intel.intent.extra.phonedoctor.DATA2";

    /**
     * "Data3" of the event.
     */
    public static final String EXTRA_DATA3 =
        "intel.intent.extra.phonedoctor.DATA3";

    /**
     * "Data4" of the event.
     */
    public static final String EXTRA_DATA4 =
        "intel.intent.extra.phonedoctor.DATA4";

    /**
     * "Data5" of the event.
     */
    public static final String EXTRA_DATA5 =
        "intel.intent.extra.phonedoctor.DATA5";

    /**
     * File absolute path associated with the event.
     */
    public static final String EXTRA_FILE =
        "intel.intent.extra.phonedoctor.FILE";

}
