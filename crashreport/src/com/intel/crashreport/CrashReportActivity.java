package com.intel.crashreport;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class CrashReportActivity extends PreferenceActivity {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.menu);
    }
}