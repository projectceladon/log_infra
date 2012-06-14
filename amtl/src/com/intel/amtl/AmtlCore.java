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
import android.content.Context;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

public class AmtlCore {

    /* AMTL log tag */
    public static final String TAG = "AMTL";
    /* Module log tag */
    private static final String MODULE = "Core";
    /* USB switch service state file */
    public static final String OUTPUT_FILE = "usbswitch.conf";

    /* AMTL core exception messages  */
    private static final String ERR_MODEM_NOT_READY = "Modem is not ready";
    private static final String ERR_APPLY_CFG = "Failed to apply configuration";
    private static final String ERR_UPDATE_CFG = "Failed to get current configuration";

    private static final int MAX_MODEM_STATUS_RETRY = 5;
    private static final int MODEM_STATUS_RETRY_INTERVAL = 1000;

    /* AMTL Core reference: singleton design pattern */
    private static AmtlCore core;

    public static final Runtime rtm = java.lang.Runtime.getRuntime();

    /* Current predefined configuration */
    private PredefinedCfg curCfg;
    /* Future predefined configuration to set */
    private PredefinedCfg futCfg;

    /* Current custom configuration */
    private CustomCfg curCustomCfg;
    /* Future custom configuration to set */
    private CustomCfg futCustomCfg;

    /* Flag of first configuration set:
       to not interpret as a configuration to set */
    private boolean firstCfgSet = true;

    /* Current configuration values */
    private int serviceValue;
    private int traceLevelValue;
    private int xsioValue;
    private int infoModemReboot;
    private int muxTraceValue;

    private Services services;
    private ModemConfiguration modemCfg;
    private ModemStatusMonitor modemStatusMonitor;
    private SynchronizeSTMD ttyManager;

    private Context ctx;

    private RandomAccessFile gsmtty;

    /* Constructor */
    private AmtlCore() throws AmtlCoreException {
        Log.i(TAG, MODULE + ": create application core");

        try {
            /* Create status monitor and open gsmtty device */
            this.modemStatusMonitor = new ModemStatusMonitor();
            this.ttyManager = new SynchronizeSTMD();
            this.ttyManager.openTty();
            this.gsmtty = new RandomAccessFile(this.ttyManager.getTtyName(), "rw");
        }
        catch (ExceptionInInitializerError ex) {
            throw new AmtlCoreException("AMTL library not found, please install it first");
        }
        catch (Exception ex) {
            throw new AmtlCoreException(String.format("Error while opening %s", this.ttyManager.getTtyName()));
        }

        this.firstCfgSet = true;
        this.curCfg = PredefinedCfg.UNKNOWN_CFG;
        this.futCfg = PredefinedCfg.UNKNOWN_CFG;

        this.modemCfg = new ModemConfiguration();
        this.services = new Services();

        this.curCustomCfg = new CustomCfg();
        this.futCustomCfg = new CustomCfg();

        this.ctx = null;

        /* Don't forget to start modem status monitoring */
        this.modemStatusMonitor.start();

        /* Wait for modem readiness to avoid writing on modem device too early */
        int nbTry = 0;
        do {
            nbTry++;
            android.os.SystemClock.sleep(MODEM_STATUS_RETRY_INTERVAL);
        }
        while (!this.modemStatusMonitor.isModemUp() && nbTry < MAX_MODEM_STATUS_RETRY);

        if (nbTry >= MAX_MODEM_STATUS_RETRY) {
            throw new AmtlCoreException(ERR_MODEM_NOT_READY);
        }
    }

    /* Set Amtl core application context */
    protected void setContext(Context ctx) {
        this.ctx = ctx;
    }

    /* Get a reference of AMTL core application: singleton design pattern */
    protected static AmtlCore get() throws AmtlCoreException {
        if (core == null) {
            core = new AmtlCore();
        }
        return core;
    }

    /* Destructor */
    protected void destroy() {
        /* Close gsmtty device */
        if (this.gsmtty != null) {
            try {
                this.gsmtty.close();
            }
            catch (IOException ex) {
                Log.e(TAG, MODULE + ": " + ex.toString());
            }
        }
        /* Stop modem status monitoring */
        if (this.modemStatusMonitor != null) {
            this.modemStatusMonitor.stop();
            this.modemStatusMonitor = null;
        }
        /* Core is not available anymore */
        this.core = null;
    }

    /* Set configuration to use after reboot */
    protected int setCfg(PredefinedCfg cfg) {
        switch (cfg) {
            case COREDUMP:
            case OFFLINE_BP_LOG:
            case ONLINE_BP_LOG:
            case TRACE_DISABLE:
                this.futCfg = cfg;
                break;
            default:
                this.futCfg = PredefinedCfg.UNKNOWN_CFG;
                Log.e(TAG, MODULE + ": can't set configuration, unknown configuration");
                return -1;
        }
        return 0;
    }

