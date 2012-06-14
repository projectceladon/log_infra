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

import java.io.IOException;
import java.io.InputStream;

import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.os.Handler;
import android.util.Log;

public class ModemStatusMonitor implements Runnable {

    private final static String MODULE = "ModemStatusMonitor";

    public final static byte MODEM_DOWN = 0;
    public final static byte MODEM_UP = 1;
    public final static byte PLATFORM_SHUTDOWN = 2;
    public final static byte MODEM_COLD_RESET = 4;
    public final static byte UNKNOWN_MODEM_STATE = -1;

    protected LocalSocket clientSocket = null;
    protected Thread thread = null;
    protected volatile boolean stopRequested = false;

    protected volatile byte currentModemStatus;

    protected LocalSocketAddress getSocketAddress() {
        return new LocalSocketAddress("modem-status", LocalSocketAddress.Namespace.RESERVED);
    }

    protected byte getCurrentModemStatus() {
        return this.currentModemStatus;
    }

    /* Check if modem is up */
    protected boolean isModemUp() {
        return (this.currentModemStatus == MODEM_UP);
    }

    /* Start monitoring modem status */
    protected void start() {
        this.stopRequested = false;
        this.clientSocket = new LocalSocket();
        this.thread = new Thread(this);
        this.thread.setName("STMD Client");
        this.thread.start();
    }

    /* Stop monitoring modem status */
    protected void stop() {
        this.stopRequested = true;
        this.cleanUp();
        try {
            this.thread.join();
        }
        catch (InterruptedException e) {
            Log.e(AmtlCore.TAG, MODULE + ": " + e.getMessage());
        }
    }

    /* Modem status monitoring runtime */
    public void run() {

        byte[] recvBuffer = new byte[1024]; // should be large enough to contain
                                            // response
        InputStream inputStream = null;
        int readCount = 0;

        try {
            this.clientSocket.connect(this.getSocketAddress());
            inputStream = this.clientSocket.getInputStream();
        }
        catch (Exception e) {
            Log.e(AmtlCore.TAG, MODULE + ": " + e.getMessage());
            this.cleanUp();
            return;
        }

        while (!this.stopRequested) {
            try {
                readCount = inputStream.read(recvBuffer);
                this.handleResponse(recvBuffer, readCount);
            }
            catch (IOException ex) {
                Log.e(AmtlCore.TAG, MODULE + ": " + ex.toString());
                this.cleanUp();
                return;
            }
        }
    }

    /* Modem status response handler */
    protected void handleResponse(byte[] buffer, int length) {

        for (int i = 0; i < length; i += 4) {
            switch (buffer[i]) {
                case MODEM_COLD_RESET:
                    Log.i(AmtlCore.TAG, MODULE + ": modem status = MODEM_COLD_RESET");
                    this.currentModemStatus = MODEM_COLD_RESET;
                    break;
                case MODEM_DOWN:
                    Log.i(AmtlCore.TAG, MODULE + ": modem status = MODEM_DOWN");
                    this.currentModemStatus = MODEM_DOWN;
                    break;
                case MODEM_UP:
                    Log.i(AmtlCore.TAG, MODULE + ": modem status = MODEM_UP");
                    this.currentModemStatus = MODEM_UP;
                    break;
                case PLATFORM_SHUTDOWN:
                    Log.i(AmtlCore.TAG, MODULE + ": modem status = PLATFORM_SHUTDOWN");
                    this.currentModemStatus = PLATFORM_SHUTDOWN;
                    break;
                default:
                    Log.i(AmtlCore.TAG, MODULE + ": modem status = UNKNOWN");
                    this.currentModemStatus = UNKNOWN_MODEM_STATE;
                    break;
            }
        }
    }

    /* Clean modem status monitor */
    protected void cleanUp() {
        if (this.clientSocket != null) {
            try {
                this.clientSocket.shutdownInput();
                this.clientSocket.close();
            }
            catch (IOException e) {
                Log.e(AmtlCore.TAG, MODULE + ": " + e.getMessage());
            }
            this.clientSocket = null;
        }
    }
}
