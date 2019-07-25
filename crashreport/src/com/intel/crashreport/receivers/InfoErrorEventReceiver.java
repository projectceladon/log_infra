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

import static com.intel.phonedoctor.Constants.TAG;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserHandle;
import android.util.Log;

/**
 * @brief InfoErrorEventReceiver waits for Info/Error/Stats event from other applications
 *
 * Info/Error/Stats events are send through intents which are defined in
 * {@see PDIntentConstants}. The intent is passed to {@see EventReceiverService}
 * which do the processing and the event creation.
 */
public class InfoErrorEventReceiver extends BroadcastReceiver {
    private static final String Module = "InfoErrorEventReceiver: ";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, Module + "onReceive: " + intent.toString());
        processIntent(context, intent);
    }

    private void processIntent(Context context, Intent intent) {
        if (intent != null) {
            Intent mEventReceiverService = new Intent(context, EventReceiverService.class);
            mEventReceiverService.putExtra(EventReceiverService.EXTRA_ORIGINAL_ACTION, intent.getAction());
            Bundle mExtras = intent.getExtras();
            if (mExtras != null)
                mEventReceiverService.putExtras(mExtras);
            context.startServiceAsUser(mEventReceiverService, UserHandle.CURRENT);
        }
    }

}
