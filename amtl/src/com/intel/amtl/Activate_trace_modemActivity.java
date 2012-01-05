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
import java.io.File;
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

public class Activate_trace_modemActivity extends Activity {
    private Button button_enable_activate;
    private Button button_disable_activate;
    private Button button_disable_repeat_activate;
    private Button button_disable_save_activate;
    private Button button_disable_data_activate;
    private Button button_enable_mux;
    private Button button_disable_mux;
    private Button button_max_data;
    private Button button_emmc;
    private Button button_sdcard;
    private Button button_usb;
    private Button button_100mb;
    private Button button_800mb;
    private Button button_oneshot;
    private Button button_persistent;
    private ProgressDialog progressDialog;
    private TextView activate_text;
    private int service_enabled;
    private Button button_apply_activate;
    public static final String PREFS_NAME = "Configure_trace_modemActivity";
    private boolean configure_status;
    Runtime rtm=java.lang.Runtime.getRuntime();

    private void writeSimple(String iout,String ival) throws IOException {
        RandomAccessFile f = new RandomAccessFile(iout, "rws");
        f.writeBytes(ival);
        f.close();
    }

    /*Create repository in sdcard*/
    private void useSdDirLog() {
        File f=new File("/mnt/sdcard/data/logs/");
        f.mkdirs();
    }

    /*mts is already running, print message*/
    private void service_unavailable() {
        activate_text.setVisibility(View.VISIBLE);
        activate_text.setText("Sorry mts is already running, please stop it before");
    }

    /*configure_trace_modem not enabled*/
    private void EnableMessage() {
        button_apply_activate.setEnabled(false);
        activate_text.setVisibility(View.VISIBLE);
        activate_text.setText("Sorry ENABLE configure_trace_modem FIRST");
    }

    /*Find the service selected*/
    private int service_selected() {
        if (((CompoundButton) button_emmc).isChecked()) {
            if (((CompoundButton) button_100mb).isChecked()) {
                if (((CompoundButton) button_oneshot).isChecked()) {
                    /*emmc 100MB oneshot*/
                    return 1;
                } else {
                    /*emmc 100MB persistent*/
                    return 2;
                }
            } else { /*800MB*/
                if (((CompoundButton) button_oneshot).isChecked()) {
                    /*emmc 800MB oneshot*/
                    return 3;
                } else {
                    /*emmc 800mb persistent*/
                    return 4;
                }
            }
        } else if (((CompoundButton) button_sdcard).isChecked()) {
            useSdDirLog();
            if (((CompoundButton) button_100mb).isChecked()) {
                if (((CompoundButton) button_oneshot).isChecked()) {
                    /*sdcard 100MB oneshot*/
                    return 5;
                } else {
                    /*sdcard 100MB persistent*/
                    return 6;
                }
            } else { /*800MB*/
                if (((CompoundButton) button_oneshot).isChecked()) {
                    /*sdcard 800MB oneshot*/
                    return 7;
                } else {
                    /*sdcard 800MB persistent*/
                    return 8;
                }
            }
        } else return 9;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_trace_modem);

        button_enable_activate = (RadioButton) findViewById(R.id.enable_activate_button);
        button_disable_activate = (RadioButton) findViewById(R.id.disable_activate_button);
        button_disable_repeat_activate = (RadioButton) findViewById(R.id.disable_repeat_activate_button);
        button_disable_save_activate = (RadioButton) findViewById(R.id.disable_save_activate_button);
        button_disable_data_activate = (RadioButton) findViewById(R.id.disable_data_activate_button);
        button_enable_mux = (RadioButton) findViewById(R.id.enable_mux_button);
        button_disable_mux = (RadioButton) findViewById(R.id.disable_mux_button);
        button_max_data= (RadioButton) findViewById(R.id.max_data_activate);
        button_emmc = (RadioButton) findViewById(R.id.emmc_activate_button);
        button_sdcard = (RadioButton) findViewById(R.id.sdcard_activate_button);
        button_usb = (RadioButton) findViewById(R.id.usb_activate_button);
        button_100mb = (RadioButton) findViewById(R.id.mb_100_activate_button);
        button_800mb = (RadioButton) findViewById(R.id.mb_800_activate_button);
        button_oneshot = (RadioButton) findViewById(R.id.oneshot_button);
        button_persistent = (RadioButton) findViewById(R.id.persistent_button);
        activate_text=(TextView) findViewById(R.id.text_activate);
        button_apply_activate = (Button) findViewById(R.id.apply_activate_button);

