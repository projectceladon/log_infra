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

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RadioButton;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class Additional_Features_Activity extends Activity {
    private Button button_usb_modem;
    private Button button_usb_ape;
    private Button button_toggle_on1;
    private final char[] inputBuffer = new char[255];
    private String data;
    protected final String output_file = "usbswitch.conf";
    private ProgressDialog progressDialog;
    Runtime rtm = java.lang.Runtime.getRuntime();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.additional_features);

        button_usb_modem = (RadioButton) findViewById(R.id.additional_features_modem_btn);
        button_usb_ape = (RadioButton) findViewById(R.id.additional_features_ape_btn);
        button_toggle_on1 = (Button) findViewById(R.id.additional_features_toggle_on1_btn);

        /*Read usbswitch.conf file*/
        try {
            FileInputStream fIn = openFileInput(Main_Activity.output_file);
            InputStreamReader isr = new InputStreamReader(fIn);
            isr.read(inputBuffer);
            data = new String(inputBuffer);

            /*Set button according to usbswitch.conf file*/
            if (data.contains("1")) {
                ((CompoundButton) button_usb_modem).setChecked(true);
            } else {
                ((CompoundButton) button_usb_ape).setChecked(true);
            }
            fIn.close();
            isr.close();
        } catch (Exception e1) {
            e1.printStackTrace();
            Log.e(Modem_Configuration.TAG, "Additional_Features_Activity can't read the file usbswitch.conf");
        }

        /*Listener on button_usb_modem : USB ape to modem*/
        ((CompoundButton) button_usb_modem).setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                if (isChecked) {
                    progressDialog = ProgressDialog.show(Additional_Features_Activity.this, "Please wait....", "Usbswitch MODEM in Progress");
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                rtm.exec("start usb_to_modem");
                                android.os.SystemClock.sleep(1000);
                                progressDialog.dismiss();
                            } catch (IOException e) {
                                Log.e(Modem_Configuration.TAG, "Additional_Features_Activity can't enable usbswitch MODEM");
                                e.printStackTrace();
                            } catch (NullPointerException e) {
                                Log.v(Modem_Configuration.TAG, "Additional_Features_Activity usbswitch MODEM listener: null pointer");
                            }
                        }
                    }).start();
                }
            }
        });

        /*Listener on button_usb_ape : USB modem to ape*/
        ((CompoundButton) button_usb_ape).setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                if (isChecked) {
                    progressDialog = ProgressDialog.show(Additional_Features_Activity.this, "Please wait....", "Usbswitch APE in Progress");
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                rtm.exec("start usb_to_ape");
                                android.os.SystemClock.sleep(1000);
                                progressDialog.dismiss();
                            } catch (IOException e) {
                                Log.e(Modem_Configuration.TAG, "Additional_Features_Activity can't enable usbswitch APE");
                                e.printStackTrace();
                            } catch (NullPointerException e) {
                                Log.v(Modem_Configuration.TAG, "Additional_Features_Activity usbswitch APE listener: null pointer");
                            }
                        }
                    }).start();
                }
            }
        });

        /*Listener on button_toggle_on1*/
        button_toggle_on1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                try {
                    rtm.exec("start toggle_on1");
                    Toast toast = Toast.makeText(Additional_Features_Activity.this, "Toggle pin on1 DONE", Toast.LENGTH_LONG);
                    toast.show();
                } catch (IOException e) {
                    Log.e(Modem_Configuration.TAG, "Toggle_pin can't apply");
                    e.printStackTrace();
                }
            }
        });
    }
}