    /* Set custom configuration to use after reboot */
    protected int setCustomCfg(CustomCfg cfg) {
        this.futCfg = PredefinedCfg.CUSTOM;
        this.futCustomCfg = cfg;
        return 0;
    }

    /* Test if a reboot is needed */
    protected boolean rebootNeeded() {
        if (this.futCfg == PredefinedCfg.CUSTOM) {
            return(
                this.curCustomCfg.traceLocation != this.futCustomCfg.traceLocation ||
                this.curCustomCfg.traceLevel != this.futCustomCfg.traceLevel ||
                this.curCustomCfg.traceFileSize != this.futCustomCfg.traceFileSize ||
                this.curCustomCfg.hsiFrequency != this.futCustomCfg.hsiFrequency);
        }
        else {
            return (this.curCfg != this.futCfg);
        }
    }

    /* Get current active configuration */
    protected PredefinedCfg getCurCfg() {
        return this.curCfg;
    }

    /* Get current custom configuration */
    protected CustomCfg getCurCustomCfg() {
        return this.curCustomCfg;
    }

    /* Apply selected configuration */
    protected void applyCfg() throws AmtlCoreException {
        if (!modemStatusMonitor.isModemUp()) {
            throw new AmtlCoreException(ERR_MODEM_NOT_READY);
        }
        try {
            /* Stop current running service */
            this.services.stop_service();
            switch (this.futCfg) {
                case COREDUMP:
                    applyCoredumpCfg();
                    break;
                case OFFLINE_BP_LOG:
                    applyOfflineBpLogCfg();
                    break;
                case ONLINE_BP_LOG:
                    applyOnlineBpLogCfg();
                    break;
                case TRACE_DISABLE:
                    applyTraceDisableCfg();
                    break;
                case CUSTOM:
                    applyCustomCfg();
                    break;
                default:
                    Log.e(TAG, MODULE + ": unknown configuration to apply");
                    break;
            }
        }
        catch (IOException e) {
            Log.e(TAG, MODULE + ": can't apply configuration");
            throw new AmtlCoreException(ERR_APPLY_CFG);
        }
    }

    /* Apply blue configuration */
    private void applyCoredumpCfg() throws IOException {
        this.modemCfg.setXsio(this.gsmtty, ModemConfiguration.XSIO_2);
        this.modemCfg.setTraceLevel(this.gsmtty, CustomCfg.TRACE_LEVEL_BB_3G);
        this.services.stop_service();
    }

    /* Apply green configuration */
    private void applyOfflineBpLogCfg() throws IOException {
        this.services.stop_service();
        this.modemCfg.setXsio(this.gsmtty, ModemConfiguration.XSIO_4);
        this.modemCfg.setTraceLevel(this.gsmtty, CustomCfg.TRACE_LEVEL_BB_3G);
        this.services.enable_service(Services.MTS_EXTFS);
    }

    /* Apply purple configuration */
    private void applyOnlineBpLogCfg() throws IOException {
        this.services.stop_service();
        this.modemCfg.setXsio(this.gsmtty, ModemConfiguration.XSIO_0);
        this.modemCfg.setTraceLevel(this.gsmtty, CustomCfg.TRACE_LEVEL_BB_3G);
        this.services.enable_service(Services.ONLINE_BP_LOG);
    }

    /* Apply yellow configuration */
    private void applyTraceDisableCfg() throws IOException {
        this.modemCfg.setXsio(this.gsmtty, ModemConfiguration.XSIO_0);
        this.modemCfg.setTraceLevel(this.gsmtty, CustomCfg.TRACE_LEVEL_NONE);
        this.services.stop_service();
    }

