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

package com.intel.crashreport.receivers;

import static com.intel.phonedoctor.PDIntentConstants.INTENT_REPORT_INFO;
import static com.intel.phonedoctor.PDIntentConstants.INTENT_REPORT_ERROR;
import static com.intel.phonedoctor.PDIntentConstants.EXTRA_TYPE;
import static com.intel.phonedoctor.PDIntentConstants.EXTRA_DATA0;
import static com.intel.phonedoctor.PDIntentConstants.EXTRA_DATA1;
import static com.intel.phonedoctor.PDIntentConstants.EXTRA_DATA2;
import static com.intel.phonedoctor.PDIntentConstants.EXTRA_DATA3;
import static com.intel.phonedoctor.PDIntentConstants.EXTRA_DATA4;
import static com.intel.phonedoctor.PDIntentConstants.EXTRA_DATA5;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.intel.crashreport.CustomizableEventData;
import com.intel.crashreport.EventGenerator;

/**
 * @brief InfoErrorEventReceiver waits for Info/Error event from other applications
 *
 * Info/Error events are send through intents which are defined in
 * {@see PDIntentConstants}. It manage to create the equivalent
 * Info/Error event with {@see EventGenerator}.
 */
public class InfoErrorEventReceiver extends BroadcastReceiver {
    private static final String TAG = "PhoneDoctor";
    private static final String Module = "InfoErrorEventReceiver: ";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, Module + "onReceive: " + intent.toString());
        processIntent(context, intent);
    }

    private void processIntent(Context context, Intent intent) {
        CustomizableEventData mEvent;
        String mExtra;
        if (intent.getAction().equals(INTENT_REPORT_INFO)) {
            mEvent = EventGenerator.INSTANCE.getEmptyInfoEvent();
        } else if (intent.getAction().equals(INTENT_REPORT_ERROR)) {
            mEvent = EventGenerator.INSTANCE.getEmptyErrorEvent();
        } else {
            Log.w(TAG, Module + "Intent not regonized: " + intent.getAction() +
                  " , Event generation aborted");
            return;
        }
        mExtra = intent.getStringExtra(EXTRA_TYPE);
        if (mExtra != null) {
            mEvent.setType(mExtra);
        } else {
            Log.w(TAG, Module + "Event Type (EXTRA_TYPE) not provided, Event generation aborted");
            return;
        }
        mExtra = intent.getStringExtra(EXTRA_DATA0);
        if (mExtra != null)
            mEvent.setData0(mExtra);
        mExtra = intent.getStringExtra(EXTRA_DATA1);
        if (mExtra != null)
            mEvent.setData1(mExtra);
        mExtra = intent.getStringExtra(EXTRA_DATA2);
        if (mExtra != null)
            mEvent.setData2(mExtra);
        mExtra = intent.getStringExtra(EXTRA_DATA3);
        if (mExtra != null)
            mEvent.setData3(mExtra);
        mExtra = intent.getStringExtra(EXTRA_DATA4);
        if (mExtra != null)
            mEvent.setData4(mExtra);
        mExtra = intent.getStringExtra(EXTRA_DATA5);
        if (mExtra != null)
            mEvent.setData5(mExtra);
        if (mEvent != null)
            EventGenerator.INSTANCE.generateEvent(mEvent);
    }

}
