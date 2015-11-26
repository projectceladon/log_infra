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
