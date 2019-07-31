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
