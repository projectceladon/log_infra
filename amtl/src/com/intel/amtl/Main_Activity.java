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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ToggleButton;

import java.io.FileOutputStream;
import java.io.IOException;

public class Main_Activity extends Activity {
    private ToggleButton button_modem_coredump;
    private ToggleButton button_ape_log_file;
    private ToggleButton button_disable_modem_trace;
    private int service_value;
    private int trace_level_value;
    private int xsio_value;
    private int info_modem_reboot;
    protected static String output_file = "usbswitch.conf";
    protected boolean flag = true;
    private ProgressDialog progressDialog;
    private Services services;
    private Main_Activity main_activity;
    private Modem_Configuration modem_configuration;
    private SynchronizeSTMD synchronizestmd;
    protected Modem_Application modem_application;
    Runtime rtm = java.lang.Runtime.getRuntime();

    /*Print pop-up message with ok and cancel buttons*/
    private void message_warning(String title, String message) {
        new AlertDialog.Builder(this)
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                /*Nothing to do*/
            }
        })
        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                /*Exit*/
                finish();
            }
        })
        .show();
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

    /*Create menu_advanced*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.layout.menu_advanced, menu);
        return true;
    }

    /*Start activity according to button pressed*/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.settings:
            Intent i = new Intent(Main_Activity.this, Settings_Activity.class);
            startActivity(i);
            return true;
        case R.id.additional_features:
            Intent j = new Intent(Main_Activity.this, Additional_Features_Activity.class);
            startActivity(j);
            return true;
        default:
            return super.onContextItemSelected(item);
        }
    }

    /*Create usbswitch.conf file if it doesn't exist*/
    protected void usbswitch_update() {
        try {
            FileOutputStream usbswitch = openFileOutput(output_file, Context.MODE_APPEND);
            usbswitch.close();
        } catch (IOException e1) {
            Log.e(Modem_Configuration.TAG, "Main_Activity can't create the file usbswitch.conf");
            e1.printStackTrace();
        }

        /*Update the value of usbswitch in /data/data/com.intel.amtl/file/usbswitch.conf
         * 0: usb ape
         * 1: usb modem */
        try {
            rtm.exec("start usbswitch_status");
        } catch (IOException e1) {
            Log.e(Modem_Configuration.TAG, "Main_Activity can't start the service usbswitch_status");
            e1.printStackTrace();
        }
    }

    /*Update main menu layout according to current configuration*/
    private void update_main_menu() {
        if (modem_application.modem_status != 1) {
            message_pop_up("SORRY", "Modem is not ready. Please Try again");
        } else {
            progressDialog = ProgressDialog.show(Main_Activity.this, "Please wait....", "UPDATE in Progress");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        /*If necessary Create and Update usbswitch.conf file*/
                        usbswitch_update();

                        /*Recover the current configuration*/
                        service_value = services.service_status();
                        trace_level_value = modem_configuration.read_write_modem(Modem_Configuration.gsmtty_port,"at+xsystrace=10\r\n");
                        xsio_value = modem_configuration.read_write_modem(Modem_Configuration.gsmtty_port,"at+xsio?\r\n");

                        /*Recover the modem reboot information*/
                        info_modem_reboot = modem_configuration.modem_reboot_status(xsio_value);

                        main_activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                set_main_buttons(info_modem_reboot, service_value, trace_level_value);

                                /*If modem has not been rebooted, print a pop-up message with ok button*/
                                if ((info_modem_reboot == Modem_Configuration.reboot_ko0) || (info_modem_reboot == Modem_Configuration.reboot_ko2)
                                        || (info_modem_reboot == Modem_Configuration.reboot_ko4) || (info_modem_reboot == Modem_Configuration.reboot_ko5)) {
                                    message_pop_up("WARNING", "Your board need a HARDWARE REBOOT");
                                }
                            }
                        });
                    } catch (IOException e2) {
                        Log.e(Modem_Configuration.TAG, "Main_Activity can't update the current menu");
                        e2.printStackTrace();
                    } catch (NullPointerException e) {
                        Log.v(Modem_Configuration.TAG, "Main_Activity current menu : null pointer");
                    }
                    progressDialog.dismiss();
                }
            }).start();
        }
    }

    /*Set all button according the current configuration during the update*/
    private void set_main_buttons(int xsio_val, int service_val, int trace_level_val) {
        if (((xsio_val == Modem_Configuration.reboot_ok2) || (xsio_val == Modem_Configuration.reboot_ko2))
                && (service_val == Modem_Configuration.mts_disable) && (trace_level_val == Modem_Configuration.trace_bb_3g)) {
            /*Trace in coredump enabled*/
            (button_modem_coredump).setChecked(true);
            (button_ape_log_file).setChecked(false);
            (button_disable_modem_trace).setChecked(false);
        } else if (((xsio_val == Modem_Configuration.reboot_ok4) || (xsio_val == Modem_Configuration.reboot_ko4))
                && (service_val == Modem_Configuration.mtsextfs_persistent) && (trace_level_val == Modem_Configuration.trace_bb_3g)) {
            /*Trace in APE log file enabled*/
            (button_modem_coredump).setChecked(false);
            (button_ape_log_file).setChecked(true);
            (button_disable_modem_trace).setChecked(false);
        } else if (((xsio_val == Modem_Configuration.reboot_ok0) || (xsio_val == Modem_Configuration.reboot_ko0))
                && (service_val == Modem_Configuration.mts_disable) && (trace_level_val == Modem_Configuration.trace_disable)) {
            /*Trace disabled*/
            (button_modem_coredump).setChecked(false);
            (button_ape_log_file).setChecked(false);
            (button_disable_modem_trace).setChecked(true);
        } else {
            /*Other configuration not available in standard mode*/
            (button_modem_coredump).setChecked(false);
            (button_ape_log_file).setChecked(false);
            (button_disable_modem_trace).setChecked(false);
            message_pop_up("WARNING","Please use Advanced Menu to know your current configuration");
        }
    }

    /*Trace stopped : download factory settings*/
    private void disable_modem_trace() {
        if (modem_application.modem_status != 1) {
            message_pop_up("SORRY", "Modem is not ready. Please Try again");
        } else {
            progressDialog = ProgressDialog.show(Main_Activity.this, "Please wait....", "Traces will be STOPPED");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        modem_configuration.read_write_modem(Modem_Configuration.gsmtty_port,"at+xsio=0\r\n");
                        modem_configuration.enable_trace_level(Modem_Configuration.trace_disable);

                        /*Check the status of services and stops it if necessary*/
                        services.stop_service(services.service_status());

                        /*Print a pop-up message, user must reboot*/
                        main_activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                message_pop_up("WARNING", "Your board need a HARDWARE REBOOT");
                            }
                        });
                        progressDialog.dismiss();
                    } catch (IOException e) {
                        Log.e(Modem_Configuration.TAG, "Main_Activity can't apply disable configuration");
                        e.printStackTrace();
                    } catch (NullPointerException e) {
                        Log.v(Modem_Configuration.TAG, "Main_Activity disable configuration: null pointer");
                    }
                }
            }).start();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        modem_application = (Modem_Application) getApplicationContext();
        synchronizestmd = new SynchronizeSTMD(modem_application);

        /*Start Synchronize between AMTL and STMD*/
        synchronizestmd.start();

        modem_configuration = new Modem_Configuration();
        services = new Services();
        main_activity = this;

        button_modem_coredump = (ToggleButton) findViewById(R.id.modem_coredump_btn);
        button_ape_log_file = (ToggleButton) findViewById(R.id.ape_log_file_btn);
        button_disable_modem_trace = (ToggleButton) findViewById(R.id.disable_modem_trace_btn);

        /*On start, print a warning message*/
        message_warning("WARNING","This is a R&D Application. Please do not use unless you are asked to!");

        /*Listener on Modem Coredump button*/
        button_modem_coredump.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (button_modem_coredump.isChecked()) {
                    (button_ape_log_file).setChecked(false);
                    (button_disable_modem_trace).setChecked(false);

                    if (modem_application.modem_status != 1) {
                        message_pop_up("SORRY", "Modem is not ready. Please Try again");
                    } else {
                        progressDialog = ProgressDialog.show(Main_Activity.this, "Please wait....", "Traces will be saved in:\n/data/logs/modemcrash/");
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    modem_configuration.read_write_modem(Modem_Configuration.gsmtty_port,"at+xsio=2\r\n");
                                    modem_configuration.enable_trace_level(Modem_Configuration.trace_bb_3g);

                                    /*Check the status of services and stops it if necessary*/
                                    services.stop_service(services.service_status());

                                    /*Print a pop-up message, user must reboot*/
                                    main_activity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            message_pop_up("WARNING", "Your board need a HARDWARE REBOOT");
                                        }
                                    });
                                    progressDialog.dismiss();
                                } catch (IOException e) {
                                    Log.e(Modem_Configuration.TAG, "Main_Activity can't apply the modem coredump configuration");
                                    e.printStackTrace();
                                } catch (NullPointerException e) {
                                    Log.v(Modem_Configuration.TAG, "Main_Activity modem coredump configuration: null pointer");
                                }
                            }
                        }).start();
                    }
                } else {
                    /*If user presses again on button_modem_coredump, traces are stopped*/
                    (button_modem_coredump).setChecked(false);
                    (button_disable_modem_trace).setChecked(true);
                    disable_modem_trace();
                }
            }
        });

        /*Listener on APE Log File button*/
        button_ape_log_file.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (button_ape_log_file.isChecked()) {
                    (button_modem_coredump).setChecked(false);
                    (button_disable_modem_trace).setChecked(false);

                    if (modem_application.modem_status != 1) {
                        message_pop_up("SORRY", "Modem is not ready. Please Try again");
                    } else {
                        progressDialog = ProgressDialog.show(Main_Activity.this, "Please wait....", "Traces will be saved in:\n/data/logs/modemcrash/");
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {

                                    /*Check the status of services and stops it if necessary*/
                                    services.stop_service(services.service_status());

                                    modem_configuration.read_write_modem(Modem_Configuration.gsmtty_port,"at+xsio=4\r\n");
                                    modem_configuration.enable_trace_level(Modem_Configuration.trace_bb_3g);

                                    /*Enable mtsextfs persistent*/
                                    services.enable_service(Modem_Configuration.mtsextfs_persistent);

                                    /*Print a pop-up message, user must reboot*/
                                    main_activity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            message_pop_up("WARNING", "Your board need a HARDWARE REBOOT");
                                        }
                                    });
                                    progressDialog.dismiss();
                                } catch (IOException e) {
                                    Log.e(Modem_Configuration.TAG, "Main_Activity can't apply the ape_log_file configuration");
                                    e.printStackTrace();
                                } catch (NullPointerException e) {
                                    Log.v(Modem_Configuration.TAG, "Main_Activity ape_log_file configuration: null pointer");
                                }
                            }
                        }).start();
                    }
                } else {
                    /*If user presses again on button_ape_log_file, traces are stopped*/
                    (button_ape_log_file).setChecked(false);
                    (button_disable_modem_trace).setChecked(true);
                    disable_modem_trace();
                }
            }
        });

        /*Listener on Disable button*/
        button_disable_modem_trace.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (button_disable_modem_trace.isChecked()) {
                    (button_ape_log_file).setChecked(false);
                    (button_modem_coredump).setChecked(false);
                    disable_modem_trace();
                } else {
                    (button_disable_modem_trace).setChecked(true);
                }
            }
        });
    }

    /*User returns to the activity -> update it*/
    @Override
    protected void onResume() {
        super.onResume();
        update_main_menu();
    }

    @Override
    protected void onDestroy() {
        Log.d(Modem_Configuration.TAG, "onDestroy() call");
        super.onDestroy();

        synchronizestmd.flag = false;
        if (synchronizestmd.mSocket != null) {
            synchronizestmd.close_gsmtty();
            try {
                Log.d(Modem_Configuration.TAG, "onDestroy() msocket !=null");
                synchronizestmd.mSocket.close();
            } catch (IOException ex) {
                /*ignore failure to close socket*/
            }
            synchronizestmd.mSocket = null;
        }
    }
}
