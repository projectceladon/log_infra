
package com.intel.crashreport.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.intel.crashreport.CustomizableEventData;
import com.intel.crashreport.EventGenerator;

public class KratosSessionInfoReceiver extends BroadcastReceiver {
    private static final String TAG = "KratosSessionInfoReceiver";

    private static final String ACTION_SESSION_INFO = "intel.intent.action.kratos.SESSION_INFO";
    private static final String EXTRA_ART_SERVER_LINK = "intel.intent.extra.kratos.ART_SERVER_LINK";

    private static final String EVENT_TYPE_KRATOS = "KRATOS";
    private static final String EVENT_DATA0_KRATOS_SESSION_UPLOAD = "Session upload";
    private static final String EVENT_LINK_TAG = "link:";
    private static final String EVENT_ERROR_NO_LINK = "No ART session link found";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive: " + intent.toString());
        processIntent(context, intent);
    }

    private void processIntent(Context context, Intent intent) {
        if (intent.getAction().contentEquals(ACTION_SESSION_INFO)) {
            String sessionARTLink = intent.getStringExtra(EXTRA_ART_SERVER_LINK);
            if (sessionARTLink == null) {
                Log.w(TAG, EVENT_ERROR_NO_LINK);
                CustomizableEventData mEvent = EventGenerator.INSTANCE.getEmptyErrorEvent();
                mEvent.setType(EVENT_TYPE_KRATOS);
                mEvent.setData0(EVENT_DATA0_KRATOS_SESSION_UPLOAD);
                mEvent.setData1(EVENT_ERROR_NO_LINK);
                EventGenerator.INSTANCE.generateEvent(mEvent);
            } else {
                CustomizableEventData mEvent = EventGenerator.INSTANCE.getEmptyInfoEvent();
                mEvent.setType(EVENT_TYPE_KRATOS);
                mEvent.setData0(EVENT_DATA0_KRATOS_SESSION_UPLOAD);
                mEvent.setData1(EVENT_LINK_TAG + sessionARTLink);
                EventGenerator.INSTANCE.generateEvent(mEvent);
            }
        }
    }

}
