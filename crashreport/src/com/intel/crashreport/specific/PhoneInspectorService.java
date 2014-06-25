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
 * Author: Jean THIRY <jeanx.thiry@intel.com>
 */

package com.intel.crashreport.specific;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.intel.crashreport.Log;

import android.app.IntentService;
import android.content.Intent;
import android.os.DropBoxManager;

/**
 * PhoneInspectorService is the service class responsible for catching intents necessary
 * for PhoneInspector class treatments such as Dropbox state, boot state...etc.
 */
public class PhoneInspectorService extends IntentService {

    private static final String TAG = "PhoneDoctor";
    private static final String Module = "PhoneInspectorService: ";

    public PhoneInspectorService() {
        super("PhoneInspectorService");
    }

    @Override
    /**
     * Manage treatment of received intent
     */
    protected void onHandleIntent(Intent intent) {

        if (intent.hasExtra(NotificationReceiver.EXTRA_TYPE)) {
            //Temporary variable to fix Klocwork reported error about 'null'
            String extraType = intent.getStringExtra(NotificationReceiver.EXTRA_TYPE);
            if(extraType == null) {
                Log.d(Module + ": Extra type is <null>, there is nothing to do for given intent.");
                return;
            }
            //Check type contained in intent and performs appropriate treatment
            if ( extraType.equals(NotificationReceiver.DROPBOX_ENTRY_ADDED)) {

                //Get intent data
                String intentTag = intent.getStringExtra(DropBoxManager.EXTRA_TAG);
                long intentTimeMs = intent.getLongExtra(DropBoxManager.EXTRA_TIME, 0);

                //Format date and log message
                Date date = new Date(intentTimeMs);
                String formattedDate = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS").format(date);
                Log.d(Module + ": New dropbox entry: " + intentTag + " " + formattedDate + " ["+ intentTimeMs + "]");

                //Manage FullDropBox case
                PhoneInspector phoneInspector = PhoneInspector.getInstance(getApplicationContext());
                phoneInspector.newDropBoxEntryAdded(intentTimeMs);
            }

            else if( extraType.equals(NotificationReceiver.BOOT_COMPLETED)) {

            }

        }
    }

}
