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

package com.intel.crashreport.logconfig;

import com.intel.crashreport.CrashReport;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import android.os.UserHandle;

public class BootConfigServiceReceiver extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.contentEquals(intent.getAction())) {
            CrashReport app = (CrashReport)(context.getApplicationContext());
            if(!app.isUserBuild())
                context.startServiceAsUser(new Intent(context, BootConfigService.class), UserHandle.CURRENT);
        }
    }

}
