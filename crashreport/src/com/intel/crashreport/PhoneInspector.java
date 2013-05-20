/* Phone Doctor (CLOTA)
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
 * Author: Jean Thiry <jeanx.thiry@intel.com>
 */
package com.intel.crashreport;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.content.Context;
import android.os.DropBoxManager;
import android.os.SystemProperties;

/**
 * This singleton class is responsible for watching infrastructure state allowing
 * CrashReport application to adapt its behavior depending on certain conditions.
 */
public class PhoneInspector {

    private static final PhoneInspector INSTANCE = new PhoneInspector();

    /**
     * Private class attributes
     */
    private static final String TAG = "PhoneDoctor";
    private static final String Module = "PhoneInspector: ";
    private static Context mCtx;
    private static DropBoxManager mDropBoxManager;

    /**
     * Crashlog daemon mode property : this property is read by crashlog daemon
     * allowing to modify its behavior
     */
    private static final String FULL_DROPBOX_PROP = "persist.sys.crashlogd.mode";

    /**
     * Values that can be taken by Crashlog daemon FULL_DROPBOX_PROP property
     */
    public static final String LOW_MEM_MODE = "lowmemory";
    public static final String NOMINAL_MODE = "nominal";

    /**
     * Returns the PhoneInspector instance
     * @param aContext
     * @return PhoneInspector singleton
     */
    public static PhoneInspector getInstance (Context aContext) {

        if (aContext == null) {
                throw new IllegalArgumentException("Unresolved context");
        }
        mCtx = aContext;
        mDropBoxManager = (DropBoxManager) mCtx.getSystemService(Context.DROPBOX_SERVICE);
        return INSTANCE;
    }

    /**
     * Constructor
     */
    private PhoneInspector() {}

    /**
     * Check FullDropBox state (full or not) and set Crashlog daemon property according to this state so that
     * logs could be stored in /logs partition rather than in /data partition
     */
    public void manageFullDropBox() {
        ApplicationPreferences mAppPrefs = new ApplicationPreferences(mCtx);
        if (!mAppPrefs.isFullDropboxMethodAvailable()) {
            Log.w("DropBoxManager.isFull() method not available");
            return;
        }

        Method dbmIsFullM = getDropBoxManagerIsFullMethod();
        if (dbmIsFullM == null) {
            Log.w("DropBoxManager.isFull() method not available, save it");
            mAppPrefs.setFullDropboxMethodNotAvailable();
            return;
        }

        Boolean isFull = getDropBoxManagerIsFullValue(mDropBoxManager, dbmIsFullM);
        if (isFull == null) {
            mAppPrefs.setFullDropboxMethodNotAvailable();
            return;
        }

        if (isFull) {
            Log.i(Module + "DropBox full");
            SystemProperties.set(FULL_DROPBOX_PROP, LOW_MEM_MODE);
        } else {
            Log.d(Module + "DropBox not full");
            SystemProperties.set(FULL_DROPBOX_PROP, NOMINAL_MODE);
        }
    }

    /**
     * Return isFull() method of DropBoxManager class
     *
     * @return isFull() method, null if not found
     */
    private static Method getDropBoxManagerIsFullMethod() {
        Class<DropBoxManager> cl = DropBoxManager.class;
        try {
            return cl.getMethod("isFull", (Class[]) null);
        } catch (NoSuchMethodException e) {}
        return null;
    }

    /**
     * Return DropBoxManager.isFull() value
     *
     * @param mDBM DropBoxManager instance
     * @param isFull DropBoxManager.isFull() method
     * @return true or false according to the value, null if not available
     */
    private static Boolean getDropBoxManagerIsFullValue(DropBoxManager mDBM, Method isFull) {
        try {
            return (Boolean) isFull.invoke(mDBM, (Object[]) null);
        } catch (IllegalArgumentException e) {
            Log.e("DropBoxManager.isFull() method invoke fail", e);
        } catch (IllegalAccessException e) {
            Log.e("DropBoxManager.isFull() method invoke fail", e);
        } catch (InvocationTargetException e) {
            Log.e("DropBoxManager.isFull() method invoke fail", e);
        }
        return null;
    }

}
