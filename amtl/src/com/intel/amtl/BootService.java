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
import android.os.SystemProperties;
import android.util.Log;

import java.io.IOException;

public class BootService extends Service {

    private static final String MODULE = "BootService";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onStart(Intent intent, int startId) {

        // Get selected service name
        String service_name = "";

        service_name = SystemProperties.get("persist.service.mts.name");
        Log.i(AmtlCore.TAG, MODULE + ": start " + service_name + " service");

        /* Test if requested service is already running */
        if (SystemProperties.get("init.svc."+service_name).equals("running")) {
            Log.i(AmtlCore.TAG, MODULE + ": "+service_name+" service already running");
        }
        else {
            /* Remove old running service if necessary */
            if (SystemProperties.get("init.svc.mtsfs").equals("running")) {
                SystemProperties.set("persist.service.mtsfs", "0");
            }
            else if (SystemProperties.get("init.svc.mtsextfs").equals("running")) {
                SystemProperties.set("persist.service.mtsextfs.enable", "0");
            }
            else if (SystemProperties.get("init.svc.mtssd").equals("running")) {
                SystemProperties.set("persist.service.mtssd.enable", "0");
            }
            else if (SystemProperties.get("init.svc.mtsextsd").equals("running")) {
                SystemProperties.set("persist.service.mtsextsd.enable", "0");
            }
            else if (service_name.equals("usbmodem")) {
                if (SystemProperties.get("init.svc.usb_to_modem").equals("running")) {
                    SystemProperties.set("persist.service.usbmodem.enable", "0");
                }
            }
            else if (SystemProperties.get("init.svc.mtsusb").equals("running")) {
                SystemProperties.set("persist.service.mtsusb.enable", "0");
            }
            else {
                /* No service enabled */
            }
        }

        if (service_name.equals("mtsusb")) {
            try {
                Log.i(AmtlCore.TAG, MODULE + ": start mtsusb service");
                AmtlCore.rtm.exec("start mtsusb");
            }
            catch (IOException e) {
                Log.e(AmtlCore.TAG, MODULE + ": can't start mtsusb service");
            }
        }
        else if (service_name.equals("disable") || service_name.equals("")) {
            Log.i(AmtlCore.TAG, MODULE + ": MTS service disabled");
        }
        else {
            Log.i(AmtlCore.TAG, MODULE + ": start " + service_name + " service");
            SystemProperties.set("persist.service." + service_name + ".enable", "1");
        }
    }
}
