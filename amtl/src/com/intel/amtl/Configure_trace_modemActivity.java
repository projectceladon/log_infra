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
import java.io.RandomAccessFile;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RadioButton;
import android.widget.TextView;

public class Configure_trace_modemActivity extends Activity {
    private Button button_enable;
    private Button button_disable;
    private Button button_speed_78;
    private Button button_speed_156;
    private Button button_disable_speed;
    private Button button_apply_configure;
    private ProgressDialog progressDialog;
    private TextView configure_text;
    public static final String PREFS_NAME = "Activate_trace_modemActivity";
    private int activate_status;

    /*Send command to the modem*/
    void writeSimple(String iout,String ival) throws IOException {
        RandomAccessFile f = new RandomAccessFile(iout, "rws");
        f.writeBytes(ival);
        f.close();
    }

    /*Print a RebootMessage*/
    private void RebootMessage() {
        configure_text.setVisibility(View.VISIBLE);
        configure_text.setText("Your board need a HARDWARE reboot");
    }

    /*Print message if activate_trace_modem is running*/
    private void DisableMessage() {
        button_apply_configure.setEnabled(false);
        configure_text.setVisibility(View.VISIBLE);
        configure_text.setText("Sorry DISABLE activate_trace_modem FIRST");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.conf_trace_modem);

        button_enable = (RadioButton) findViewById(R.id.enable_configure_button);
        button_disable = (RadioButton) findViewById(R.id.disable_configure_button);
        button_speed_78 = (RadioButton) findViewById(R.id.speed_78mhz_button);
        button_speed_156 = (RadioButton) findViewById(R.id.speed_156mhz_button);
        button_disable_speed = (RadioButton) findViewById(R.id.disable_speed_conf_button);
        button_apply_configure = (Button) findViewById(R.id.apply_configure_button);
        configure_text=(TextView) findViewById(R.id.text_configure);

        /*Get the between instance stored values*/
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        ((CompoundButton) button_enable).setChecked(preferences.getBoolean("button_enable_value", false));
        ((CompoundButton) button_disable).setChecked(preferences.getBoolean("button_disable_value", true));
        ((CompoundButton) button_speed_78).setChecked(preferences.getBoolean("button_speed_78_value", false));
        ((CompoundButton) button_speed_156).setChecked(preferences.getBoolean("button_speed_156_value", false));
        ((CompoundButton) button_disable_speed).setChecked(preferences.getBoolean("button_disable_speed_value", true));

        /* Get the value of the button_disable_activate_value of activate_trace_modem */
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        activate_status = settings.getInt("service_enabled", 0);

        /*Disable others button if disable_button is checked during the loading of preferences*/
        if (((CompoundButton) button_disable).isChecked()) {
            button_speed_78.setEnabled(false);
            button_speed_156.setEnabled(false);
        }

        /*Listener on button_enable*/
        ((CompoundButton) button_enable).setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                if(isChecked) {
                    button_speed_78.setEnabled(true);
                    button_speed_156.setEnabled(true);
                    button_disable_speed.setEnabled(false);
                    ((CompoundButton) button_speed_78).setChecked(true);
                } else {
                    button_speed_78.setEnabled(false);
                    button_speed_156.setEnabled(false);
                    button_disable_speed.setEnabled(true);
                    ((CompoundButton) button_disable_speed).setChecked(true);
                }
            }
        });

        /*Listener for button_apply_configure*/
        button_apply_configure.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (activate_status != 0) {
                    /*Activate_trace_modem in running*/
                    DisableMessage();
                } else {
                    progressDialog = ProgressDialog.show(Configure_trace_modemActivity.this, "Please wait....", "Apply configuration in Progress");

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (((CompoundButton) button_enable).isChecked()) {
                                    if (((CompoundButton) button_speed_78).isChecked()) {
                                        /*Enable hsi 78MHz*/
                                        writeSimple("/dev/gsmtty1","at+xsio=4\r\n");
                                        android.os.SystemClock.sleep(1000);
                                    } else if (((CompoundButton) button_speed_156).isChecked()) {
                                        /*Enable hsi 156MHz*/
                                        writeSimple("/dev/gsmtty1","at+xsio=5\r\n");
                                        android.os.SystemClock.sleep(1000);
                                    }
                                } else {
                                    /*Disable hsi logs*/
                                    writeSimple("/dev/gsmtty1","at+xsio=0\r\n");
                                    android.os.SystemClock.sleep(1000);
                                }
                                progressDialog.dismiss();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                    Save_status_configure();
                    RebootMessage();
                }
            }
        });
    }

    protected void Save_status_configure() {
        /*Store values between instances here*/
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        /*Put the values from the UI*/
        boolean button_enable_value = ((CompoundButton) button_enable).isChecked();
        boolean button_disable_value = ((CompoundButton) button_disable).isChecked();
        boolean button_speed_78_value = ((CompoundButton) button_speed_78).isChecked();
        boolean button_speed_156_value = ((CompoundButton) button_speed_156).isChecked();
        boolean button_disable_speed_value = ((CompoundButton) button_disable_speed).isChecked();

        /*Value to store*/
        editor.putBoolean("button_enable_value", button_enable_value);
        editor.putBoolean("button_disable_value", button_disable_value);
        editor.putBoolean("button_speed_78_value", button_speed_78_value);
        editor.putBoolean("button_speed_156_value", button_speed_156_value);
        editor.putBoolean("button_disable_speed_value", button_disable_speed_value);

        /*Commit to storage*/
        editor.commit();
    }
}