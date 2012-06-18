
package com.intel.crashreport.logconfig;

import java.util.ArrayList;

import android.content.Context;
import android.os.Looper;

public interface IConfigServiceClient {

    public Context getContext();

    public Looper getLooper();

    public void updateAppliedConfigs(ArrayList<String> configs,boolean enabled);

    public void clientFinished();

}
