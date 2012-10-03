
package com.intel.crashreport.logconfig;

import java.util.List;

import android.content.Context;
import android.os.Looper;

import com.intel.crashreport.logconfig.bean.ConfigStatus;

public interface IConfigServiceClient {

    public Context getContext();

    public Looper getLooper();

    public void updateAppliedConfigs(List<ConfigStatus> configs);

    public void clientFinished();

}