        /*Get the between instance stored values*/
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        ((CompoundButton) button_enable_activate).setChecked(preferences.getBoolean("button_enable_activate_value", false));
        ((CompoundButton) button_disable_activate).setChecked(preferences.getBoolean("button_disable_activate_value", true));
        ((CompoundButton) button_disable_repeat_activate).setChecked(preferences.getBoolean("button_disable_repeat_activate_value", true));
        ((CompoundButton) button_disable_save_activate).setChecked(preferences.getBoolean("button_disable_save_activate_value", true));
        ((CompoundButton) button_disable_data_activate).setChecked(preferences.getBoolean("button_disable_data_activate_value", true));
        ((CompoundButton) button_emmc).setChecked(preferences.getBoolean("button_emmc_value", false));
        ((CompoundButton) button_sdcard).setChecked(preferences.getBoolean("button_sdcard_value", false));
        ((CompoundButton) button_usb).setChecked(preferences.getBoolean("button_usb_value", false));
        ((CompoundButton) button_100mb).setChecked(preferences.getBoolean("button_100mb_value", false));
        ((CompoundButton) button_800mb).setChecked(preferences.getBoolean("button_800mb_value", false));
        ((CompoundButton) button_max_data).setChecked(preferences.getBoolean("button_max_data_value", false));
        ((CompoundButton) button_oneshot).setChecked(preferences.getBoolean("button_oneshot_value", false));
        ((CompoundButton) button_persistent).setChecked(preferences.getBoolean("button_persistent_value", false));
        ((CompoundButton) button_disable_mux).setChecked(preferences.getBoolean("button_disable_mux_value", true));
        ((CompoundButton) button_enable_mux).setChecked(preferences.getBoolean("button_enable_mux_value", false));
        service_enabled = preferences.getInt("service_enabled", service_enabled);

        /* Get the value of the button_enable_value of configure_trace_modem */
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        configure_status = settings.getBoolean("button_enable_value", false);

        /*Disable others button if disable_button is checked during the loading of preferences*/
        if (((CompoundButton) button_disable_activate).isChecked()) {
            button_emmc.setEnabled(false);
            button_sdcard.setEnabled(false);
            button_usb.setEnabled(false);
            button_100mb.setEnabled(false);
            button_800mb.setEnabled(false);
            button_oneshot.setEnabled(false);
            button_persistent.setEnabled(false);
            button_max_data.setEnabled(false);
        }

