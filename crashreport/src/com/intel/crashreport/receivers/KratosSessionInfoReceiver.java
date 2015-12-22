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

package com.intel.crashreport.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.intel.crashreport.CustomizableEventData;
import com.intel.crashreport.GeneralEventGenerator;
import com.intel.crashreport.specific.EventGenerator;

public class KratosSessionInfoReceiver extends BroadcastReceiver {
    private static final String TAG = "KratosSessionInfoReceiver";

    private static final String ACTION_SESSION_INFO = "intel.intent.action.kratos.SESSION_INFO";
    private static final String EXTRA_ART_SERVER_LINK = "intel.intent.extra.kratos.ART_SERVER_LINK";

    private static final String EVENT_TYPE_KRATOS = "KRATOS";
    private static final String EVENT_DATA0_KRATOS_SESSION_UPLOAD = "Session upload";
    private static final String EVENT_LINK_TAG = "link:";
    private static final String EVENT_ERROR_NO_LINK = "No ART session link found";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive: " + intent.toString());
        processIntent(context, intent);
    }

    private void processIntent(Context context, Intent intent) {
        if (ACTION_SESSION_INFO.contentEquals(intent.getAction())) {
            String sessionARTLink = intent.getStringExtra(EXTRA_ART_SERVER_LINK);
            if (sessionARTLink == null) {
                Log.w(TAG, EVENT_ERROR_NO_LINK);
                CustomizableEventData mEvent = EventGenerator.INSTANCE.getEmptyErrorEvent();
                mEvent.setType(EVENT_TYPE_KRATOS);
                mEvent.setData0(EVENT_DATA0_KRATOS_SESSION_UPLOAD);
                mEvent.setData1(EVENT_ERROR_NO_LINK);
                GeneralEventGenerator.INSTANCE.generateEvent(mEvent);
            } else {
                CustomizableEventData mEvent = EventGenerator.INSTANCE.getEmptyInfoEvent();
                mEvent.setType(EVENT_TYPE_KRATOS);
                mEvent.setData0(EVENT_DATA0_KRATOS_SESSION_UPLOAD);
                mEvent.setData1(EVENT_LINK_TAG + sessionARTLink);
                GeneralEventGenerator.INSTANCE.generateEvent(mEvent);
            }
        }
    }

}
