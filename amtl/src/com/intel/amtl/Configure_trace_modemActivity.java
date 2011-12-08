/*
 * Copyright (C) 2009 The Android Open Source Project
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
    private Button button_trace_bb;
    private Button button_trace_bb_3g;
    private Button button_trace_bb_3g_digrf;
    private Button button_disable_trace;
    private ProgressDialog progressDialog;
    private TextView configure_text;

    void writeSimple(String iout,String ival) throws IOException {
        RandomAccessFile f = new RandomAccessFile(iout, "rws");
        f.writeBytes(ival);
        f.close();
    }

    private void RebootMessage() {
        configure_text.setVisibility(1);
        configure_text.setText("Your board need a HARDWARE reboot");
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
        button_trace_bb= (RadioButton) findViewById(R.id.trace_bb_sw_button);
        button_trace_bb_3g = (RadioButton) findViewById(R.id.trace_bb_sw_3g_sw_button);
        button_trace_bb_3g_digrf = (RadioButton) findViewById(R.id.trace_bb_sw_3g_sw_digrf_button);
        button_disable_trace = (RadioButton) findViewById(R.id.disable_trace_conf);
        configure_text=(TextView) findViewById(R.id.text_configure);

        /*Get the between instance stored values*/
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        ((CompoundButton) button_enable).setChecked(preferences.getBoolean("button_enable_value", false));
        ((CompoundButton) button_disable).setChecked(preferences.getBoolean("button_disable_value", true));
        ((CompoundButton) button_speed_78).setChecked(preferences.getBoolean("button_speed_78_value", false));
        ((CompoundButton) button_speed_156).setChecked(preferences.getBoolean("button_speed_156_value", false));
        ((CompoundButton) button_disable_speed).setChecked(preferences.getBoolean("button_disable_speed_value", true));
        ((CompoundButton) button_trace_bb).setChecked(preferences.getBoolean("button_trace_bb_value", false));
        ((CompoundButton) button_trace_bb_3g).setChecked(preferences.getBoolean("button_trace_bb_3g_value", false));
        ((CompoundButton) button_trace_bb_3g_digrf).setChecked(preferences.getBoolean("button_trace_bb_3g_digrf_value", false));
        ((CompoundButton) button_disable_trace).setChecked(preferences.getBoolean("button_disable_trace_value", true));

        /*Disable others button if disable_button is checked during the loading of preferences*/
        if (((CompoundButton) button_disable).isChecked()) {
            button_speed_78.setEnabled(false);
            button_speed_156.setEnabled(false);
            button_trace_bb.setEnabled(false);
            button_trace_bb_3g.setEnabled(false);
            button_trace_bb_3g_digrf.setEnabled(false);
        }

        /*Listener on button_enable*/
        ((CompoundButton) button_enable).setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                if(isChecked) {
                    button_speed_78.setEnabled(true);
                    button_speed_156.setEnabled(true);
                    button_disable_speed.setEnabled(false);

                    button_trace_bb.setEnabled(true);
                    button_trace_bb_3g.setEnabled(true);
                    button_trace_bb_3g_digrf.setEnabled(true);
                    button_disable_trace.setEnabled(false);

                    ((CompoundButton) button_speed_78).setChecked(true);
                    ((CompoundButton) button_trace_bb).setChecked(true);
                } else {
                    button_speed_78.setEnabled(false);
                    button_speed_156.setEnabled(false);
                    button_disable_speed.setEnabled(true);

                    button_trace_bb.setEnabled(false);
                    button_trace_bb_3g.setEnabled(false);
                    button_trace_bb_3g_digrf.setEnabled(false);
                    button_disable_trace.setEnabled(true);

                    ((CompoundButton) button_disable_speed).setChecked(true);
                    ((CompoundButton) button_disable_trace).setChecked(true);
                }
            }
        });


        /*Listener for apply button*/
        Button button_apply_configure = (Button) findViewById(R.id.apply_configure_button);
        button_apply_configure.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
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
                                    writeSimple("/dev/gsmtty1","at+trace=,115200,\"st=1,pr=1,bt=1,ap=0,db=1,lt=0,li=1,ga=0,ae=0\"\r\n");
                                    android.os.SystemClock.sleep(1000);

                                    if (((CompoundButton) button_trace_bb).isChecked()) {
                                        /*Enable first level trace*/
                                        writeSimple("/dev/gsmtty1","at+xsystrace=1,\"bb_sw=1\",,\"oct=4\"\r\n");
                                        android.os.SystemClock.sleep(2000);
                                    } else if (((CompoundButton) button_trace_bb_3g).isChecked()) {
                                        /*Enable second level trace*/
                                        writeSimple("/dev/gsmtty1","at+xsystrace=1,\"bb_sw=1;3g_sw=1\",,\"oct=4\"\r\n");
                                        android.os.SystemClock.sleep(2000);
                                    } else {
                                        /*Enable third level trace*/
                                        writeSimple("/dev/gsmtty1","at+xsystrace=1,\"digrf=1;bb_sw=1;3g_sw=1\",\"digrf=0x84\",\"oct=4\"\r\n");
                                        android.os.SystemClock.sleep(2000);
                                    }
                                } else if (((CompoundButton) button_speed_156).isChecked()) {
                                    writeSimple("/dev/gsmtty1","at+xsio=5\r\n");
                                    android.os.SystemClock.sleep(1000);
                                    writeSimple("/dev/gsmtty1","at+trace=,115200,\"st=1,pr=1,bt=1,ap=0,db=1,lt=0,li=1,ga=0,ae=0\"\r\n");
                                    android.os.SystemClock.sleep(1000);
                                    if (((CompoundButton) button_trace_bb).isChecked()) {
                                        /*Enable first level trace*/
                                        writeSimple("/dev/gsmtty1","at+xsystrace=1,\"bb_sw=1\",,\"oct=4\"\r\n");
                                        android.os.SystemClock.sleep(2000);
                                    } else if (((CompoundButton) button_trace_bb_3g).isChecked()) {
                                        /*Enable second level trace*/
                                        writeSimple("/dev/gsmtty1","at+xsystrace=1,\"bb_sw=1;3g_sw=1\",,\"oct=4\"\r\n");
                                        android.os.SystemClock.sleep(2000);
                                    } else {
                                        /*Enable third level trace*/
                                        writeSimple("/dev/gsmtty1","at+xsystrace=1,\"digrf=1;bb_sw=1;3g_sw=1\",\"digrf=0x84\",\"oct=4\"\r\n");
                                        android.os.SystemClock.sleep(2000);
                                    }
                                }
                            } else {
                                /*Disable hsi logs*/
                                writeSimple("/dev/gsmtty1","at+xsio=0\r\n");
                                android.os.SystemClock.sleep(1000);
                                /*Disable trace*/
                                writeSimple("/dev/gsmtty1","at+trace=0\"\r\n");
                                android.os.SystemClock.sleep(1000);
                                writeSimple("/dev/gsmtty1","at+xsystrace=0\"\r\n");
                                android.os.SystemClock.sleep(2000);
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
        boolean button_trace_bb_value = ((CompoundButton) button_trace_bb).isChecked();
        boolean button_trace_bb_3g_value = ((CompoundButton) button_trace_bb_3g).isChecked();
        boolean button_trace_bb_3g_digrf_value = ((CompoundButton) button_trace_bb_3g_digrf).isChecked();
        boolean button_disable_trace_value = ((CompoundButton) button_disable_trace).isChecked();

        /*Value to store*/
        editor.putBoolean("button_enable_value", button_enable_value);
        editor.putBoolean("button_disable_value", button_disable_value);
        editor.putBoolean("button_speed_78_value", button_speed_78_value);
        editor.putBoolean("button_speed_156_value", button_speed_156_value);
        editor.putBoolean("button_disable_speed_value", button_disable_speed_value);
        editor.putBoolean("button_trace_bb_value", button_trace_bb_value);
        editor.putBoolean("button_trace_bb_3g_value", button_trace_bb_3g_value);
        editor.putBoolean("button_trace_bb_3g_digrf_value", button_trace_bb_3g_digrf_value);
        editor.putBoolean("button_disable_trace_value", button_disable_trace_value);

        /*Commit to storage*/
        editor.commit();
    }
}