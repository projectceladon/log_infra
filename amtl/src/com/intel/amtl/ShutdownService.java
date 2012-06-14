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

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import android.os.SystemProperties;

import java.io.IOException;
import java.lang.RuntimeException;

public class ShutdownService extends Service {

    private static final String MODULE = "ShutdownService";

    private AmtlCore core;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        /* Get application core */
        try {
            core = AmtlCore.get();
            this.core.setContext(this.getApplicationContext());
        }
        catch (AmtlCoreException e) {
            Log.e(AmtlCore.TAG, MODULE + ": failed to get Amtl core");
            this.stopSelf();
        }
    }

    @Override
    public void onStart(Intent intent, int startId) {
        /* If configuration has changed, tells AmtlCore to apply the new configuration */
        if (this.core.rebootNeeded()) {
            Log.i(AmtlCore.TAG, MODULE + ": apply new configuration");
            try {
                this.core.applyCfg();
                Toast toast = Toast.makeText(ShutdownService.this, "New Amtl configuration applied", Toast.LENGTH_LONG);
                toast.show();
            }
            catch (AmtlCoreException e) {
                Toast toast = Toast.makeText(ShutdownService.this, e.getMessage(), Toast.LENGTH_LONG);
                toast.show();
            }
        }
    }
}
