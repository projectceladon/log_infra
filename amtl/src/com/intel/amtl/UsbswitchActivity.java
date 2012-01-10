/* Android Modem Traces and Logs
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
 * Author: Tony Goubert <tonyx.goubert@intel.com>
 */

package com.intel.amtl;

import java.io.IOException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;

public class UsbswitchActivity extends Activity {

    private Button usb_modem_button;
    private Button button_usb_switch;
    private ProgressDialog progressDialog;
    Runtime rtm=java.lang.Runtime.getRuntime();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.usbswitch);

        usb_modem_button = (RadioButton) findViewById(R.id.button_usb_modem);
        button_usb_switch = (Button) findViewById(R.id.button_apply_usb_switch);

        /*Get the between instance stored values*/
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        ((CompoundButton) usb_modem_button).setChecked(preferences.getBoolean("usb_modem_button_value", false));

        /*Listener for button_usb_switch*/
        button_usb_switch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                progressDialog = ProgressDialog.show(UsbswitchActivity.this, "Please wait....", "USB switch in Progress");

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (((CompoundButton) usb_modem_button).isChecked()) {
                                /*USB ape to modem*/
                                rtm.exec("stop usb_to_ape");
                                rtm.exec("start usb_to_modem");
                            } else {
                                /*USB modem to ape*/
                                rtm.exec("stop usb_to_modem");
                                rtm.exec("start usb_to_ape");
                            }
                            android.os.SystemClock.sleep(1000);
                            progressDialog.dismiss();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
                Save_status_usbswitch();
            }
        });
    }

    protected void Save_status_usbswitch() {
        /*Store values between instances here*/
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        /*Put the value from the UI*/
        boolean usb_modem_button_value = ((CompoundButton) usb_modem_button).isChecked();

        /*Value to store*/
        editor.putBoolean("usb_modem_button_value", usb_modem_button_value);

        /*Commit to storage*/
        editor.commit();
    }
}