
package com.intel.crashreport.logconfig;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootConfigServiceReceiver extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().contentEquals(Intent.ACTION_BOOT_COMPLETED)) {
            context.startService(new Intent(context, BootConfigService.class));
        }
    }

}
