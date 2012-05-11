/* ACS UI Recorder
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
 * Author: Julien Reynaud <julienx.reynaud@intel.com>
 */


package com.intel.acs.uirecorder;

import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;


public final class UiRecorderBinder {

    private static final String TAG = "UiRecorderBinder";

    private static IBinder mServiceManager = null;

    public UiRecorderBinder() {
        Log.d(TAG, "Instanciating class");
    }

    public IBinder AttachService(String name) {
        if (mServiceManager == null) {
            mServiceManager = ServiceManager.getService(name);
        }
        return mServiceManager;
    }

    public int record(String path) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        int result;
        try {
            data.writeInterfaceToken(mServiceManager.getInterfaceDescriptor());
            data.writeString(path);
            mServiceManager.transact(TRANSACTION_record,data,reply,0);
            reply.readException();
            result = reply.readInt();
        }finally{
            reply.recycle();
            data.recycle();
        }
        return result;
    }

    public int replay(String path) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        int result;
        try {
            data.writeInterfaceToken(mServiceManager.getInterfaceDescriptor());
            data.writeString(path);
            mServiceManager.transact(TRANSACTION_replay,data,reply,0);
            reply.readException();
            result = reply.readInt();
        }finally{
            reply.recycle();
            data.recycle();
        }
        return result;
    }

    public int stop() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        int result;
        try {
            data.writeInterfaceToken(mServiceManager.getInterfaceDescriptor());
            mServiceManager.transact(TRANSACTION_stop,data,reply,0);
            reply.readException();
            result = reply.readInt();
        }finally{
            reply.recycle();
            data.recycle();
        }
        return result;
    }

    static final int TRANSACTION_record= (IBinder.FIRST_CALL_TRANSACTION+0);
    static final int TRANSACTION_replay= (IBinder.FIRST_CALL_TRANSACTION+1);
    static final int TRANSACTION_stop= (IBinder.FIRST_CALL_TRANSACTION+2);
}
