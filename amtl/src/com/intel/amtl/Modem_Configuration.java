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

import java.io.IOException;
import java.io.RandomAccessFile;

public class Modem_Configuration {
    private int read_write_modem_status;
    protected static String gsmtty_port = "/dev/gsmtty19";
    protected static int gsmtty_baudrate = 115200;
    protected static String TAG = "AMTL";
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
    protected final static int xsio4 = 16;
    protected final static int xsio5 = 17;
    protected final static int xsio2 = 18;
    protected final static int xsio0 = 19;
    protected final int mux_disable = 20;
    protected final int mux_enable = 21;
    protected final static int mts_disable = 30;
    protected final static int mtsfs_persistent = 31;
    protected final static int mtsextfs_persistent = 32;
    protected final static int mtssd_persistent = 33;
    protected final static int mtsextsd_persistent = 34;
    protected final static int mtsusb = 35;
    protected final static int online_bp_logging_persistent = 36;
    protected final static int trace_disable = 40;
    protected final static int trace_bb = 41;
    protected final static int trace_bb_3g = 42;
    protected final static int trace_bb_3g_digrf = 43;
    protected final static int reboot_ok0 = 50;
    protected final static int reboot_ko0 = 51;
    protected final static int reboot_ok2 = 52;
    protected final static int reboot_ko2 = 53;
    protected final static int reboot_ok4 = 54;
    protected final static int reboot_ko4 = 55;
    protected final static int reboot_ok5 = 56;
    protected final static int reboot_ko5 = 57;
    Runtime rtm = java.lang.Runtime.getRuntime();

    /*Simplify the modem status : rebooted (ok) or not rebooted(ko)*/
    protected int modem_reboot_status(int reboot_value) {
        if (reboot_value == xsio_00) {
            /*xsio=0 and modem has been rebooted*/
            return reboot_ok0;
        } else if ((reboot_value == xsio_02) || (reboot_value == xsio_04) || (reboot_value == xsio_05)) {
            /*xsio=0 and modem has NOT been rebooted*/
            return reboot_ko0;
        } else if (reboot_value == xsio_22) {
            /*xsio=2 and modem has been rebooted*/
            return reboot_ok2;
        } else if ((reboot_value == xsio_20) || (reboot_value == xsio_24) || (reboot_value == xsio_25)) {
            /*xsio=2 and modem has NOT been rebooted*/
            return reboot_ko2;
        } else if (reboot_value == xsio_44) {
            /*xsio=4 and modem has been rebooted*/
            return reboot_ok4;
        } else if ((reboot_value == xsio_40) || (reboot_value == xsio_42) || (reboot_value == xsio_45)) {
            /*xsio=4 and modem has NOT been rebooted*/
            return reboot_ko4;
        } else if (reboot_value == xsio_55) {
            /*xsio=5 and modem has been rebooted*/
            return reboot_ok5;
        } else if ((reboot_value == xsio_50) || (reboot_value == xsio_52) || (reboot_value == xsio_54)) {
            /*xsio=5 and modem has NOT been rebooted*/
            return reboot_ko5;
        } else {
            return reboot_ok0;
        }
    }

    /*Send command to the modem and read the response*/
    protected int read_write_modem(String iout,String ival) throws IOException {
        byte rsp_byte_tmp[] = new byte[1024];
        int bRead = 0;
        int bCount = 0;
        byte rsp_byte[] = new byte[1024];
        byte ok_byte[] = new byte[1024];
        String modem_value;

        for(int i = 0;i < ok_byte.length;i++) {
            rsp_byte[i] = 0;
        }

        if (ival.startsWith("at+") || ival.startsWith("AT+")) {
            Log.d(TAG, ival);
        }

        RandomAccessFile f = new RandomAccessFile(iout, "rwd");
        f.writeBytes(ival);

        do {
            bRead = f.read(rsp_byte_tmp);
            for (int i = 0;i < bRead;i++) {
                rsp_byte[i+bCount] = rsp_byte_tmp[i];
            }
            bCount += bRead;
        }
        /*find "OK\r\n" at reponse end*/
        while (rsp_byte[bCount-4] != 0x4f || rsp_byte[bCount-3] != 0x4b || rsp_byte[bCount-2] != 0x0d || rsp_byte[bCount-1] != 0x0a);

            modem_value = new String(rsp_byte);

            if (ival == "at+xsio?\r\n") {
                if (modem_value.contains("0, *0")) {
                    read_write_modem_status = xsio_00;
                } else if (modem_value.contains("2, *0")) {
                    read_write_modem_status = xsio_20;
                } else if (modem_value.contains("2, *2")) {
                    read_write_modem_status = xsio_22;
                } else if (modem_value.contains("0, *2")) {
                    read_write_modem_status = xsio_02;
                } else if (modem_value.contains("4, *0")) {
                    read_write_modem_status = xsio_40;
                } else if (modem_value.contains("4, *4")) {
                    read_write_modem_status = xsio_44;
                } else if (modem_value.contains("0, *4")) {
                    read_write_modem_status = xsio_04;
                } else if (modem_value.contains("5, *0")) {
                    read_write_modem_status = xsio_50;
                } else if (modem_value.contains("5, *5")) {
                    read_write_modem_status = xsio_55;
                } else if (modem_value.contains("0, *5")) {
                    read_write_modem_status = xsio_05;
                } else if (modem_value.contains("2, *4")) {
                    read_write_modem_status = xsio_24;
                } else if (modem_value.contains("2, *5")) {
                    read_write_modem_status = xsio_25;
                } else if (modem_value.contains("4, *2")) {
                    read_write_modem_status = xsio_42;
                } else if (modem_value.contains("4, *5")) {
                    read_write_modem_status = xsio_45;
                } else if (modem_value.contains("5, *2")) {
                    read_write_modem_status = xsio_52;
                } else if (modem_value.contains("5, *4")) {
                    read_write_modem_status = xsio_54;
                } else {
                    read_write_modem_status = xsio_00;
                }
            } else if (ival == "at+xsystrace=10\r\n") {
                if ((modem_value.contains("bb_sw: Oct")) && (modem_value.contains("3g_sw: Oct")) && (modem_value.contains("digrf: Oct"))) {
                    read_write_modem_status = trace_bb_3g_digrf;
                } else if ((modem_value.contains("bb_sw: Oct")) && (modem_value.contains("3g_sw: Oct"))) {
                    read_write_modem_status = trace_bb_3g;
                } else if (modem_value.contains("bb_sw: Oct")) {
                    read_write_modem_status = trace_bb;
                } else {
                    read_write_modem_status = trace_disable;
                }
            } else if (ival == "at+xmux?\r\n") {
                if ((modem_value.contains("1,3,-1"))) {
                    read_write_modem_status = mux_enable;
                } else {
                    read_write_modem_status = mux_disable;
                }
            }
        f.close();
        return read_write_modem_status;
    }

