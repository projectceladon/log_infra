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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * @brief InfoErrorEventReceiver waits for Info/Error/Stats event from other applications
 *
 * Info/Error/Stats events are send through intents which are defined in
 * {@see PDIntentConstants}. The intent is passed to {@see EventReceiverService}
 * which do the processing and the event creation.
 */
public class InfoErrorEventReceiver extends BroadcastReceiver {
    private static final String Module = "InfoErrorEventReceiver: ";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, Module + "onReceive: " + intent.toString());
        processIntent(context, intent);
    }

    private void processIntent(Context context, Intent intent) {
        if (intent != null) {
            Intent mEventReceiverService = new Intent(context, EventReceiverService.class);
            mEventReceiverService.putExtra(EventReceiverService.EXTRA_ORIGINAL_ACTION, intent.getAction());
            Bundle mExtras = intent.getExtras();
            if (mExtras != null)
                mEventReceiverService.putExtras(mExtras);
            context.startService(mEventReceiverService);
        }
    }

}
