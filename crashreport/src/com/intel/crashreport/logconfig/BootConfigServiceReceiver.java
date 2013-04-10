
package com.intel.crashreport.logconfig;

import com.intel.crashreport.CrashReport;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemProperties;

public class BootConfigServiceReceiver extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().contentEquals(Intent.ACTION_BOOT_COMPLETED)) {
            CrashReport app = (CrashReport)(context.getApplicationContext());
            if(!app.isUserBuild())
                context.startService(new Intent(context, BootConfigService.class));
        }
    }

}
