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

import android.util.Log;
import android.os.SystemProperties;

import java.io.IOException;

public class Services {

    private static final String MODULE = "Services";

    private static final String PERSIST_MTS_NAME = "persist.service.mts.name";

    /* Services values */
    protected final static int MTS_DISABLE = 0;
    protected final static int MTS_FS = 1;
    protected final static int MTS_EXTFS = 2;
    protected final static int MTS_SD = 3;
    protected final static int MTS_EXTSD = 4;
    protected final static int MTS_USB = 5;
    protected final static int ONLINE_BP_LOG = 6;

    private int service_val;

    /* Enable selected service */
    protected void enable_service(int service) {
        String service_name = "";
        switch (service) {
        case MTS_FS:
            /* emmc 100MB persistent => emmc100 */
            service_name = "mtsfs";
            break;
        case MTS_EXTFS:
            /* emmc 800MB persistent => emmc800 */
            service_name = "mtsextfs";
            break;
        case MTS_SD:
            /* sdcard 100MB persistent => sdcard100 */
            service_name = "mtssd";
            break;
        case MTS_EXTSD:
            /* sdcard 800MB persistent => sdcard800 */
            service_name = "mtsextsd";
            break;
        case ONLINE_BP_LOG:
            /* Online BP logging => usbmodem */
            service_name = "usbmodem";
            break;
        case MTS_USB:
            /* USB oneshot */
            service_name = "mtsusb";
            break;
        case MTS_DISABLE:
            service_name = "disable";
            break;
        default:
            /* Do nothing */
            break;
        }
        Log.i(AmtlCore.TAG, MODULE + ": enable " + service_name + " service");
        SystemProperties.set(PERSIST_MTS_NAME, service_name);
    }

    /* Return the number of service which is enabled */
    protected int service_status() {
        if (SystemProperties.get("init.svc.mtsfs").equals("running")) {
            /* emmc 100MB persistent */
            service_val = MTS_FS;
        }
        else if (SystemProperties.get("init.svc.mtsextfs").equals("running")) {
            /* emmc 800MB persistent */
            service_val = MTS_EXTFS;
        }
        else if (SystemProperties.get("init.svc.mtssd").equals("running")) {
            /* sdcard 100MB persistent */
            service_val = MTS_SD;
        }
        else if (SystemProperties.get("init.svc.mtsextsd").equals("running")) {
            /* sdcard 800MB persistent */
            service_val = MTS_EXTSD;
        }
        else if (SystemProperties.get("persist.service.usbmodem.enable").equals("1")) {
            /* Online BP logging => persistent USB to modem service */
            /* USB modem is done by a script starting and exiting continuously,
               we can't rely on init.svc.... property */
            service_val = ONLINE_BP_LOG;
        }
        else if (SystemProperties.get("init.svc.mtsusb").equals("running")) {
            /* USB oneshot */
            service_val = MTS_USB;
        }
        else {
            /* No service enabled */
            service_val = MTS_DISABLE;
        }
        return service_val;
    }

    /* Stop the current service */
    protected void stop_service() {
        try {
            int service_status = service_status();
            switch(service_status) {
            case MTS_DISABLE:
                /* Already disable => nothing to do */
                break;
            case MTS_FS:
                /* emmc 100 MB persistent */
                SystemProperties.set("persist.service.mtsfs.enable", "0");
                break;
            case MTS_EXTFS:
                /* emmc 800 MB persistent */
                SystemProperties.set("persist.service.mtsextfs.enable", "0");
                break;
            case MTS_SD:
                /* sdcard 100 MB persistent */
                SystemProperties.set("persist.service.mtssd.enable", "0");
                break;
            case MTS_EXTSD:
                /* sdcard 800 MB persistent */
                SystemProperties.set("persist.service.mtsextsd.enable", "0");
                break;
            case ONLINE_BP_LOG:
                /* Persistent USB to modem service */
                SystemProperties.set("persist.service.usbmodem.enable", "0");
                break;
            case MTS_USB:
                /* USB oneshot */
                AmtlCore.rtm.exec("stop mtsusb");
                break;
            default:
                /* Do nothing */
                break;
            }
            SystemProperties.set("persist.service.mts.name", "disable");
        }
        catch (IOException e) {
            Log.e(AmtlCore.TAG, MODULE + ": can't stop current running MTS");
        }
    }
}
