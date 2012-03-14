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
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RadioButton;

import java.io.IOException;

public class Settings_Activity extends Activity {
    private Button button_location_emmc;
    private Button button_location_sdcard;
    private Button button_location_coredump;
    private Button button_location_usb_ape;
    private Button button_location_usb_modem;
    private Button button_level_bb;
    private Button button_level_bb_3g;
    private Button button_level_bb_3g_digrf;
    private Button button_level_no_trace;
    private Button button_trace_size_100;
    private Button button_trace_size_800;
    private Button button_trace_size_disable;
    private Button button_hsi_frequencies_78;
    private Button button_hsi_frequencies_156;
    private Button button_hsi_frequencies_coredump;
    private Button button_hsi_frequencies_disable;
    protected int current_modem_reboot;
    private int current_service_value;
    private int current_trace_level_value;
    private int current_xsio_value;
    private int current_mux_value;
    private boolean mux_status;
    private boolean activate_status;
    private CheckBox checkbox_activate;
    private CheckBox checkbox_mux;
    private ProgressDialog progressDialog;
    private Modem_Configuration modem_configuration;
    private Settings_Activity settings_activity;
    private Services services;
    protected Modem_Application modem_application;

    /*Trace file size not useful in coredump, usb ape and usb modem cases*/
    private void unset_trace_file_size() {
        button_trace_size_100.setEnabled(false);
        button_trace_size_800.setEnabled(false);
        ((CompoundButton) button_trace_size_disable).setChecked(true);
        button_hsi_frequencies_78.setEnabled(false);
        button_hsi_frequencies_156.setEnabled(false);
        if (((CompoundButton) button_level_no_trace).isChecked()) {
            ((CompoundButton) button_level_bb_3g).setChecked(true);
        }
    }

    /*Trace file size is useful in EMMC and SDCARD cases*/
    private void set_trace_file_size() {
        button_trace_size_100.setEnabled(true);
        button_trace_size_800.setEnabled(true);
        ((CompoundButton) button_trace_size_800).setChecked(true);
        button_hsi_frequencies_78.setEnabled(true);
        button_hsi_frequencies_156.setEnabled(true);
        if (((CompoundButton) button_hsi_frequencies_disable).isChecked() || ((CompoundButton) button_hsi_frequencies_coredump).isChecked()) {
            ((CompoundButton) button_hsi_frequencies_78).setChecked(true);
        }
        if (((CompoundButton) button_level_no_trace).isChecked()) {
            ((CompoundButton) button_level_bb_3g).setChecked(true);
        }
    }

    /*Find the service selected by the user*/
    private int service_selected() {
        if (((CompoundButton) button_location_emmc).isChecked()) {
            if (((CompoundButton) button_trace_size_100).isChecked()) {
                /*emmc 100MB persistent*/
                return Modem_Configuration.mtsfs_persistent;
            } else { /*800MB*/
                /*emmc 800mb persistent*/
                return Modem_Configuration.mtsextfs_persistent;
            }
        } else if (((CompoundButton) button_location_sdcard).isChecked()) {
            if (((CompoundButton) button_trace_size_100).isChecked()) {
                /*sdcard 100MB persistent*/
                return Modem_Configuration.mtssd_persistent;
            } else { /*800MB*/
                /*sdcard 800MB persistent*/
                return Modem_Configuration.mtsextsd_persistent;
            }
        } else if (((CompoundButton) button_location_usb_ape).isChecked()) {
            /*USB max oneshot*/
            return Modem_Configuration.mtsusb;
        } else {
            /*Disable, service not necessary*/
            return Modem_Configuration.mts_disable;
        }
    }

    /*Find the frequency selected by user*/
    private int xsio_selected() {
        if (((CompoundButton) button_hsi_frequencies_78).isChecked()) {
            return Modem_Configuration.xsio4;
        } else if (((CompoundButton) button_hsi_frequencies_156).isChecked()) {
            return Modem_Configuration.xsio5;
        } else if (((CompoundButton) button_hsi_frequencies_coredump).isChecked()) {
            return Modem_Configuration.xsio2;
        } else {
            return Modem_Configuration.xsio0;
        }
    }