    /* Apply custom configuration */
    private void applyCustomCfg() {
        int serviceToStart;
        int xsioToSet;

        /* Determine service to configure */
        if (this.futCustomCfg.traceLocation == CustomCfg.TRACE_LOC_EMMC) {
            serviceToStart = (this.futCustomCfg.traceFileSize == CustomCfg.LOG_SIZE_100_MB) ? Services.MTS_FS: Services.MTS_EXTFS;
        }
        else if (this.futCustomCfg.traceLocation == CustomCfg.TRACE_LOC_SDCARD) {
            serviceToStart = (this.futCustomCfg.traceFileSize == CustomCfg.LOG_SIZE_100_MB) ? Services.MTS_SD: Services.MTS_EXTSD;
        }
        else if (this.futCustomCfg.traceLocation == CustomCfg.TRACE_LOC_USB_APE) {
            serviceToStart = Services.MTS_USB;
        }
        else if (this.futCustomCfg.traceLocation == CustomCfg.TRACE_LOC_USB_MODEM) {
            serviceToStart = Services.ONLINE_BP_LOG;
        }
        else {
            serviceToStart = Services.MTS_DISABLE;
        }

        /* Determine XSIO value to set */
        if (this.futCustomCfg.traceLocation == CustomCfg.TRACE_LOC_EMMC ||
            this.futCustomCfg.traceLocation == CustomCfg.TRACE_LOC_SDCARD) {
            xsioToSet = (this.futCustomCfg.hsiFrequency == CustomCfg.HSI_FREQ_78_MHZ) ? ModemConfiguration.XSIO_5: ModemConfiguration.XSIO_4;
        }
        else if (this.futCustomCfg.traceLocation == CustomCfg.TRACE_LOC_COREDUMP) {
            xsioToSet = ModemConfiguration.XSIO_2;
        }
        else {
            xsioToSet = ModemConfiguration.XSIO_0;
        }

        /* Apply configuration */
        this.services.stop_service();
        this.modemCfg.setXsio(this.gsmtty, xsioToSet);
        this.modemCfg.setTraceLevel(this.gsmtty, futCustomCfg.traceLevel);
        this.services.enable_service(serviceToStart);
    }

    /* Enable/Disable MUX traces */
    protected void setMuxTrace(int muxTrace) {
        if (muxTrace == CustomCfg.MUX_TRACE_ON) {
            this.modemCfg.setMuxTraceOn(this.gsmtty);
        }
        else {
            this.modemCfg.setMuxTraceOff(this.gsmtty);
        }
    }