        /*listener on button_enable_activate*/
        ((CompoundButton)button_enable_activate).setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                if(isChecked) {
                    button_emmc.setEnabled(true);
                    button_sdcard.setEnabled(true);
                    button_usb.setEnabled(true);
                    button_100mb.setEnabled(true);
                    button_800mb.setEnabled(true);
                    button_oneshot.setEnabled(true);
                    button_persistent.setEnabled(true);
                    ((CompoundButton) button_emmc).setChecked(true);
                    ((CompoundButton) button_800mb).setChecked(true);
                    ((CompoundButton) button_persistent).setChecked(true);
                    button_disable_save_activate.setEnabled(false);
                    button_disable_data_activate.setEnabled(false);
                    button_disable_repeat_activate.setEnabled(false);
                    button_max_data.setEnabled(false);
                    activate_text.setVisibility(View.GONE);
                } else {
                    button_emmc.setEnabled(false);
                    button_sdcard.setEnabled(false);
                    button_usb.setEnabled(false);
                    button_100mb.setEnabled(false);
                    button_800mb.setEnabled(false);
                    button_oneshot.setEnabled(false);
                    button_persistent.setEnabled(false);
                    ((CompoundButton) button_disable_save_activate).setChecked(true);
                    ((CompoundButton) button_disable_data_activate).setChecked(true);
                    ((CompoundButton) button_disable_repeat_activate).setChecked(true);
                    button_disable_save_activate.setEnabled(true);
                    button_disable_data_activate.setEnabled(true);
                    button_disable_repeat_activate.setEnabled(true);
                    button_max_data.setEnabled(false);
                    button_apply_activate.setEnabled(true);
                    activate_text.setVisibility(View.GONE);
                }
            }
        });

        /*Listener on button_usb, button_max_data and button_oneshot checked by default*/
        ((CompoundButton) button_usb).setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                if(isChecked) {
                    button_100mb.setEnabled(false);
                    button_800mb.setEnabled(false);
                    button_oneshot.setEnabled(true);
                    button_max_data.setEnabled(true);
                    ((CompoundButton) button_max_data).setChecked(true);
                    ((CompoundButton) button_oneshot).setChecked(true);
                    button_persistent.setEnabled(false);
                } else {
                    button_100mb.setEnabled(true);
                    button_800mb.setEnabled(true);
                    ((CompoundButton) button_800mb).setChecked(true);
                    button_max_data.setEnabled(false);
                    button_oneshot.setEnabled(true);
                    button_persistent.setEnabled(true);
                    ((CompoundButton) button_persistent).setChecked(true);
                }
            }
        });

        /*Listener for apply mux configuration button*/
        Button button_apply_activate = (Button) findViewById(R.id.apply_activate_button);
        button_apply_activate.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!configure_status) { /*configure_trace_modem not enabled ?*/
                    EnableMessage();
                } else if (service_enabled!=0 && (((CompoundButton) button_enable_activate).isChecked())) {
                    /*mts already running*/
                    service_unavailable();
                } else {
                    progressDialog = ProgressDialog.show(Activate_trace_modemActivity.this, "Please wait....", "Apply activate configuration in Progress");
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (((CompoundButton) button_disable_activate).isChecked()) {
                                    switch(service_enabled) {
                                    case 0: /*Already disable*/
                                        /*Nothing to do*/
                                        break;
                                    case 1: /*emmc 100MB oneshot*/
                                        service_enabled=0;
                                        rtm.exec("stop mtsfs");
                                        break;
                                    case 2: /*emmc 100MB persistent*/
                                        rtm.exec("setprop persist.service.mtsfs.enable 0");
                                        service_enabled=0;
                                        break;
                                    case 3: /*emmc 800MB oneshot*/
                                        rtm.exec("stop mtsextfs");
                                        service_enabled=0;
                                        break;
                                    case 4: /*emmc 800MB persistent*/
                                        rtm.exec("setprop persist.service.mtsextfs.enable 0");
                                        service_enabled=0;
                                        break;
                                    case 5: /*sdcard 100MB oneshot*/
                                        rtm.exec("stop mtssd");
                                        service_enabled=0;
                                        break;
                                    case 6: /*sdcard 100MB persistent*/
                                        rtm.exec("setprop persist.service.mtssd.enable 0");
                                        service_enabled=0;
                                        break;
                                    case 7: /*sdcard 800MB oneshot*/
                                        rtm.exec("stop mtsextsd");
                                        service_enabled=0;
                                        break;
                                    case 8: /*sdcard 800MB persistent*/
                                        rtm.exec("setprop persist.service.mtsextsd.enable 0");
                                        service_enabled=0;
                                        break;
                                    case 9: /*USB oneshot*/
                                        rtm.exec("stop mtsusb");
                                        service_enabled=0;
                                        break;
                                    default:
                                        service_enabled=0;
                                    }
                                    android.os.SystemClock.sleep(1000);
                                    Save_status_activate();
                                } else {
                                    /*Update value of service_enabled*/
                                    service_enabled=service_selected();

                                    /*enable the service*/
                                    switch(service_enabled) {
                                    case 1: /*emmc 100MB oneshot*/
                                        rtm.exec("start mtsfs");
                                        break;
                                    case 2: /*emmc 100MB persistent*/
                                        rtm.exec("setprop persist.service.mtsfs.enable 1");
                                        break;
                                    case 3: /*emmc 800MB oneshot*/
                                        rtm.exec("start mtsextfs");
                                        break;
                                    case 4: /*emmc 800MB persistent*/
                                        rtm.exec("setprop persist.service.mtsextfs.enable 1");
                                        break;
                                    case 5: /*sdcard 100MB oneshot*/
                                        rtm.exec("start mtssd");
                                        break;
                                    case 6: /*sdcard 100MB persistent*/
                                        rtm.exec("setprop persist.service.mtssd.enable 1");
                                        break;
                                    case 7: /*sdcard 800MB oneshot*/
                                        rtm.exec("start mtsextsd");
                                        break;
                                    case 8: /*sdcard 800MB persistent*/
                                        rtm.exec("setprop persist.service.mtsextsd.enable 1");
                                        break;
                                    case 9: /*/USB oneshot*/
                                        rtm.exec("start mtsusb");
                                        break;
                                    default:
                                        service_enabled=0;
                                    }
                                    android.os.SystemClock.sleep(1000);
                                    Save_status_activate();
                                }
                                progressDialog.dismiss();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }
            }
        });

        /*Listener for apply mux configuration button*/
        Button button_apply_mux = (Button) findViewById(R.id.apply_mux_button);
        button_apply_mux.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                final Button button_enable_mux = (RadioButton) findViewById(R.id.enable_mux_button);
                progressDialog = ProgressDialog.show(Activate_trace_modemActivity.this, "Please wait....", "Apply mux configuration in Progress");

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (((CompoundButton) button_enable_mux).isChecked()) {
                                writeSimple("/dev/gsmtty1","at+xmux=1,3,262143\r\n");
                                android.os.SystemClock.sleep(1000);
                            } else {
                                writeSimple("/dev/gsmtty1","at+xmux=1,0\r\n");
                                android.os.SystemClock.sleep(1000);
                            }
                            progressDialog.dismiss();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
                Save_status_activate();
            }
        });
    }

    protected void Save_status_activate() {
        /*Store values between instances here*/
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        /*Put the values from the UI*/
        boolean button_enable_activate_value = ((CompoundButton) button_enable_activate).isChecked();
        boolean button_disable_activate_value = ((CompoundButton) button_disable_activate).isChecked();
        boolean button_disable_repeat_activate_value = ((CompoundButton) button_disable_repeat_activate).isChecked();
        boolean button_disable_save_activate_value = ((CompoundButton) button_disable_save_activate).isChecked();
        boolean button_disable_data_activate_value = ((CompoundButton) button_disable_data_activate).isChecked();
        boolean button_emmc_value = ((CompoundButton) button_emmc).isChecked();
        boolean button_sdcard_value = ((CompoundButton) button_sdcard).isChecked();
        boolean button_usb_value = ((CompoundButton) button_usb).isChecked();
        boolean button_100mb_value = ((CompoundButton) button_100mb).isChecked();
        boolean button_800mb_value = ((CompoundButton) button_800mb).isChecked();
        boolean button_max_data_value = ((CompoundButton) button_max_data).isChecked();
        boolean button_oneshot_value = ((CompoundButton) button_oneshot).isChecked();
        boolean button_persistent_value = ((CompoundButton) button_persistent).isChecked();
        boolean button_disable_mux_value = ((CompoundButton) button_disable_mux).isChecked();
        boolean button_enable_mux_value = ((CompoundButton) button_enable_mux).isChecked();

        /*Values to store*/
        editor.putBoolean("button_enable_activate_value", button_enable_activate_value);
        editor.putBoolean("button_disable_activate_value", button_disable_activate_value);
        editor.putBoolean("button_disable_repeat_activate_value", button_disable_repeat_activate_value);
        editor.putBoolean("button_disable_save_activate_value", button_disable_save_activate_value);
        editor.putBoolean("button_disable_data_activate_value", button_disable_data_activate_value);
        editor.putBoolean("button_emmc_value", button_emmc_value);
        editor.putBoolean("button_sdcard_value", button_sdcard_value);
        editor.putBoolean("button_usb_value", button_usb_value);
        editor.putBoolean("button_100mb_value", button_100mb_value);
        editor.putBoolean("button_800mb_value", button_800mb_value);
        editor.putBoolean("button_max_data_value", button_max_data_value);
        editor.putBoolean("button_oneshot_value", button_oneshot_value);
        editor.putBoolean("button_persistent_value", button_persistent_value);
        editor.putBoolean("button_disable_mux_value", button_disable_mux_value);
        editor.putBoolean("button_enable_mux_value", button_enable_mux_value);
        editor.putInt("service_enabled", service_enabled);

        /*Commit to storage*/
        editor.commit();
    }
}