    /*Return the value of the button checked, useful for trace and xsystrace*/
    private int trace_level_selected() {
        if (((CompoundButton) button_level_bb).isChecked()) {
            return Modem_Configuration.trace_bb;
        } else if (((CompoundButton) button_level_bb_3g).isChecked()) {
            return Modem_Configuration.trace_bb_3g;
        } else if (((CompoundButton) button_level_bb_3g_digrf).isChecked()) {
            return Modem_Configuration.trace_bb_3g_digrf;
        } else {
            return Modem_Configuration.trace_disable;
        }
    }

    /*Update MUX checkbox*/
    private void set_checkbox_mux(int mux_value) {
        if (mux_value == modem_configuration.mux_enable) {
            /*mux_enable*/
            ((CompoundButton) checkbox_mux).setChecked(true);
        } else {
            /*mux_disable*/
            ((CompoundButton) checkbox_mux).setChecked(false);
        }
    }

    /*Set trace level buttons according the current configuration*/
    private void set_level_buttons(int trace_level_val) {
        switch(trace_level_val) {
        case Modem_Configuration.trace_disable:
            ((CompoundButton) button_level_no_trace).setChecked(true);
            break;
        case Modem_Configuration.trace_bb:
            ((CompoundButton) button_level_bb).setChecked(true);
            break;
        case Modem_Configuration.trace_bb_3g:
            ((CompoundButton) button_level_bb_3g).setChecked(true);
            break;
        case Modem_Configuration.trace_bb_3g_digrf:
            ((CompoundButton) button_level_bb_3g_digrf).setChecked(true);
            break;
        default:
            /*Nothing to do*/
        }
    }

