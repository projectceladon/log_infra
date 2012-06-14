/* Android Modem Traces and Logs
*
* Copyright (C) Intel 2012
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
*/

package com.intel.amtl;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class NotificationReceiver extends BroadcastReceiver {

    private static final String MODULE = "NotificationReceiver";

    private static final String bootCompleted = "android.intent.action.BOOT_COMPLETED";
    private static final String shutdown = "android.intent.action.ACTION_SHUTDOWN";
    private static final String poweroff = "android.intent.action.QUICKBOOT_POWEROFF";

    private static final Intent bootCompletedIntent = new Intent("com.intel.amtl.BootService");
    private static final Intent shutdownIntent = new Intent("com.intel.amtl.ShutdownService");

    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(bootCompleted)) {
            Log.i(AmtlCore.TAG, MODULE + ": bootCompletedIntent received");
            context.startService(bootCompletedIntent);
        }
        else if (intent.getAction().equals(shutdown) ||
                 intent.getAction().equals(poweroff)) {
            Log.i(AmtlCore.TAG, MODULE + ": shutdownIntent received");
            context.startService(shutdownIntent);
        }
    }
}
