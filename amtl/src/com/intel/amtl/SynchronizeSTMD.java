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

import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

public class SynchronizeSTMD extends Thread {
    /*Load jni library*/
    static {
        System.loadLibrary("amtl_jni");
    }
    public native int OpenSerial(String jtty_name, int baudrate);
    public native int CloseSerial(int fd);

    private static final int SOCKET_OPEN = 0;
    private static final int GET_MSG = 1;

    /*STMD status*/
    private final static byte MODEM_DOWN = 0;
    private final static byte MODEM_UP = 1;
    private final static byte PLATFORM_SHUTDOWN = 2;
    private final static byte MODEM_COLD_RESET = 4;

    protected boolean flag = true;

    /*Socket stuff*/
    private static final String MODEM_SOCKET_NAME = "modem-status";
    private static final int SOCKET_OPEN_RETRY_MILLIS = 4 * 1000;
    protected LocalSocket mSocket;

    protected Modem_Application modem_application;

    public SynchronizeSTMD(Modem_Application modem_application) {
        this.modem_application = modem_application;
        modem_application.port_fd = -1;
    }

    protected void open_gsmtty() {
        /*Check if /dev/gsmtty19 is already open*/
        if (modem_application.port_fd < 0) {
            /*Not open -> open it*/
            modem_application.port_fd = this.OpenSerial(Modem_Configuration.gsmtty_port, Modem_Configuration.gsmtty_baudrate);
            if (modem_application.port_fd < 0)
            {
                Log.e(Modem_Configuration.TAG, "open_gsmtty() can't open port_fd");
            } else {
                Log.d(Modem_Configuration.TAG, "open_gsmtty() opens port_fd "+modem_application.port_fd);
            }
        } else {
            Log.d(Modem_Configuration.TAG, "open_gsmtty() port_fd already opens "+modem_application.port_fd);
        }
    }

    protected void close_gsmtty() {
            this.CloseSerial(modem_application.port_fd);
            modem_application.port_fd = -1;
    }

    /*method: Log state and process socket messages*/
    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case SOCKET_OPEN:
                Log.d(Modem_Configuration.TAG, "handler socket open");
                break;
            case GET_MSG:
                Log.d(Modem_Configuration.TAG, "handler get_msg");
                break;
            default:
                Log.d(Modem_Configuration.TAG, "handler default");
                break;
            }
        };
    };

    @Override
    public void run() {
        int retryCount = 0;
        int retryDelay = SOCKET_OPEN_RETRY_MILLIS;
        try {
            while (flag) {
                LocalSocket s = null;
                LocalSocketAddress l;

                /*try to connect socket*/
                try {
                    s = new LocalSocket();

                    /*connect modem-status stmd*/
                    l = new LocalSocketAddress(MODEM_SOCKET_NAME,
                            LocalSocketAddress.Namespace.RESERVED);
                    s.connect(l);
                } catch (IOException ex) {
                    Log.d(Modem_Configuration.TAG, "open socket fail: ", ex);

                    /*connect fail*/
                    try {
                        if (s != null) {
                            Log.d(Modem_Configuration.TAG, "connect fail");
                            s.close();
                        }
                    } catch (IOException ex2) {
                        /*ignore failure to close after failure to connect*/
                    }
                    /*Don't print an error message after the the first time or after the 8th time */
                    if (retryCount == 8) {
                        Log.d(Modem_Configuration.TAG, "Couldn't find '" + MODEM_SOCKET_NAME
                                + "' socket after " + retryCount
                                + " times, continuing to retry silently");
                    } else if (retryCount > 0 && retryCount < 8) {
                        Log.d(Modem_Configuration.TAG, "Couldn't find '" + MODEM_SOCKET_NAME
                                + "' socket; retrying after timeout");
                    }

                    try {
                        /*Retry with 20 minutes maximum*/
                        Log.d(Modem_Configuration.TAG, "retry delay");
                        if (retryDelay < 1200000)
                            retryDelay *= 2;
                        Thread.sleep(retryDelay);
                        Log.d(Modem_Configuration.TAG, "sleep");
                    } catch (InterruptedException er) {
                    }

                    retryCount++;
                    continue;
                }
                retryCount = 0;
                mSocket = s;
                handler.sendEmptyMessage(SOCKET_OPEN);

                int length = 0;
                /*read data on socket*/
                try {
                    InputStream inputStream = mSocket.getInputStream();

                    byte data[] = new byte[1024];

                    while (flag) {
                        if (inputStream.read(data) != -1) {

                            switch(data[0]) {
                            case MODEM_DOWN:
                                modem_application.modem_status = 0;
                                Log.d(Modem_Configuration.TAG,"MODEM DOWN.");
                                break;
                            case MODEM_UP:
                                modem_application.modem_status = 1;
                                Log.d(Modem_Configuration.TAG,"MODEM UP.");
                                open_gsmtty();
                                break;
                            case PLATFORM_SHUTDOWN:
                                modem_application.modem_status = 2;
                                Log.d(Modem_Configuration.TAG,"PLATFORM_SHUTDOWN.");
                                break;
                            case MODEM_COLD_RESET:
                                modem_application.modem_status = 4;
                                Log.d(Modem_Configuration.TAG,"MODEM_COLD_RESET.");
                                break;
                            default :
                                Log.d(Modem_Configuration.TAG,"Command unknown.");
                            }
                        }
                    }

                } catch (java.io.IOException ex) {
                    Log.d(Modem_Configuration.TAG, "'" + MODEM_SOCKET_NAME + "' socket closed",
                            ex);
                } catch (Throwable tr) {
                    Log.e(Modem_Configuration.TAG, "Uncaught exception read length=" + length
                            + "Exception:" + tr.toString());
                }
                Log.d(Modem_Configuration.TAG, "Disconnected from '" + MODEM_SOCKET_NAME
                        + "' socket");
                try {
                    mSocket.close();
                } catch (IOException ex) {
                    /*ignore failure to close socket*/
                }
                mSocket = null;
            }
        } catch (Throwable tr) {
            Log.e(Modem_Configuration.TAG, "Uncaught exception", tr);
        }
    }
}