    /* Force AMTL Core to update its internal modem configuration values */
    protected void invalidate() throws AmtlCoreException {
        if (!this.modemStatusMonitor.isModemUp()) {
            throw new AmtlCoreException(ERR_MODEM_NOT_READY);
        }
        try {
            Log.i(TAG, MODULE + ": update current config");
            /* If necessary Create and Update usbswitch.conf file */
            usbswitch_update();

            /* Recover the current configuration */
            /* Current service */
            this.serviceValue = this.services.service_status();
            /* Current trace level */
            this.traceLevelValue = this.modemCfg.getTraceLevel(this.gsmtty);
            /* Current XSIO */
            this.xsioValue = this.modemCfg.getXsio(this.gsmtty);
            /* Current MUX trace state */
            this.muxTraceValue = this.modemCfg.getMuxTraceState(this.gsmtty);

            /* Update custom configuration for settings activity */
            this.curCustomCfg.traceLevel = this.traceLevelValue;
            this.curCustomCfg.muxTrace = this.muxTraceValue;

            /* Recover the modem reboot information */
            this.infoModemReboot = this.modemCfg.modem_reboot_status(xsioValue);

            if (((this.infoModemReboot == ModemConfiguration.reboot_ok2) ||
                (this.infoModemReboot == ModemConfiguration.reboot_ko2)) &&
                (this.serviceValue == Services.MTS_DISABLE) &&
                (this.traceLevelValue == CustomCfg.TRACE_LEVEL_BB_3G)) {
                /* Trace in coredump enabled */
                this.curCfg = PredefinedCfg.COREDUMP;
            }
            else if (((this.infoModemReboot == ModemConfiguration.reboot_ok4) ||
                (this.infoModemReboot == ModemConfiguration.reboot_ko4)) &&
                (this.serviceValue == Services.MTS_EXTFS) &&
                (this.traceLevelValue == CustomCfg.TRACE_LEVEL_BB_3G)) {
                /*Trace in APE log file enabled*/
                this.curCfg = PredefinedCfg.OFFLINE_BP_LOG;
            }
            else if (((this.infoModemReboot == ModemConfiguration.reboot_ok0) ||
                (this.infoModemReboot == ModemConfiguration.reboot_ko0)) &&
                (this.serviceValue == Services.ONLINE_BP_LOG) &&
                (this.traceLevelValue == CustomCfg.TRACE_LEVEL_BB_3G)) {
                /* Online BP logging */
                this.curCfg = PredefinedCfg.ONLINE_BP_LOG;
            }
            else if (((this.infoModemReboot == ModemConfiguration.reboot_ok0) ||
                (this.infoModemReboot == ModemConfiguration.reboot_ko0)) &&
                (this.serviceValue == Services.MTS_DISABLE) &&
                (this.traceLevelValue == CustomCfg.TRACE_LEVEL_NONE)) {
                /*Trace disabled*/
                this.curCfg = PredefinedCfg.TRACE_DISABLE;
            }
            else {
                this.curCfg = PredefinedCfg.UNKNOWN_CFG;
            }

            switch (this.serviceValue) {
                case Services.MTS_DISABLE:
                    this.curCustomCfg.hsiFrequency = CustomCfg.HSI_FREQ_NONE;
                    this.curCustomCfg.traceFileSize = CustomCfg.LOG_SIZE_NONE;
                    this.curCustomCfg.traceLocation =
                        (this.traceLevelValue == CustomCfg.TRACE_LEVEL_NONE) ? CustomCfg.TRACE_LOC_NONE: CustomCfg.TRACE_LOC_COREDUMP;
                    break;
                case Services.MTS_FS:
                    this.curCustomCfg.hsiFrequency = CustomCfg.HSI_FREQ_78_MHZ;
                    this.curCustomCfg.traceLocation = CustomCfg.TRACE_LOC_EMMC;
                    this.curCustomCfg.traceFileSize = CustomCfg.LOG_SIZE_100_MB;
                    break;
                case Services.MTS_EXTFS:
                    this.curCustomCfg.hsiFrequency = CustomCfg.HSI_FREQ_78_MHZ;
                    this.curCustomCfg.traceLocation = CustomCfg.TRACE_LOC_EMMC;
                    this.curCustomCfg.traceFileSize = CustomCfg.LOG_SIZE_800_MB;
                    break;
                case Services.MTS_SD:
                    this.curCustomCfg.hsiFrequency = CustomCfg.HSI_FREQ_78_MHZ;
                    this.curCustomCfg.traceLocation = CustomCfg.TRACE_LOC_SDCARD;
                    this.curCustomCfg.traceFileSize = CustomCfg.LOG_SIZE_100_MB;
                    break;
                case Services.MTS_EXTSD:
                    this.curCustomCfg.hsiFrequency = CustomCfg.HSI_FREQ_78_MHZ;
                    this.curCustomCfg.traceLocation = CustomCfg.TRACE_LOC_SDCARD;
                    this.curCustomCfg.traceFileSize = CustomCfg.LOG_SIZE_800_MB;
                    break;
                case Services.MTS_USB:
                    this.curCustomCfg.hsiFrequency = CustomCfg.HSI_FREQ_NONE;
                    this.curCustomCfg.traceLocation = CustomCfg.TRACE_LOC_USB_APE;
                    this.curCustomCfg.traceFileSize = CustomCfg.LOG_SIZE_NONE;
                    break;
                case Services.ONLINE_BP_LOG:
                    this.curCustomCfg.hsiFrequency = CustomCfg.HSI_FREQ_78_MHZ;
                    this.curCustomCfg.traceLocation = CustomCfg.TRACE_LOC_USB_MODEM;
                    this.curCustomCfg.traceFileSize = CustomCfg.LOG_SIZE_800_MB;
                    break;
            }

            if (this.firstCfgSet) {
                this.futCfg = this.curCfg;
                this.firstCfgSet = false;
            }

        }
        catch (IOException e) {
            Log.e(TAG, MODULE + ": can't retreive current configuration => " + e.getMessage());
            throw new AmtlCoreException(ERR_UPDATE_CFG);
        }
    }

    /*Create usbswitch.conf file if it doesn't exist*/
    protected void usbswitch_update() {
        FileOutputStream usbswitch = null;
        try {
            if (ctx != null) {
                usbswitch = this.ctx.openFileOutput(OUTPUT_FILE, Context.MODE_APPEND);
            }
            else {
                Log.e(TAG, MODULE + ": failed to open " + OUTPUT_FILE + " (NULL context)");
            }
        }
        catch (IOException e) {
            Log.e(TAG, MODULE + ": can't create the file usbswitch.conf");
        }
        finally {
            try {
                if (usbswitch != null)
                    usbswitch.close();
            }
            catch (IOException e) {
                Log.e(TAG, MODULE + ": " + e.getMessage());
            }
        }

        /*Update the value of usbswitch in /data/data/com.intel.amtl/file/usbswitch.conf
        * 0: usb ape
        * 1: usb modem */
        try {
            this.rtm.exec("start usbswitch_status");
        }
        catch (IOException e) {
            Log.e(TAG, MODULE + ": can't start the service usbswitch_status");
        }
    }

    /* Get current service value */
    protected int getCurService() {
        return this.serviceValue;
    }

    /* Get current trace level value */
    protected int getCurTraceLevel() {
        return this.traceLevelValue;
    }

    /* Get current XSIO value */
    protected int getXsioValue() {
        return this.xsioValue;
    }

    /* Get current info modem reboot value */
    protected int getInfoModemReboot() {
        return this.infoModemReboot;
    }

    /* Get MUX trace status */
    protected int getMuxTraceValue() {
        return this.muxTraceValue;
    }
}