    /*Send commands to the modem to enable/disable mux traces*/
    protected void enable_xsio(int xsio) {
        try {
            if (xsio == xsio4) {
                /*Enable frequency78*/
                read_write_modem(gsmtty_port,"at+xsio=4\r\n");
            } else if (xsio == xsio5) {
                /*Enable frequency156*/
                read_write_modem(gsmtty_port,"at+xsio=5\r\n");
            } else if (xsio == xsio2) {
                /*Enable coredump*/
                read_write_modem(gsmtty_port,"at+xsio=2\r\n");
            } else {
                /*Disable*/
                read_write_modem(gsmtty_port,"at+xsio=0\r\n");
            }
        } catch (IOException e) {
            Log.e(TAG, "Modem_Configuration can't enable_frequency");
            e.printStackTrace();
        } catch (NullPointerException e) {
            Log.v(TAG, "Modem_Configuration can't enable_frequency : null pointer");
        }
    }

    /*Enable trace and xsystrace*/
    protected void enable_trace_level(int levelvalue) {
        try {
            if (levelvalue == trace_bb) {
                /*MA traces*/
                read_write_modem(gsmtty_port,"at+trace=,115200,\"st=1,pr=1,bt=1,ap=0,db=1,lt=0,li=1,ga=0,ae=0\"\r\n");
                read_write_modem(gsmtty_port,"at+xsystrace=0,\"bb_sw=1\",,\"oct=4\"\r\n");
            } else if (levelvalue == trace_bb_3g) {
                /*MA & Artemis traces*/
                read_write_modem(gsmtty_port,"at+trace=,115200,\"st=1,pr=1,bt=1,ap=0,db=1,lt=0,li=1,ga=0,ae=1\"\r\n");
                read_write_modem(gsmtty_port,"at+xsystrace=0,\"bb_sw=1;3g_sw=1\",,\"oct=4\"\r\n");
            } else if (levelvalue == trace_bb_3g_digrf) {
                /*MA & Artemis & Digrf traces*/
                read_write_modem(gsmtty_port,"at+trace=,115200,\"st=1,pr=1,bt=1,ap=0,db=1,lt=0,li=1,ga=0,ae=1\"\r\n");
                read_write_modem(gsmtty_port,"at+xsystrace=0,\"digrf=1;bb_sw=1;3g_sw=1\",\"digrf=0x84\",\"oct=4\"\r\n");
            } else {
                /*Disable trace*/
                read_write_modem(gsmtty_port,"at+trace=0,115200,\"st=0,pr=0,bt=0,ap=0,db=0,lt=0,li=0,ga=0,ae=0\"\r\n");
                read_write_modem(gsmtty_port,"at+xsystrace=0\r\n");
            }
        } catch (IOException e) {
            Log.e(TAG, "ModemTraceServer can't enable_trace_level");
            e.printStackTrace();
        }
    }

    /*Send commands to the modem to enable/disable mux traces*/
    protected void enable_mux_trace(int muxvalue) {
        try {
            if (muxvalue == mux_enable) {
                /*Enable Mux traces*/
                read_write_modem(gsmtty_port,"at+xmux=1,3,-1\r\n");
            } else {
                /*Disable Mux traces*/
                read_write_modem(gsmtty_port,"at+xmux=1,1,0\r\n");
            }
        } catch (IOException e) {
            Log.e(TAG, "Modem_Configuration can't enable_mux_trace");
            e.printStackTrace();
        }
    }
}
