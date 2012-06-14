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

import java.io.IOException;
import java.io.RandomAccessFile;

public class ModemConfiguration {

    private static final String MODULE = "ModemConfiguration";

    public final static int XSIO_0 = 0;
    public final static int XSIO_2 = 2;
    public final static int XSIO_4 = 4;
    public final static int XSIO_5 = 5;

    /* XSIO AT commands */
    private static final String AT_SET_XSIO_FMT = "at+xsio=%d\r\n";
    private static final String AT_GET_XSIO = "at+xsio?\r\n";

    /* MUX trace AT commands */
    private static final String AT_GET_MUX_TRACE = "at+xmux?\r\n";
    private static final String AT_SET_MUX_TRACE_ON = "at+xmux=1,3,-1\r\n";
    private static final String AT_SET_MUX_TRACE_OFF = "at+xmux=1,1,0\r\n";

    /* XSYSTRACE AT commands */
    private static final String AT_GET_TRACE_LEVEL = "at+xsystrace=10\r\n";
    private static final String AT_SET_XSYSTRACE_LEVEL_DISABLE = "at+xsystrace=0\r\n";
    private static final String AT_SET_XSYSTRACE_LEVEL_BB = "at+xsystrace=0,\"bb_sw=1;3g_sw=0;digrf=0\",,\"oct=4\"\r\n";
    private static final String AT_SET_XSYSTRACE_LEVEL_BB_3G = "at+xsystrace=0,\"bb_sw=1;3g_sw=1;digrf=0\",,\"oct=4\"\r\n";
    private static final String AT_SET_XSYSTRACE_LEVEL_BB_3G_DIGRF = "at+xsystrace=0,\"digrf=1;bb_sw=1;3g_sw=1\",\"digrf=0x84\",\"oct=4\"\r\n";

    /* TRACE AT commands */
    private static final String AT_SET_TRACE_LEVEL_DISABLE = "at+trace=0,115200,\"st=0,pr=0,bt=0,ap=0,db=0,lt=0,li=0,ga=0,ae=0\"\r\n";
    private static final String AT_SET_TRACE_LEVEL_BB = "at+trace=,115200,\"st=1,pr=1,bt=1,ap=0,db=1,lt=0,li=1,ga=0,ae=0\"\r\n";
    private static final String AT_SET_TRACE_LEVEL_BB_3G = "at+trace=,115200,\"st=1,pr=1,bt=1,ap=0,db=1,lt=0,li=1,ga=0,ae=1\"\r\n";
    private static final String AT_SET_TRACE_LEVEL_BB_3G_DIGRF = "at+trace=,115200,\"st=1,pr=1,bt=1,ap=0,db=1,lt=0,li=1,ga=0,ae=1\"\r\n";

    protected final static int xsio_00 = 0;
    protected final static int xsio_20 = 1;
    protected final static int xsio_22 = 2;
    protected final static int xsio_02 = 3;
    protected final static int xsio_40 = 4;
    protected final static int xsio_44 = 5;
    protected final static int xsio_04 = 6;
    protected final static int xsio_50 = 7;
    protected final static int xsio_55 = 8;
    protected final static int xsio_05 = 9;
    protected final static int xsio_24 = 10;
    protected final static int xsio_25 = 11;
    protected final static int xsio_42 = 12;
    protected final static int xsio_45 = 13;
    protected final static int xsio_52 = 14;
    protected final static int xsio_54 = 15;
    protected final static int reboot_ok0 = 50;
    protected final static int reboot_ko0 = 51;
    protected final static int reboot_ok2 = 52;
    protected final static int reboot_ko2 = 53;
    protected final static int reboot_ok4 = 54;
    protected final static int reboot_ko4 = 55;
    protected final static int reboot_ok5 = 56;
    protected final static int reboot_ko5 = 57;