    /*Set location and size buttons according the current configuration*/
    private void set_location_size_hsi_buttons(int modem_val, int service_val, int trace_level_val) {
        if ((modem_val == Modem_Configuration.reboot_ok0) || (modem_val == Modem_Configuration.reboot_ko0)) {

            if (service_val == Modem_Configuration.mts_disable) {
                /*The only way to know that we are in trace disabled OR USB modem is to check the trace_level_val*/
                if (trace_level_val == Modem_Configuration.trace_disable) {
                    /*Traces are not running*/
                    settings_activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((CompoundButton) button_location_coredump).setChecked(true);
                            ((CompoundButton) button_level_bb_3g).setChecked(true);
                            message_pop_up("INFO","Currently, traces are disabled.\n" +
                                    "We suggest you a default configuration.\n" +
                                    "Don't forget to confirm with ACTIVATE checkbox");
                        }
                    });
                } else if (trace_level_val == Modem_Configuration.trace_bb) {
                    /*USB modem with bb_sw traces*/
                    ((CompoundButton) button_location_usb_modem).setChecked(true);
                    ((CompoundButton) button_trace_size_disable).setChecked(true);
                    ((CompoundButton) button_hsi_frequencies_disable).setChecked(true);
                } else {
                    /*USB modem with bb_sw and 3g_sw traces*/
                    ((CompoundButton) button_location_usb_modem).setChecked(true);
                    ((CompoundButton) button_trace_size_disable).setChecked(true);
                    ((CompoundButton) button_hsi_frequencies_disable).setChecked(true);
                }
            } else if (service_val == Modem_Configuration.mtsusb) {
                /*USB APE*/
                ((CompoundButton) button_location_usb_ape).setChecked(true);
            }
        } else if (((modem_val == Modem_Configuration.reboot_ok2) || (modem_val == Modem_Configuration.reboot_ko2))
                && (service_val == Modem_Configuration.mts_disable)) {
            /*Traces in coredump*/
            ((CompoundButton) button_location_coredump).setChecked(true);
            ((CompoundButton) button_trace_size_disable).setChecked(true);
            ((CompoundButton) button_hsi_frequencies_coredump).setChecked(true);
        } else {
            if ((modem_val == Modem_Configuration.reboot_ok4) || (modem_val == Modem_Configuration.reboot_ko4)) {
                /*Logging over HSI: 78MHz*/
                ((CompoundButton) button_hsi_frequencies_78).setChecked(true);
            } else { /*Logging over HSI: 156MHz, (modem_val == Modem_Configuration.reboot_ok5) OR (modem_val == Modem_Configuration.reboot_ko5)*/
                ((CompoundButton) button_hsi_frequencies_156).setChecked(true);
            }

            switch (service_val) {
            case Modem_Configuration.mtsfs_persistent:
                ((CompoundButton) button_location_emmc).setChecked(true);
                ((CompoundButton) button_trace_size_100).setChecked(true);
                break;
            case Modem_Configuration.mtsextfs_persistent:
                ((CompoundButton) button_location_emmc).setChecked(true);
                ((CompoundButton) button_trace_size_800).setChecked(true);
                break;
            case Modem_Configuration.mtssd_persistent:
                ((CompoundButton) button_location_sdcard).setChecked(true);
                ((CompoundButton) button_trace_size_100).setChecked(true);
                break;
            case Modem_Configuration.mtsextsd_persistent:
                ((CompoundButton) button_location_sdcard).setChecked(true);
                ((CompoundButton) button_trace_size_800).setChecked(true);
            }
        }
    }

    /*Update menu advanced layout according to the current state*/
    private void update_menu_advanced() {
        if (modem_application.modem_status != 1) {
            message_pop_up("SORRY", "Modem is not ready. Please Try again");
        } else {
            progressDialog = ProgressDialog.show(Settings_Activity.this, "Please wait....", "UPDATE in Progress");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        /*Recover the value of each parameters*/
                        current_service_value = services.service_status();
                        current_trace_level_value = modem_configuration.read_write_modem(Modem_Configuration.gsmtty_port,"at+xsystrace=10\r\n");
                        current_xsio_value = modem_configuration.read_write_modem(Modem_Configuration.gsmtty_port,"at+xsio?\r\n");
                        current_mux_value = modem_configuration.read_write_modem(Modem_Configuration.gsmtty_port,"at+xmux?\r\n");

                        /*If mux traces were enabled, we need to set only the checkbox without sending command to the modem*/
                        if (current_mux_value == modem_configuration.mux_enable) {
                            mux_status = true;
                        } else {
                            mux_status = false;
                        }

                        /*Check if the modem has been rebooted*/
                        current_modem_reboot = modem_configuration.modem_reboot_status(current_xsio_value);

                        /*Set all buttons of the layout according to the recovered values*/
                        settings_activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                /*If trace level is enabled, we set the checkbox without sending command to the modem*/
                                if (((current_trace_level_value == Modem_Configuration.trace_bb)
                                        || (current_trace_level_value == Modem_Configuration.trace_bb_3g)
                                        || (current_trace_level_value == Modem_Configuration.trace_bb_3g_digrf))) {
                                    activate_status = true;
                                    ((CompoundButton) checkbox_activate).setChecked(true);
                                } else {
                                    activate_status = false;
                                }

                                set_level_buttons(current_trace_level_value);
                                set_location_size_hsi_buttons(current_modem_reboot, current_service_value,current_trace_level_value);
                                set_checkbox_mux(current_mux_value);

                                /*After updating, print a message with OK button*/
                                if ((current_modem_reboot == Modem_Configuration.reboot_ko0) || (current_modem_reboot == Modem_Configuration.reboot_ko2)
                                        || (current_modem_reboot == Modem_Configuration.reboot_ko4) || (current_modem_reboot == Modem_Configuration.reboot_ko5)) {
                                    message_pop_up("WARNING", "Your board need a HARDWARE REBOOT");
                                }
                            }
                        });
                    } catch (IOException e2) {
                        Log.e(Modem_Configuration.TAG, "The ACTIVATE checkbox can't download the default configuration");
                        e2.printStackTrace();
                    } catch (NullPointerException e) {
                        Log.v(Modem_Configuration.TAG, "The ACTIVATE checkbox can't download the default configuration : null pointer");
                    }
                    progressDialog.dismiss();
                }
            }).start();
        }
    }

    /*Print pop-up message with ok button*/
    private void message_pop_up(String title, String message) {
        new AlertDialog.Builder(this)
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                /*Nothing to do, wait user press ok*/
            }
        })
        .show();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        modem_application = (Modem_Application) getApplicationContext();

        modem_configuration = new Modem_Configuration();
        services = new Services();
        settings_activity = this;

        button_location_emmc = (RadioButton) findViewById(R.id.settings_location_emmc_btn);
        button_location_sdcard = (RadioButton) findViewById (R.id.settings_location_sdcard_btn);
        button_location_coredump = (RadioButton) findViewById (R.id.settings_location_coredump_btn);
        button_location_usb_ape = (RadioButton) findViewById (R.id.settings_location_usb_ape_btn);
        button_location_usb_modem = (RadioButton) findViewById (R.id.settings_location_usb_modem_btn);
        button_level_bb = (RadioButton) findViewById (R.id.settings_level_bb_btn);
        button_level_bb_3g = (RadioButton) findViewById (R.id.settings_level_bb_3g_btn);
        button_level_bb_3g_digrf = (RadioButton) findViewById (R.id.settings_level_bb_3g_digrf_btn);
        button_level_no_trace = (RadioButton) findViewById (R.id.settings_level_no_trace_btn);
        button_trace_size_100 = (RadioButton) findViewById (R.id.settings_trace_size_100_btn);
        button_trace_size_800 = (RadioButton) findViewById (R.id.settings_trace_size_800_btn);
        button_trace_size_disable = (RadioButton) findViewById (R.id.settings_trace_size_disable_btn);
        button_hsi_frequencies_78 = (RadioButton) findViewById (R.id.settings_hsi_frequencies_78_btn);
        button_hsi_frequencies_156 = (RadioButton) findViewById (R.id.settings_hsi_frequencies_156_btn);
        button_hsi_frequencies_coredump = (RadioButton) findViewById (R.id.settings_hsi_frequencies_coredump_btn);
        button_hsi_frequencies_disable = (RadioButton) findViewById (R.id.settings_hsi_frequencies_disable_btn);
        checkbox_activate = (CheckBox) findViewById (R.id.activate_checkBox);
        checkbox_mux = (CheckBox) findViewById (R.id.mux_checkBox);

        /*Listener on button_location_emmc*/
        ((CompoundButton) button_location_emmc).setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                if (isChecked) {
                    button_level_bb_3g_digrf.setEnabled(true);
                    set_trace_file_size();
                }
            }
        });

        /*Listener on button_location_sdcard*/
        ((CompoundButton) button_location_sdcard).setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                if (isChecked) {
                    button_level_bb_3g_digrf.setEnabled(true);
                    set_trace_file_size();
                }
            }
        });

        /*Listener on button_location_coredump*/
        ((CompoundButton) button_location_coredump).setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                if (isChecked) {
                    ((CompoundButton) button_hsi_frequencies_coredump).setChecked(true);
                    button_level_bb_3g_digrf.setEnabled(true);
                    unset_trace_file_size();
                }
            }
        });

        /*Listener on button_location_usb_ape*/
        ((CompoundButton) button_location_usb_ape).setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                if (isChecked) {
                    ((CompoundButton) button_hsi_frequencies_disable).setChecked(true);
                    button_level_bb_3g_digrf.setEnabled(true);
                    unset_trace_file_size();
                }
            }
        });

        /*Listener on button_location_usb_modem*/
        ((CompoundButton) button_location_usb_modem).setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                if (isChecked) {
                    ((CompoundButton) button_hsi_frequencies_disable).setChecked(true);
                    if (((CompoundButton) button_level_bb_3g_digrf).isChecked()) {
                        ((CompoundButton) button_level_bb_3g).setChecked(true);
                    }
                    button_level_bb_3g_digrf.setEnabled(false);
                    unset_trace_file_size();
                }
            }
        });

        /*Listener on Activate Checkbox*/
        ((CompoundButton) checkbox_activate).setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                if (activate_status) {
                    /*checkbox_activate checked after update_menu_advanced(), don't send command again*/
                    activate_status = false;
                } else if (modem_application.modem_status != 1) {
                    message_pop_up("SORRY", "Modem is not ready. Please Try again");
                } else {
                    progressDialog = ProgressDialog.show(Settings_Activity.this, "Please wait....", "Apply ACTIVATE configuration in Progress");
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            if (((CompoundButton) checkbox_activate).isChecked()) {
                                /*Enable frequency according  user choice*/
                                modem_configuration.enable_xsio(xsio_selected());

                                /*Enable service according with user choice*/
                                services.enable_service(service_selected());

                                /*Enable trace level according user choice*/
                                modem_configuration.enable_trace_level(trace_level_selected());
                            } else { /*user unchecks ACTIVATE checkbox, download factory settings*/
                                try {
                                    int xsio_value = modem_configuration.read_write_modem(Modem_Configuration.gsmtty_port,"at+xsio?\r\n");
                                    int trace_level_value = modem_configuration.read_write_modem(Modem_Configuration.gsmtty_port,"at+xsystrace=10\r\n");
                                    int service_value = services.service_status();

                                    /*Download default configuration of xsio => xsio=0*/
                                    if (xsio_value != Modem_Configuration.xsio_00) {
                                        modem_configuration.enable_xsio(Modem_Configuration.xsio0);
                                        xsio_value = Modem_Configuration.xsio_00;
                                    }

                                    /*Download default configuration of trace level => trace disabled*/
                                    if (trace_level_value != Modem_Configuration.trace_disable) {
                                        modem_configuration.enable_trace_level(Modem_Configuration.trace_disable);
                                        trace_level_value = Modem_Configuration.trace_disable;
                                    }

                                    /*Download default configuration of service => service disabled*/
                                    if (service_value != Modem_Configuration.mts_disable) {
                                        services.stop_service(service_value);
                                        service_value = Modem_Configuration.mts_disable;
                                    }
                                } catch (IOException e2) {
                                    Log.e(Modem_Configuration.TAG, "The ACTIVATE checkbox can't download the default configuration");
                                    e2.printStackTrace();
                                } catch (NullPointerException e) {
                                    Log.v(Modem_Configuration.TAG, "The ACTIVATE checkbox can't download the default configuration : null pointer");
                                }
                            }

                            /*Print a pop-up message, user must reboot*/
                            settings_activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    message_pop_up("WARNING", "Your board need a HARDWARE REBOOT");
                                }
                            });
                            progressDialog.dismiss();
                        }
                    }).start();
                }
            }
        });

        /*Listener on Mux Checkbox*/
        ((CompoundButton) checkbox_mux).setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                if (mux_status) {
                    /*checkbox_mux checked after update_menu_advanced(), no utility to send command again*/
                    mux_status = false;
                } else if (modem_application.modem_status != 1) {
                    message_pop_up("SORRY", "Modem is not ready. Please Try again");
                } else {
                    /*User presses on checkbox_mux*/
                    progressDialog = ProgressDialog.show(Settings_Activity.this, "Please wait....", "Apply MUX configuration in Progress");
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            if (((CompoundButton) checkbox_mux).isChecked()) {
                                /*Enable Channel 0 to 17*/
                                modem_configuration.enable_mux_trace(modem_configuration.mux_enable);
                            } else {
                                /*Disable all channels*/
                                modem_configuration.enable_mux_trace(modem_configuration.mux_disable);
                            }

                            /*Print a pop-up message, user must reboot*/
                            settings_activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    message_pop_up("WARNING", "DON'T FORGET MUX TRACES ARE NOT PERSISTENT!");
                                }
                            });
                            progressDialog.dismiss();
                        }
                    }).start();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        /*Update trace location, level, file size, HSI frequency, ACTIVATE and MUX checkbox*/
        update_menu_advanced();
    }
}
