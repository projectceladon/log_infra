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
 * Author: Tony Goubert <tonyx.goubert@intel.com>
 */

package com.intel.amtl;

import android.util.Log;
import android.os.SystemProperties;

import java.io.IOException;

public class Services {
    private Modem_Configuration modem_configuration;
    private int service_val;
    Runtime rtm = java.lang.Runtime.getRuntime();

    /*Enable the service selected*/
    protected void enable_service(int service) {
        modem_configuration = new Modem_Configuration();
        try {
            switch(service) {
            case Modem_Configuration.mtsfs_persistent:
                /*emmc 100MB persistent*/
                rtm.exec("setprop persist.service.mtsfs.enable 1");
                break;
            case Modem_Configuration.mtsextfs_persistent:
                /*emmc 800MB persistent*/
                rtm.exec("setprop persist.service.mtsextfs.enable 1");
                break;
            case Modem_Configuration.mtssd_persistent:
                /*sdcard 100MB persistent*/
                rtm.exec("setprop persist.service.mtssd.enable 1");
                break;
            case Modem_Configuration.mtsextsd_persistent:
                /*sdcard 800MB persistent*/
                rtm.exec("setprop persist.service.mtsextsd.enable 1");
                break;
            case Modem_Configuration.mtsusb:
                /*USB oneshot*/
                rtm.exec("start mtsusb");
                break;
            default:
                /*Nothing to do*/
            }
        } catch (IOException e) {
            Log.e(Modem_Configuration.TAG, "Can't start the service");
            e.printStackTrace();
        }
    }

    /*Return the number of service which is enabled*/
    protected int service_status() {
        if (SystemProperties.get("persist.service.mtsfs.enable", "").equals("1")) {
            /*emmc 100MB persistent*/
            service_val = Modem_Configuration.mtsfs_persistent;
        } else if (SystemProperties.get("persist.service.mtsextfs.enable", "").equals("1")) {
            /*emmc 800MB persistent*/
            service_val = Modem_Configuration.mtsextfs_persistent;
        } else if (SystemProperties.get("persist.service.mtssd.enable", "").equals("1")) {
            /*sdcard 100MB persistent*/
            service_val = Modem_Configuration.mtssd_persistent;
        } else if (SystemProperties.get("persist.service.mtsextsd.enable", "").equals("1")) {
            /*sdcard 800MB persistent*/
            service_val = Modem_Configuration.mtsextsd_persistent;
        } else if (SystemProperties.get("init.svc.mtsusb", "").equals("running")) {
            /*USB oneshot*/
            service_val = Modem_Configuration.mtsusb;
        } else {
            /*No service enabled*/
            service_val = Modem_Configuration.mts_disable;
        }
        return service_val;
    }

    /*Stop the current service*/
    protected void stop_service(int servicevalue) {
        try {
            switch(servicevalue) {
            case Modem_Configuration.mts_disable:
                /*Already disable -> Nothing to do*/
                break;
            case Modem_Configuration.mtsfs_persistent:
                /*emmc 100MB persistent*/
                rtm.exec("setprop persist.service.mtsfs.enable 0");
                break;
            case Modem_Configuration.mtsextfs_persistent:
                /*emmc 800MB persistent*/
                rtm.exec("setprop persist.service.mtsextfs.enable 0");
                break;
            case Modem_Configuration.mtssd_persistent:
                /*sdcard 100MB persistent*/
                rtm.exec("setprop persist.service.mtssd.enable 0");
                break;
            case Modem_Configuration.mtsextsd_persistent:
                /*sdcard 800MB persistent*/
                rtm.exec("setprop persist.service.mtsextsd.enable 0");
                break;
            case Modem_Configuration.mtsusb:
                /*USB oneshot*/
                rtm.exec("stop mtsusb");
                break;
            default:
                /*Nothing to do*/
            }
        } catch (IOException e) {
            Log.e(Modem_Configuration.TAG, "ModemTraceServer can't stop the current service");
            e.printStackTrace();
        }
    }
}
