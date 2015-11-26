/* INTEL CONFIDENTIAL
 * Copyright 2015 Intel Corporation
 *
 * The source code contained or described herein and all documents
 * related to the source code ("Material") are owned by Intel
 * Corporation or its suppliers or licensors. Title to the Material
 * remains with Intel Corporation or its suppliers and
 * licensors. The Material contains trade secrets and proprietary
 * and confidential information of Intel or its suppliers and
 * licensors. The Material is protected by worldwide copyright and
 * trade secret laws and treaty provisions. No part of the Material
 * may be used, copied, reproduced, modified, published, uploaded,
 * posted, transmitted, distributed, or disclosed in any way without
 * Intel's prior express written permission.
 *
 * No license under any patent, copyright, trade secret or other
 * intellectual property right is granted to or conferred upon you
 * by disclosure or delivery of the Materials, either expressly, by
 * implication, inducement, estoppel or otherwise. Any license under
 * such intellectual property rights must be express and approved by
 * Intel in writing.
 */

package com.intel.crashreport.specific;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.intel.crashreport.Log;
import com.intel.phonedoctor.Constants;

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

            else if( extraType.equals(NotificationReceiver.MANAGE_FREE_SPACE)) {
                PhoneInspector phoneInspector = PhoneInspector.getInstance(getApplicationContext());
                phoneInspector.manageFreeSpace(Constants.LOGS_DIR);
            }

            else if( extraType.equals(NotificationReceiver.BOOT_COMPLETED)) {

            }

        }
    }

}
