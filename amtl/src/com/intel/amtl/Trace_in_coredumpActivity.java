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
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;

public class Trace_in_coredumpActivity extends Activity {

    private Button button_trace_ma_coredump, button_trace_ma_artemis_coredump;
    private Button button_trace_ma_artemis_digrf_coredump, button_trace_none_coredump;
    private Button button_apply_trace_coredump;

    private ProgressDialog progressDialog;
    private TextView coredump_text;
    private int xsio_value;
    Runtime rtm=java.lang.Runtime.getRuntime();

    void writeSimple(String iout,String ival) throws IOException {
        RandomAccessFile f = new RandomAccessFile(iout, "rws");
        f.writeBytes(ival);
        f.close();
    }

    private void RebootMessage() {
        coredump_text.setVisibility(View.VISIBLE);
        coredump_text.setText("Your board need a HARDWARE reboot");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.trace_in_coredump);

        button_trace_ma_coredump = (Button) findViewById(R.id.trace_ma_coredump_button);
        button_trace_ma_artemis_coredump = (Button) findViewById(R.id.trace_ma_artemis_coredump_button);
        button_trace_ma_artemis_digrf_coredump = (Button) findViewById(R.id.trace_ma_artemis_digrf_coredump_button);
        button_trace_none_coredump = (Button) findViewById(R.id.trace_none_coredump_button);
        button_apply_trace_coredump = (Button) findViewById(R.id.apply_trace_coredump_button);
        coredump_text=(TextView) findViewById(R.id.text_coredump);

        /*Get the between instance stored values*/
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        ((CompoundButton) button_trace_ma_coredump).setChecked(preferences.getBoolean("button_trace_ma_coredump_value", false));
        ((CompoundButton) button_trace_ma_artemis_coredump).setChecked(preferences.getBoolean("button_trace_ma_artemis_coredump_value", false));
        ((CompoundButton) button_trace_ma_artemis_digrf_coredump).setChecked(preferences.getBoolean("button_trace_ma_artemis_digrf_coredump_value", false));
        ((CompoundButton) button_trace_none_coredump).setChecked(preferences.getBoolean("button_trace_none_coredump_value", true));
        xsio_value = preferences.getInt("xsio_value", 0);

        /*Listener for button_apply_trace*/
        button_apply_trace_coredump.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                progressDialog = ProgressDialog.show(Trace_in_coredumpActivity.this, "Please wait...", "Apply traces in Progress");

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (((CompoundButton) button_trace_ma_coredump).isChecked()) {
                                /*Enable OCT port*/
                                if (xsio_value == 0) {
                                    writeSimple("/dev/gsmtty1","at+xsio=2\r\n");
                                    android.os.SystemClock.sleep(1000);
                                    xsio_value = 2;
                                }
                                /*Enable MA traces*/
                                writeSimple("/dev/gsmtty1","at+trace=,115200,\"st=1,pr=1,bt=1,ap=0,db=1,lt=0,li=1,ga=0,ae=0\"\r\n");
                                android.os.SystemClock.sleep(1000);
                                writeSimple("/dev/gsmtty1","at+xsystrace=1,\"digrf=0;bb_sw=1;3g_sw=0\",\"digrf=0x00\",\"oct=4\"\r\n");
                                android.os.SystemClock.sleep(2000);
                            }  else if (((CompoundButton) button_trace_ma_artemis_coredump).isChecked()) {
                                /*Enable OCT port*/
                                if (xsio_value == 0) {
                                    writeSimple("/dev/gsmtty1","at+xsio=2\r\n");
                                    android.os.SystemClock.sleep(1000);
                                    xsio_value = 2;
                                }
                                /*Enable MA and Artemis traces*/
                                writeSimple("/dev/gsmtty1","at+trace=,115200,\"st=1,pr=1,bt=1,ap=0,db=1,lt=0,li=1,ga=0,ae=1\"\r\n");
                                android.os.SystemClock.sleep(1000);
                                writeSimple("/dev/gsmtty1","at+xsystrace=1,\"digrf=0;bb_sw=1;3g_sw=1\",\"digrf=0x00\",\"oct=4\"\r\n");
                                android.os.SystemClock.sleep(2000);
                            } else if (((CompoundButton) button_trace_ma_artemis_digrf_coredump).isChecked()) {
                                /*Enable OCT port*/
                                if (xsio_value == 0) {
                                    writeSimple("/dev/gsmtty1","at+xsio=2\r\n");
                                    android.os.SystemClock.sleep(1000);
                                    xsio_value = 2;
                                }
                                /*Enable MA and Artemis traces*/
                                writeSimple("/dev/gsmtty1","at+trace=,115200,\"st=1,pr=1,bt=1,ap=0,db=1,lt=0,li=1,ga=0,ae=1\"\r\n");
                                android.os.SystemClock.sleep(1000);
                                writeSimple("/dev/gsmtty1","at+xsystrace=1,\"digrf=1;bb_sw=1;3g_sw=1\",\"digrf=0x84\",\"oct=4\"\r\n");
                                android.os.SystemClock.sleep(2000);
                            }  else { /*Button None checked*/
                                /*Enable usb port*/
                                writeSimple("/dev/gsmtty1","at+xsio=0\r\n");
                                android.os.SystemClock.sleep(1000);
                                xsio_value = 0;
                                /*Disable trace*/
                                writeSimple("/dev/gsmtty1","at+trace=0,115200,\"st=0,pr=0,bt=0,ap=0,db=0,lt=0,li=0,ga=0,ae=0\"\r\n");
                                android.os.SystemClock.sleep(1000);
                                /*Disable xsystrace*/
                                writeSimple("/dev/gsmtty1","at+xsystrace=0\r\n");
                                android.os.SystemClock.sleep(2000);
                            }
                            progressDialog.dismiss();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
                Save_status_coredump();
                RebootMessage();
            }

        });
    }

    protected void Save_status_coredump() {
        /*Store values between instances here*/
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        /*Put the values from the UI*/
        boolean button_trace_ma_coredump_value = ((CompoundButton) button_trace_ma_coredump).isChecked();
        boolean button_trace_ma_artemis_coredump_value = ((CompoundButton) button_trace_ma_artemis_coredump).isChecked();
        boolean button_trace_ma_artemis_digrf_coredump_value = ((CompoundButton) button_trace_ma_artemis_digrf_coredump).isChecked();
        boolean button_trace_none_coredump_value = ((CompoundButton) button_trace_none_coredump).isChecked();

        /*Value to store*/
        editor.putBoolean("button_trace_ma_coredump_value", button_trace_ma_coredump_value);
        editor.putBoolean("button_trace_ma_artemis_coredump_value", button_trace_ma_artemis_coredump_value);
        editor.putBoolean("button_trace_ma_artemis_digrf_coredump_value", button_trace_ma_artemis_digrf_coredump_value);
        editor.putBoolean("button_trace_none_coredump_value", button_trace_none_coredump_value);
        editor.putInt("xsio_value", xsio_value);

        /*Commit to storage*/
        editor.commit();
    }
}
