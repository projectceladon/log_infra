/* ACS UI Recorder
 *
 * Copyright (C) Intel 2012
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Author: Julien Reynaud <julienx.reynaud@intel.com>
 */


package com.intel.acs.uirecorder;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class UiRecorderNotificationService extends Service {

    /**
     * The own <code>UiRecorderNotificationService</code> instance to use (singleton).
     */
    private static UiRecorderNotificationService M_INSTANCE = null;

    /**
     * The <code>int</code> value used to indicate
     * the <i>notification</i> to use on status update.
     * <p>
     * This is the always the same but updated.
     */
    private static final int NOTIFICATION_ID = 1;

    /**
     * The <code>String</code> value used to indicate that
     * the <i>service</i> is loging on LogCat.
     */
    private static final String TAG = "UiRecorderNotificationService";

    /**
     * The <code>String</code> value used to indicate
     * the <i>Package Name</i> to use on intent sending.
     * <p>
     * Used to go back to activity from notification.
     */
    private static final String PCK_NAME = "com.intel.acs.uirecorder";

    /**
     * The <code>String</code> value used to indicate
     * the full <i>Activity Name</i> to use on intent sending.
     * <p>
     * Used to go back to activity from notification.
     */
    private static final String ACT_NAME = PCK_NAME+".UiRecorderActivity";

    /**
     * The <code>int</code> value used to indicate
     * the current <i>Recording status</i> to use.
     */
    private static int CURRENT_STATUS = UiRecorderConstant.STATUS_UNKNOWN;

    /**
     * The <code>String</code> value used to indicate
     * the current <i>path</i> to use for recording.
     */
    private static String RECORD_PATH = null;

    /**
     * The <code>NotificationManager</code> instance to use.
     */
    private static NotificationManager mNotificationManager = null;

    @Override
        public void onCreate() {
            // TODO Auto-generated method stub
            super.onCreate();
            if(M_INSTANCE == null)
                M_INSTANCE = this;
            //Initialize notification
            this.initNotification();
        }

    /**
     * Called when destroying the service,
     * release the media player if needed
     * */
    public void onDestroy() {
        Log.i(TAG, "Reset current instance");
        M_INSTANCE = null;
        if (mNotificationManager != null){
            Log.i(TAG, "Cancelling notification ...");
            mNotificationManager.cancel(NOTIFICATION_ID);
        }
    }

    /**
     * Retrieve the unique instance of UiRecorderNotificationService
     * <p>
     * @return The UiRecorderNotificationService instance.
     * */
    public static UiRecorderNotificationService getInstance(){
        return M_INSTANCE;
    }

    @Override
        public IBinder onBind(Intent intent) {
            // TODO Auto-generated method stub
            Log.i(TAG, "Binding UiRecorderNotificationService.");
            return null;
        }

    /**
     * Gets the <i>current</i> recording status.
     * <p>
     * @return the recording status as <code>int</code>.
     */
    public int getRecordingStatus(){
        return CURRENT_STATUS;
    }

    /**
     * Sets the <i>current</i> recording status from one passed as parameter.
     * <p>
     * @param newStatus The new recording status as <code>int</code>
     */
    private void setRecordingStatus(int newStatus){
        CURRENT_STATUS = newStatus;
    }

    /**
     * Gets the <i>current</i> recording path.
     * <p>
     * @return the recording path as <code>String</code>.
     */
    public String getRecordPath(){
        return RECORD_PATH;
    }

    /**
     * Sets the <i>current</i> recording path from one passed as parameter.
     * <p>
     * @param newRecordPath The new recording path as <code>String</code>
     */
    public void setRecordPath(String newRecordPath){
        RECORD_PATH = newRecordPath;
    }

    /**
     * Initializes the Notification bar displaying a welcome message.
     */
    public void initNotification(){

        Log.i(TAG,"Initialize notification");

        if (mNotificationManager ==  null){
            mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        this.updateNotification(UiRecorderConstant.STATUS_UNKNOWN, true);
    }

    /**
     * Updates the current notification with the new status.
     * <p>
     * According if it coming from first init or an update, the ticker text can be different.
     * <p>
     * @param newStatus The new recording status as <code>int</code>
     * @param isInit <code>boolean</code> indicating update or init.
     */
    public void updateNotification(int newStatus, boolean isInit){

        Log.i(TAG,"Update notification");

        this.setRecordingStatus(newStatus);

        int icon = R.drawable.ic_stat_notification_icon;

        //If update concatenate update and new status
        String tickerText = "default ticker text";
        if(isInit){
            tickerText = getResources().getString(R.string.notification_service_started);
        }else{
            tickerText = getResources().getString( R.string.notification_service_update);
            tickerText = tickerText.concat(": ");
            tickerText = tickerText.concat(this.getStringStatusFromStatus(newStatus));
        }

        long when = System.currentTimeMillis();

        Notification notification = new Notification(icon, tickerText, when);
        notification.flags |= Notification.FLAG_NO_CLEAR; //Do not clear the notification
        notification.defaults |= Notification.DEFAULT_LIGHTS; // LED
        notification.defaults |= Notification.DEFAULT_VIBRATE; //Vibration

        Context context = getApplicationContext();

        CharSequence contentTitle = getResources().getString(R.string.recording_status_string);
        CharSequence contentText = this.getStringStatusFromStatus(CURRENT_STATUS);

        Intent notificationIntent = new Intent(this, UiRecorderNotificationService.class);
        notificationIntent.setComponent(new ComponentName(PCK_NAME, ACT_NAME));
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_SINGLE_TOP |
                Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

        notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);

        //Pass the Notification to the NotificationManager:
        mNotificationManager.notify(NOTIFICATION_ID, notification);
    }

    /**
     * Returns the string resource according to the status passed as parameter.
     * <p>
     * Possible values are set on UiRecorderConstant class.
     * @param status as <code>int</code> define the status.
     *
     * @return the status as <code>String</code>
     */
    public String getStringStatusFromStatus(int status){
        String contentText = null;
        switch (status){
            case UiRecorderConstant.STATUS_READY:
                contentText = getResources().getString(R.string.recording_status_ready);
                break;
            case UiRecorderConstant.STATUS_RECORDING:
                contentText = getResources().getString(R.string.recording_status_recording);
                break;
            case UiRecorderConstant.STATUS_REPLAYING:
                contentText = getResources().getString(R.string.recording_status_replay);
                break;
            case UiRecorderConstant.STATUS_UNKNOWN:
                contentText = getResources().getString(R.string.recording_status_unknown);
                break;
            default:
                contentText = getResources().getString(R.string.recording_status_unknown);
                break;
        }
        return contentText;
    }
}
