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

package com.intel.crashreport.receivers;

import static com.intel.phonedoctor.Constants.TAG;
import static com.intel.phonedoctor.PDIntentConstants.EXTRA_DATA0;
import static com.intel.phonedoctor.PDIntentConstants.EXTRA_DATA1;
import static com.intel.phonedoctor.PDIntentConstants.EXTRA_DATA2;
import static com.intel.phonedoctor.PDIntentConstants.EXTRA_DATA3;
import static com.intel.phonedoctor.PDIntentConstants.EXTRA_DATA4;
import static com.intel.phonedoctor.PDIntentConstants.EXTRA_DATA5;
import static com.intel.phonedoctor.PDIntentConstants.EXTRA_TYPE;
import static com.intel.phonedoctor.PDIntentConstants.EXTRA_FILE;
import static com.intel.phonedoctor.PDIntentConstants.INTENT_REPORT_ERROR;
import static com.intel.phonedoctor.PDIntentConstants.INTENT_REPORT_INFO;
import static com.intel.phonedoctor.PDIntentConstants.INTENT_REPORT_STATS;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.intel.crashreport.CustomizableEventData;
import com.intel.crashreport.GeneralEventGenerator;
import com.intel.crashreport.specific.EventGenerator;
import com.intel.phonedoctor.utils.FileOps;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

/**
 * @brief EventReceiverService create event according to the passed intent.
 */
public class EventReceiverService extends IntentService {

    public static final String Module = "EventReceiverService: ";

    public static final String EXTRA_ORIGINAL_ACTION = "intel.intent.extra.ORIGINAL_ACTION";

    public EventReceiverService() {
        super("EventReceiverService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        CustomizableEventData mEvent;
        String mExtra;

        // Extract event name
        if (INTENT_REPORT_INFO.equals(intent.getStringExtra(EXTRA_ORIGINAL_ACTION))) {
            mEvent = EventGenerator.INSTANCE.getEmptyInfoEvent();
        } else if (INTENT_REPORT_ERROR.equals(intent.getStringExtra(EXTRA_ORIGINAL_ACTION))) {
            mEvent = EventGenerator.INSTANCE.getEmptyErrorEvent();
        } else if (INTENT_REPORT_STATS.equals(intent.getStringExtra(EXTRA_ORIGINAL_ACTION))) {
            mEvent = EventGenerator.INSTANCE.getEmptyStatsEvent();
        } else {
            Log.w(TAG, Module + "Intent action not regonized: " + intent.getStringExtra(EXTRA_ORIGINAL_ACTION) +
                  " , Event generation aborted");
            return;
        }

        // Event type is mandatory
        mExtra = intent.getStringExtra(EXTRA_TYPE);
        if (mExtra != null) {
            mEvent.setType(mExtra);
        } else {
            Log.w(TAG, Module + "Event Type (EXTRA_TYPE) not provided, Event generation aborted");
            return;
        }

        // Extract data fields
        mExtra = intent.getStringExtra(EXTRA_DATA0);
        if (mExtra != null)
            mEvent.setData0(mExtra);
        mExtra = intent.getStringExtra(EXTRA_DATA1);
        if (mExtra != null)
            mEvent.setData1(mExtra);
        mExtra = intent.getStringExtra(EXTRA_DATA2);
        if (mExtra != null)
            mEvent.setData2(mExtra);
        mExtra = intent.getStringExtra(EXTRA_DATA3);
        if (mExtra != null)
            mEvent.setData3(mExtra);
        mExtra = intent.getStringExtra(EXTRA_DATA4);
        if (mExtra != null)
            mEvent.setData4(mExtra);
        mExtra = intent.getStringExtra(EXTRA_DATA5);
        if (mExtra != null)
            mEvent.setData5(mExtra);

        // File copy
        mExtra = intent.getStringExtra(EXTRA_FILE);
        if (mExtra != null) {
            Log.d(TAG, Module + "file : " + mExtra);
            File inFile = new File(mExtra);
            if (inFile.exists() && !inFile.isDirectory()) {
                try {
                    File outDir = GeneralEventGenerator.INSTANCE.getNewEventDirectory();
                    mEvent.setCrashDir(outDir.getAbsolutePath());
                    retrieveFileToDirectory(inFile, outDir);
                } catch (FileNotFoundException e) {
                    Log.e(TAG, Module + "can't retrieve new event directory to copy file: " + inFile, e);
                }
            } else {
                Log.w(TAG, Module + "file not available: " + inFile);
            }
        }

        // Generate event
        if (mEvent != null)
            GeneralEventGenerator.INSTANCE.generateEvent(mEvent);
    }

    /**
     * Try to retrieve file in the specified directory
     *
     * @param inputFile file of the content to retrieve
     * @param dir output directory where file is created
     * @return true on success else false
     */
    private boolean retrieveFileToDirectory(File inputFile, File dir) {
        try {
            File outFile = new File(dir, inputFile.getName());
            outFile.createNewFile();
            FileOps.copy(inputFile, outFile);
            return true;
        } catch (IOException e) {
            Log.e(TAG, Module + "copy file failed:" + inputFile, e);
        }
        return false;
    }

}