    /* Simplify the modem status : rebooted (ok) or not rebooted(ko) */
    protected int modem_reboot_status(int reboot_value) {
        int ret = reboot_ok0;
        switch (reboot_value) {
            case xsio_00:
                /* xsio = 0 and modem has been rebooted */
                ret = reboot_ok0;
                break;
            case xsio_02:
            case xsio_04:
            case xsio_05:
                /* xsio = 0 and modem has not been rebooted */
                ret = reboot_ko0;
                break;
            case xsio_22:
                /* xsio = 2 and modem has been rebooted */
                ret = reboot_ok2;
                break;
            case xsio_20:
            case xsio_24:
            case xsio_25:
                /* xsio = 2 and modem has not been rebooted */
                ret = reboot_ko2;
                break;
            case xsio_44:
                /* xsio = 4 and modem has been rebooted */
                ret = reboot_ok4;
                break;
            case xsio_40:
            case xsio_42:
            case xsio_45:
                /* xsio = 4 and modem has not been rebooted */
                ret = reboot_ko4;
                break;
            case xsio_55:
                /* xsio = 5 and modem has been rebooted */
                ret = reboot_ok5;
                break;
            case xsio_50:
            case xsio_52:
            case xsio_54:
                /* xsio = 5 and modem has not been rebooted */
                ret = reboot_ko5;
                break;
            default:
                ret = reboot_ok0;
                break;
        }
        return ret;
    }

    /* Send command to the modem and read the response */
    private int read_write_modem(RandomAccessFile f, String ival) throws IOException {
        int ret;
        byte rsp_byte_tmp[] = new byte[1024];
        int bRead = 0;
        int bCount = 0;
        byte rsp_byte[] = new byte[1024];
        byte ok_byte[] = new byte[1024];
        String modem_value;

        for (int i = 0;i < ok_byte.length;i++) {
            rsp_byte[i] = 0;
        }

        if (ival.startsWith("at+") || ival.startsWith("AT+")) {
            Log.d(AmtlCore.TAG, ival);
        }

        f.writeBytes(ival);

        do {
            bRead = f.read(rsp_byte_tmp);
            for (int i = 0;i < bRead;i++) {
                rsp_byte[i+bCount] = rsp_byte_tmp[i];
            }
            bCount += bRead;
        }
        /* find "OK\r\n" at reponse end */
        while (rsp_byte[bCount-4] != 0x4f || rsp_byte[bCount-3] != 0x4b || rsp_byte[bCount-2] != 0x0d || rsp_byte[bCount-1] != 0x0a);

        modem_value = new String(rsp_byte);

        if (ival.equals(AT_GET_XSIO)) {
            ret = getXsioValue(modem_value);
        }
        else if (ival.equals(AT_GET_TRACE_LEVEL)) {
            ret = getTraceLevelValue(modem_value);
        }
        else if (ival.equals(AT_GET_MUX_TRACE)) {
            if ((modem_value.contains("1,3,-1"))) {
                ret = CustomCfg.MUX_TRACE_ON;
            }
            else {
                ret = CustomCfg.MUX_TRACE_OFF;
            }
        }
        else {
            ret = -1;
        }
        return ret;
    }

    /* Get trace level from string */
    private int getTraceLevelValue(String s) {
        int ret = CustomCfg.TRACE_LEVEL_NONE;
        if ((s.contains("bb_sw: Oct")) && (s.contains("3g_sw: Oct")) && (s.contains("digrf: Oct"))) {
            ret = CustomCfg.TRACE_LEVEL_BB_3G_DIGRF;
        }
        else if ((s.contains("bb_sw: Oct")) && (s.contains("3g_sw: Oct"))) {
            ret = CustomCfg.TRACE_LEVEL_BB_3G;
        }
        else if (s.contains("bb_sw: Oct")) {
            ret = CustomCfg.TRACE_LEVEL_BB;
        }
        else {
            ret = CustomCfg.TRACE_LEVEL_NONE;
        }
        return ret;
    }

