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
import java.io.InputStream;

public class SynchronizeSTMD {

    private static final String AMTL_JNI_LIBRARY = "amtl_jni";

    /* Load Amtl JNI library */
    static {
        System.loadLibrary(AMTL_JNI_LIBRARY);
    }

    private native int OpenSerial(String jtty_name, int baudrate);
    private native int CloseSerial(int fd);

    private static final String TTY_NAME = "/dev/gsmtty19";
    private static final int TTY_BAUDRATE = 115200;

    private static final int TTY_CLOSED = -1;

    private static final String MODULE = "SynchronizeSTMD";

    private int ttyFd;

    public SynchronizeSTMD() {
        this.ttyFd = TTY_CLOSED;
    }

    public String getTtyName() {
        return TTY_NAME;
    }

    public void openTty() {
        /* Check if /dev/gsmtty19 is already opened */
        if (this.ttyFd == TTY_CLOSED) {
            /*Not open -> open it*/
            this.ttyFd = this.OpenSerial(TTY_NAME, TTY_BAUDRATE);
            if (this.ttyFd < 0) {
                Log.e(AmtlCore.TAG, MODULE + ": can't open " + TTY_NAME);
            } else {
                Log.d(AmtlCore.TAG, MODULE + ": " + TTY_NAME + " opened => " + this.ttyFd);
            }
        } else {
            Log.d(AmtlCore.TAG, MODULE +  ": " + TTY_NAME + " already opened => " + this.ttyFd);
        }
    }

    public void closeTty() {
        if (this.ttyFd != TTY_CLOSED) {
            this.CloseSerial(this.ttyFd);
            Log.i(AmtlCore.TAG, MODULE + ": " + TTY_NAME + " closed");
            this.ttyFd = TTY_CLOSED;
        }
    }
}
