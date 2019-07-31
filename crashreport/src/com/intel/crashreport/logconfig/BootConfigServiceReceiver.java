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

package com.intel.crashreport.logconfig;

import com.intel.crashreport.CrashReport;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import android.os.UserHandle;

public class BootConfigServiceReceiver extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.contentEquals(intent.getAction())) {
            CrashReport app = (CrashReport)(context.getApplicationContext());
            if(!app.isUserBuild())
                context.startServiceAsUser(new Intent(context, BootConfigService.class), UserHandle.CURRENT);
        }
    }

}