    /* Get XSIO value from string */
    private int getXsioValue(String s) {
        int ret = xsio_00;
        if (s.contains("0, *0")) {
            ret = xsio_00;
        }
        else if (s.contains("2, *0")) {
            ret = xsio_20;
        }
        else if (s.contains("2, *2")) {
            ret = xsio_22;
        }
        else if (s.contains("0, *2")) {
            ret = xsio_02;
        }
        else if (s.contains("4, *0")) {
            ret = xsio_40;
        }
        else if (s.contains("4, *4")) {
            ret = xsio_44;
        }
        else if (s.contains("0, *4")) {
            ret = xsio_04;
        }
        else if (s.contains("5, *0")) {
            ret = xsio_50;
        }
        else if (s.contains("5, *5")) {
            ret = xsio_55;
        }
        else if (s.contains("0, *5")) {
            ret = xsio_05;
        }
        else if (s.contains("2, *4")) {
            ret = xsio_24;
        }
        else if (s.contains("2, *5")) {
            ret = xsio_25;
        }
        else if (s.contains("4, *2")) {
            ret = xsio_42;
        }
        else if (s.contains("4, *5")) {
            ret = xsio_45;
        }
        else if (s.contains("5, *2")) {
            ret = xsio_52;
        }
        else if (s.contains("5, *4")) {
            ret = xsio_54;
        }
        else {
            ret= xsio_00;
        }
        return ret;
    }

    /* Set XSIO configuration */
    protected void setXsio(RandomAccessFile f, int xsio) {
        String atCmd;
        switch (xsio) {
            case XSIO_2:
                /* Enable coredump */
                atCmd = String.format(AT_SET_XSIO_FMT, XSIO_2);
                break;
            case XSIO_4:
                /* Enable frequency78 */
                atCmd = String.format(AT_SET_XSIO_FMT, XSIO_4);
                break;
            case XSIO_5:
                /* Enable frequency156 */
                atCmd = String.format(AT_SET_XSIO_FMT, XSIO_5);
                break;
            default:
                /* Disable */
                atCmd = String.format(AT_SET_XSIO_FMT, XSIO_0);
                break;
        }
        try {
            read_write_modem(f, atCmd);
        }
        catch (IOException e) {
            Log.e(AmtlCore.TAG, MODULE + ": can't enable_frequency");
        }
    }

    /* Set trace level */
    protected void setTraceLevel(RandomAccessFile f, int level) {
        try {
            switch (level) {
            case CustomCfg.TRACE_LEVEL_BB:
                /* MA traces */
                read_write_modem(f, AT_SET_TRACE_LEVEL_BB);
                read_write_modem(f, AT_SET_XSYSTRACE_LEVEL_BB);
                break;
            case CustomCfg.TRACE_LEVEL_BB_3G:
                /* MA & Artemis traces */
                read_write_modem(f, AT_SET_TRACE_LEVEL_BB_3G);
                read_write_modem(f, AT_SET_XSYSTRACE_LEVEL_BB_3G);
                break;
            case CustomCfg.TRACE_LEVEL_BB_3G_DIGRF:
                /* MA & Artemis & Digrf traces */
                read_write_modem(f, AT_SET_TRACE_LEVEL_BB_3G_DIGRF);
                read_write_modem(f, AT_SET_XSYSTRACE_LEVEL_BB_3G_DIGRF);
                break;
            default:
                /* Disable trace */
                read_write_modem(f, AT_SET_TRACE_LEVEL_DISABLE);
                read_write_modem(f, AT_SET_XSYSTRACE_LEVEL_DISABLE);
                break;
            }
        }
        catch (IOException e) {
            Log.e(AmtlCore.TAG, MODULE + ": can't set trace level");
        }
    }

    /* Set MUX traces on */
    protected void setMuxTraceOn(RandomAccessFile f) {
        try {
            read_write_modem(f, AT_SET_MUX_TRACE_ON);
        }
        catch (IOException e) {
            Log.e(AmtlCore.TAG, MODULE + ": can't set MUX trace ON");
        }
    }

    /* Set MUX traces off */
    protected void setMuxTraceOff(RandomAccessFile f) {
        try {
            read_write_modem(f, AT_SET_MUX_TRACE_OFF);
        }
        catch (IOException e) {
            Log.e(AmtlCore.TAG, MODULE + ": can't set MUX trace OFF");
        }
    }

    /* Get current trace level */
    protected int getTraceLevel(RandomAccessFile f) throws IOException {
        return read_write_modem(f, AT_GET_TRACE_LEVEL);
    }

    /* Get current XSIO */
    protected int getXsio(RandomAccessFile f) throws IOException {
        return read_write_modem(f, AT_GET_XSIO);
    }

    protected int getMuxTraceState(RandomAccessFile f) throws IOException {
        return read_write_modem(f, AT_GET_MUX_TRACE);
    }

}
