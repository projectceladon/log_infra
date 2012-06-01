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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author intel
 *
 */
public class UiRecorderActivity extends Activity {

    private static final String TAG = "AcsUiRecorderActivity";
    // Menus ID
    public static final int MENUABOUT = Menu.FIRST + 1;
    public static final int MENUQUIT = Menu.FIRST + 2;

    public static final String ACS_UI_SERVICE = "AcsUiService";

    /** Called when the activity is first created. */
    @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            Log.d(TAG, "*** onCreate: ***" + savedInstanceState );
            Log.d(TAG, "CurrentThread Id: " + Thread.currentThread().getId());

            setContentView(R.layout.main);
            this.checkDisclaimerDisplaying();

            // Add a listener on each button in current layout
            ((Button) findViewById(R.id.button_start)).setOnClickListener(eClickStart);
            ((Button) findViewById(R.id.button_stop)).setOnClickListener(eClickStop);
            ((Button) findViewById(R.id.button_replay)).setOnClickListener(eClickReplay);

            // Get the UiRecorderNotificationService instance.
            UiRecorderNotificationService uiNotifService = UiRecorderNotificationService.getInstance();

            if (uiNotifService ==  null){
                this.startNotificationService();
                //Set default value to unknown on textView, don't bother in service because first start
                this.updateRecordingStatus(UiRecorderConstant.STATUS_UNKNOWN, false);
                // Update the base path if no yet define
                this.updateBasePathTextView(null);
            }else{
                //Update the recording status displayed
                this.updateRecordingStatus(uiNotifService.getRecordingStatus(), false);
                //Update the base path from previous one if recording
                this.updateBasePathTextView(uiNotifService.getRecordPath());
                //Update button accessibility according current status
                this.updateButtonAccessibility();
            }
        }

    /**
     * This listener is called when user click on button record button
     */
    Button.OnClickListener eClickStart = new Button.OnClickListener() {
        public void onClick(View v) {
            runStartRecording();
        }
    };

    /**
     * This listener is called when user click on button stop button
     */
    Button.OnClickListener eClickStop = new Button.OnClickListener() {
        public void onClick(View v) {
            runStopRecording();
        }
    };

    /**
     * This listener is called when user click on button replay button
     */
    Button.OnClickListener eClickReplay = new Button.OnClickListener() {
        public void onClick(View v) {
            runReplayRecord();
        }
    };

    /**
     * Start the <i>recording</i> to the folder specified on the <code>EditText</code>
     */
    private void runStartRecording(){

        String path = ((EditText)findViewById(R.id.basepath)).getText().toString();
        String message = getResources().getString(R.string.record_started_on)+" "+path;

        Log.i(TAG, "Start recording on '"+path+"'");

        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();

        //Start recording from low level service
        if(!this.runStartOnService(path)){
            Log.e(TAG, "An error occured, nothing to do in current activity !");
            return;
        }

        //Update the path into the service
        this.updateBasePathOnService(path);

        //Update the recording status on text View and on service
        this.updateRecordingStatus(UiRecorderConstant.STATUS_RECORDING, true);

        //Update button accessibility because recording status changed
        this.updateButtonAccessibility();

        //Close current activity
        this.finish();
    };

    /**
     * Stop the <i>recording</i> add generate a new default folder on the <code>EditText</code>
     */
    private void runStopRecording(){

        // Get the UiRecorderNotificationService instance.
        UiRecorderNotificationService uiNotifService = UiRecorderNotificationService.getInstance();

        //Stop recording from low level service
        if(!this.runStopOnService()){
            Log.e(TAG, "An error occured, nothing to do in current activity !");
            return;
        }

        Log.i(TAG, "Stop recording, logs available on '"+uiNotifService.getRecordPath()+"'");

        Toast.makeText(getApplicationContext(), R.string.record_stopped, Toast.LENGTH_SHORT).show();

        //Update the path into the Notification Service
        this.updateBasePathOnService(null);

        //Update the base path on text View because new log
        this.updateBasePathTextView(null);

        //Update the recording status on text View and on service
        this.updateRecordingStatus(UiRecorderConstant.STATUS_READY, true);

        //Update button accessibility because recording status changed
        this.updateButtonAccessibility();
    }

    /**
     * Start the <i>replay</i> from the folder specified on the <code>EditText</code>
     */
    private void runReplayRecord(){

        String path = ((EditText)findViewById(R.id.basepath)).getText().toString();
        String message = getResources().getString(R.string.replay_started_from)+" "+path;

        Log.i(TAG, "Start replay from '"+path+"'");

        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();

        //Start replay from low level service
        if(!this.runReplayOnService(path)){
            Log.e(TAG, "An error occured, nothing to do in current activity !");
            return;
        }

        //Update the path into the service
        this.updateBasePathOnService(path);

        //Update the recording status on text View and on service
        this.updateRecordingStatus(UiRecorderConstant.STATUS_REPLAYING, true);

        //Update button accessibility because recording status changed
        this.updateButtonAccessibility();

        //Close current activity
        this.finish();
    };


    /**
     * Update the <i>recording status</i> <code>TextView</code> color according its value.
     */
    private void updateRecordingStatusColor(){

        //Get the current status
        String currentStatus = ((TextView)findViewById(R.id.recording_status)).getText().toString();

        if (currentStatus.equals(getResources().getString(R.string.recording_status_ready))){
            ((TextView)findViewById(R.id.recording_status)).setTextColor(Color.GREEN);
        }else if (currentStatus.equals(getResources().getString(R.string.recording_status_recording))){
            ((TextView)findViewById(R.id.recording_status)).setTextColor(Color.RED);
        }else if (currentStatus.equals(getResources().getString(R.string.recording_status_replay))){
            ((TextView)findViewById(R.id.recording_status)).setTextColor(Color.YELLOW);
        }else if (currentStatus.equals(getResources().getString(R.string.recording_status_unknown))){
            ((TextView)findViewById(R.id.recording_status)).setTextColor(Color.BLUE);
        }else{
            ((TextView)findViewById(R.id.recording_status)).setTextColor(Color.GRAY);
        }
    };

    /**
     * Update the <i>recording status</i> with one passed as parameter.
     * Update the recording status on the textView and on Service if necessary.
     * <p>
     *
     * @param newStatus as <code>Int</code> from defined resource file.
     * @param updateOnService as <code>Boolean</code> to check if an update TO service is requested.
     * (Eg. when restart activity we have to update textview <u>from</u> value stored in service,
     * we <u>not</u> have to update value into service).
     */
    private void updateRecordingStatus(int newStatus, boolean updateOnService){

        if (updateOnService){
            Log.i(TAG, "Update Recording Status on Service");
            // Get the UiRecorderNotificationService instance.
            UiRecorderNotificationService uiNotifService = UiRecorderNotificationService.getInstance();
            //Update recording status into the service
            if (uiNotifService != null) {

                if (uiNotifService.getRecordingStatus() == newStatus){
                    Log.w(TAG, "Already on requested status");
                }else{
                    uiNotifService.updateNotification(newStatus, false);
                }
            } else {
                Log.e(TAG,"Unable to update status on service because it not started.");
            }
        }

        Log.i(TAG, "Update Recording Status on TextView");
        //Update recording status into the activity
        String newStatusString = "";
        switch (newStatus){
            case UiRecorderConstant.STATUS_READY:
                newStatusString = getResources().getString(R.string.recording_status_ready);
                break;
            case UiRecorderConstant.STATUS_RECORDING:
                newStatusString = getResources().getString(R.string.recording_status_recording);
                break;
            case UiRecorderConstant.STATUS_REPLAYING:
                newStatusString = getResources().getString(R.string.recording_status_replay);
                break;
            case UiRecorderConstant.STATUS_UNKNOWN:
                newStatusString = getResources().getString(R.string.recording_status_unknown);
                break;
            default:
                newStatusString = getResources().getString(R.string.recording_status_unknown);
                break;
        }

        ((TextView)findViewById(R.id.recording_status)).setText(newStatusString);
        this.updateRecordingStatusColor();
    }


    /**
     * Generates a <i>default folder name</i> as base path.
     * <br>
     * Generate it from current date with format <code>MMddyyyy_hhmmss</code>
     *
     * @return <code>String</code> corresponding to the generated folder name.
     */
    private String generateFolderDateName(){
        String sdcard_path = Environment.getExternalStorageDirectory().toString();
        String currentDateTimeString = new SimpleDateFormat("hss").format(new Date());
        return sdcard_path+'/'+currentDateTimeString;
    }

    /**
     * Update the <i>Base Path</i> show in the textView with one passed as parameter.
     * <p>
     *
     * @param newFolderDateNameString as <code>String</code> for destination folder.
     * @return <code>void</code>
     */
    private void updateBasePathTextView(String newFolderDateNameString){

        if (newFolderDateNameString == null){
            newFolderDateNameString = this.generateFolderDateName();
        }
        ((EditText)findViewById(R.id.basepath)).setText(newFolderDateNameString);
    }
    /**
     * Update the <i>Base Path</i> in the service with one passed as parameter.
     * <p>
     * @param newFolderDateNameString as <code>String</code> for destination folder.
     * <p>
     * @return <code>Boolean</code> indicating operation succeed or not.
     */
    private boolean updateBasePathOnService(String newFolderDateNameString){

        // Get the UiRecorderNotificationService instance.
        UiRecorderNotificationService uiNotifService = UiRecorderNotificationService.getInstance();

        if (uiNotifService != null) {
            uiNotifService.setRecordPath(newFolderDateNameString);
            return true;
        } else {
            Log.e(TAG,"Unable to update base path on service because notification service is not started.");
            return false;
        }
    }

    /**
     * Starts the <i>Notification Service</i> .
     * <br>
     * @return <code>boolean</code> indicating operation succeed or not.
     */
    private boolean startNotificationService(){

        Log.i(TAG, "Start notification service");
        // Get the UiRecorderNotificationService instance.
        UiRecorderNotificationService uiNotifService = UiRecorderNotificationService.getInstance();

        if (uiNotifService != null){
            Log.v(TAG, "UiRecorderNotificationService already started !");
            return true;
        }else{
            Log.v(TAG, "UiRecorderNotificationService not yet started, start it ...");
            Intent uiIntent = new Intent(this, UiRecorderNotificationService.class);
            startService(uiIntent);
            ComponentName startResult = startService(uiIntent);
            if(startResult != null) {
                Log.i(TAG, "Notification service started successfully !");
                uiNotifService = UiRecorderNotificationService.getInstance();
                return true;
            } else {
                Log.e(TAG,"Unable to start notification service on time.");
                return false;
            }
        }
    }

    /**
     * Stop the <i>Notification Service</i> .
     * <br>
     * @return <code>Boolean</code> indicating operation succeed or not.
     */
    private boolean stopNotificationService(){

        Log.i(TAG, "Stoppping notification service");

        if (UiRecorderNotificationService.getInstance() == null){
            Log.v(TAG, "UiRecorderNotificationService not started !");
            return true;
        }else{
            Log.v(TAG, "UiRecorderNotificationService not yet stopped, stop it ...");
            Intent uiIntent = new Intent(this, UiRecorderNotificationService.class);
            boolean stopResult = stopService(uiIntent);
            if(stopResult){
                Log.i(TAG, "Notification service stopped successfully !");
                return true;
            } else {
                Log.e(TAG, "Can't stop Notification service");
                return false;
            }
        }
    }

    /**
     * Calls the <i>start</i> action on the Acs Ui Service.
     * <p>
     * @return <code>Boolean</code> indicating operation succeed or not.
     */
    private boolean runStartOnService(String path){

        Log.i(TAG, "*** Start runStartOnService ***");

        Log.d(TAG, "Retrieve binder ...");
        UiRecorderBinder myBinder = new UiRecorderBinder();

        Log.d(TAG, "Attaching service ...");
        if (myBinder.AttachService(ACS_UI_SERVICE) == null){
            Log.e(TAG, "Unable to attache service");
            return false;
        }
        try {
            Log.i(TAG, "Start record on service ...");
            int parcelReturn = myBinder.record(path);

            if (parcelReturn == 0){
                Log.i(TAG, "Start recording success ["+String.valueOf(parcelReturn)+"]");
                return true;
            }else{
                Log.e(TAG, "An error occur during start recording ! ["+String.valueOf(parcelReturn)+"]");
                return false;
            }
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }
    }


    /**
     * run the <i>stop Action</i> on the Acs Ui Service.
     * <br>
     * @return <code>Boolean</code> indicating operation succeed or not.
     */
    private boolean runStopOnService(){

        Log.i(TAG, "*** Start runStopOnService ***");

        Log.d(TAG, "Retrieve binder ...");
        UiRecorderBinder myBinder = new UiRecorderBinder();

        Log.d(TAG, "Attaching service ...");
        if (myBinder.AttachService(ACS_UI_SERVICE) == null){
            Log.e(TAG, "Unable to attache service");
            return false;
        }
        try {
            Log.i(TAG, "Start record service ...");
            int parcelReturn = myBinder.stop();

            if (parcelReturn == 0){
                Log.i(TAG, "Stop recording success ["+String.valueOf(parcelReturn)+"]");
                return true;
            }else{
                Log.e(TAG, "An error occur during stop recording ! ["+String.valueOf(parcelReturn)+"]");
                return false;
            }
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Calls the <i>replay</i> action on the Acs Ui Service.
     * <br>
     * @return <code>Boolean</code> indicating operation succeed or not.
     */
    private boolean runReplayOnService(String path){

        Log.i(TAG, "*** Start runReplayOnService ***");

        Log.d(TAG, "Retrieve binder ...");
        UiRecorderBinder myBinder = new UiRecorderBinder();

        Log.d(TAG, "Attaching service ...");
        if (myBinder.AttachService(ACS_UI_SERVICE) == null){
            Log.e(TAG, "Unable to attache service");
            return false;
        }
        try {
            Log.i(TAG, "Start replay on service ...");
            int parcelReturn = myBinder.replay(path);

            if (parcelReturn == 0){
                Log.i(TAG, "Start replay success ["+String.valueOf(parcelReturn)+"]");
                return true;
            }else{
                Log.e(TAG, "An error occur during start replaying ! ["+String.valueOf(parcelReturn)+"]");
                return false;
            }
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Update the <i>buttons accessibility</i> according the current status.
     * <p>
     * <ul>
     * <li>The buttons "start" and "replay" should be disabled when <i>recording</i>.</li>
     * <li>The button "stop" should be disabled when <i>ready</i>.</li>
     * <li>The buttons "start" and "stop" should be disabled when <i>replaying</i>.</li>
     * </ul>
     * </p>
     * @return <code>void</code>
     */
    private void updateButtonAccessibility() {

        Log.i(TAG, "Update button accessibility");
        // Get the UiRecorderNotificationService instance.
        UiRecorderNotificationService uiNotifService = UiRecorderNotificationService.getInstance();

        if (uiNotifService == null){
            //Nothing to do
            return;
        }else{

            int currentStatus = uiNotifService.getRecordingStatus();
            //Update button enabling according current status
            switch (currentStatus){
                case UiRecorderConstant.STATUS_READY:
                    ((Button) findViewById(R.id.button_start)).setEnabled(true);
                    ((Button) findViewById(R.id.button_stop)).setEnabled(false);
                    ((Button) findViewById(R.id.button_replay)).setEnabled(true);
                    break;
                case UiRecorderConstant.STATUS_RECORDING:
                    ((Button) findViewById(R.id.button_start)).setEnabled(false);
                    ((Button) findViewById(R.id.button_stop)).setEnabled(true);
                    ((Button) findViewById(R.id.button_replay)).setEnabled(false);
                    break;
                case UiRecorderConstant.STATUS_REPLAYING:
                    ((Button) findViewById(R.id.button_start)).setEnabled(false);
                    ((Button) findViewById(R.id.button_stop)).setEnabled(true);
                    ((Button) findViewById(R.id.button_replay)).setEnabled(false);
                    break;
                case UiRecorderConstant.STATUS_UNKNOWN:
                    ((Button) findViewById(R.id.button_start)).setEnabled(true);
                    ((Button) findViewById(R.id.button_stop)).setEnabled(true);
                    ((Button) findViewById(R.id.button_replay)).setEnabled(true);
                    break;
                default:
                    ((Button) findViewById(R.id.button_start)).setEnabled(false);
                    ((Button) findViewById(R.id.button_stop)).setEnabled(false);
                    ((Button) findViewById(R.id.button_replay)).setEnabled(false);
                    break;
            }
        }
    }


    /* (non-Javadoc)
     * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
     */
    @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            super.onCreateOptionsMenu(menu);
            menu.add(Menu.NONE,MENUABOUT, Menu.NONE, R.string.about);
            menu.add(Menu.NONE,MENUQUIT, Menu.NONE, R.string.quit);
            return true;
        }

    /* (non-Javadoc)
     * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
     */
    @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            switch (item.getItemId()) {
                case MENUABOUT:
                    this.showAboutDialog();
                    return true;
                case MENUQUIT:
                    this.showQuitDialog();
            }
            return false;
        }

    /**
     * Shows the <i>Quit</i> dialog box.
     * <p>
     * Propose:
     * <p>
     * <ul>
     * <li>Yes: The application will close, stopping current record.</li>
     * <li>No: The application will go back to previous status.</li>
     * </ul>
     * </p>
     * @return <code>void</code>
     */
    private void showQuitDialog(){

        Log.i(TAG, "Show Quit Dialog box");

        new AlertDialog.Builder(this)
            .setTitle(R.string.quit)
            .setMessage(R.string.quit_question)
            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    stopNotificationService();
                    finish();
                }
            })
        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getApplicationContext(),
                    R.string.quit_abort,
                    Toast.LENGTH_SHORT).show();
                dialog.cancel();
            }
        })
        .show();
    }

    /**
     * Shows the <i>About</i> dialog box.
     * <p>
     * Propose:
     * <p>
     * <ul>
     * <li>Ok: The application will go back to previous status.</li>
     * </ul>
     * </p>
     * @return <code>void</code>
     */
    private void showAboutDialog(){

        Log.i(TAG, "Show About Dialog box");
        //Retrieve the copyright from raw resource
        InputStream inputStream = getResources().openRawResource(R.raw.copyright);
        String sCopyright = this.builStringFromInputStream(inputStream);

        new AlertDialog.Builder(this)
            .setTitle(R.string.about)
            .setMessage(sCopyright)
            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            })
        .show();
    }
    /**
     * Checks whether the <i>Disclaimer</i> should be displayed or not according sharedpref.
     * <p>
     * @return <code>void</code>
     */
    private void checkDisclaimerDisplaying(){
        SharedPreferences settings;
        settings = getSharedPreferences(
                UiRecorderConstant.ACS_UI_REC_SHARED_PREFERENCES, 0);

        if (!settings.getBoolean(UiRecorderConstant.DISCLAIMER_ACCEPTED, false)) {
            Log.i(TAG, "Disclaimer not yet accepted, showing it");
            this.showDisclaimer();
        }
    }

    /**
     * Shows the <i>Disclaimer</i> dialog box.
     * <p>
     * Propose:
     * <p>
     * <ul>
     * <li>Accept: The application will continue.</li>
     * <li>Decline: The application will close aborting current operation.</li>
     * </ul>
     * </p>
     * @return <code>void</code>
     */
    private void showDisclaimer(){

        Log.i(TAG, "Showing Disclaimer Dialog box");
        //Retrieve the copyright from raw resource
        InputStream inputStream = getResources().openRawResource(R.raw.disclaimer);
        String sDisclaimer = this.builStringFromInputStream(inputStream);

        new AlertDialog.Builder(this)
            .setTitle(R.string.disclamer_title)
            .setMessage(sDisclaimer)
            .setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Log.w(TAG, "Disclaimer accepted, welcome");
                    //If accept, update SharedPreferences
                    SharedPreferences settings = getSharedPreferences(
                        UiRecorderConstant.ACS_UI_REC_SHARED_PREFERENCES, 0);
                    Editor editor = settings.edit();
                    editor.putBoolean(UiRecorderConstant.DISCLAIMER_ACCEPTED, true);
                    editor.commit();

                }
            })
        .setNegativeButton(R.string.decline, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getApplicationContext(),
                    R.string.disclamer_abort,
                    Toast.LENGTH_SHORT).show();
                Log.w(TAG, "Disclaimer declined, force closing application");
                stopNotificationService();
                finish();
            }
        })
        .show();
    }

    /**
     * Creates a <i>String</i> from input stream defined
     * on the given values.
     *
     * @param inputStream The <code>InputStream</code> to be used.
     *
     * @return String corresponding to the content of the InputStream
     */
    private String builStringFromInputStream(InputStream inputStream){

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int i;
        try {
            i = inputStream.read();
            while (i != -1) {
                byteArrayOutputStream.write(i);
                i = inputStream.read();
            }
            inputStream.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
        }
        return byteArrayOutputStream.toString();
    }
}
