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
 */

package com.intel.amtl;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RadioButton;

import java.io.IOException;

public class SettingsActivity extends Activity {

    private static final String MODULE = "SettingsActivity";

    private CompoundButton button_location_emmc;
    private CompoundButton button_location_sdcard;
    private CompoundButton button_location_coredump;
    private CompoundButton button_location_usb_ape;
    private CompoundButton button_location_usb_modem;
    private CompoundButton button_level_bb;
    private CompoundButton button_level_bb_3g;
    private CompoundButton button_level_bb_3g_digrf;
    private CompoundButton button_trace_size_100;
    private CompoundButton button_trace_size_800;
    private CompoundButton button_hsi_frequencies_78;
    private CompoundButton button_hsi_frequencies_156;
    private CheckBox checkbox_activate;
    private CheckBox checkbox_mux;

    private boolean invalidateFlag;

    private AmtlCore core;

    /* Local selected values */
    private CustomCfg cfg;

    /* Trace file size not useful in coredump, usb ape and usb modem cases */
    private void unset_trace_file_size() {
        button_trace_size_100.setEnabled(false);
        button_trace_size_800.setEnabled(false);
        button_hsi_frequencies_78.setEnabled(false);
        button_hsi_frequencies_156.setEnabled(false);
        button_level_bb_3g.setChecked(cfg.traceLevel == CustomCfg.TRACE_LEVEL_NONE);
    }

    /* Trace file size is useful in EMMC and SDCARD cases */
    private void set_trace_file_size() {
        button_trace_size_100.setEnabled(true);
        button_trace_size_800.setEnabled(true);
        button_trace_size_800.setChecked(true);
        button_hsi_frequencies_78.setEnabled(true);
        button_hsi_frequencies_78.setChecked(true);
        button_level_bb_3g.setChecked(cfg.traceLevel == CustomCfg.TRACE_LEVEL_NONE);
    }

    /* Set trace level button */
    private void set_trace_level_button() {
        switch(cfg.traceLevel) {
        case CustomCfg.TRACE_LEVEL_NONE:
            break;
        case CustomCfg.TRACE_LEVEL_BB:
            button_level_bb.setChecked(true);
            break;
        case CustomCfg.TRACE_LEVEL_BB_3G:
            button_level_bb_3g.setChecked(true);
            break;
        case CustomCfg.TRACE_LEVEL_BB_3G_DIGRF:
            button_level_bb_3g_digrf.setChecked(true);
            break;
        default:
            /* Do nothing */
            break;
        }
    }

    /* Set trace location button */
    private void set_location_button() {
        switch (cfg.traceLocation) {
        case CustomCfg.TRACE_LOC_NONE:
            break;
        case CustomCfg.TRACE_LOC_EMMC:
            button_location_emmc.setChecked(true);
            set_trace_file_size();
            break;
        case CustomCfg.TRACE_LOC_SDCARD:
            button_location_sdcard.setChecked(true);
            set_trace_file_size();
            break;
        case CustomCfg.TRACE_LOC_COREDUMP:
            button_location_coredump.setChecked(true);
            unset_trace_file_size();
            break;
        case CustomCfg.TRACE_LOC_USB_APE:
            button_location_usb_ape.setChecked(true);
            unset_trace_file_size();
            break;
        case CustomCfg.TRACE_LOC_USB_MODEM:
            button_location_usb_modem.setChecked(true);
            unset_trace_file_size();
            break;
        default:
            /* Do nothing */
            break;
        }
    }

    /* Set log size button */
    private void set_log_size_button() {
        switch (cfg.traceFileSize) {
        case CustomCfg.LOG_SIZE_NONE:
            break;
        case CustomCfg.LOG_SIZE_100_MB:
            button_trace_size_100.setChecked(true);
            break;
        case CustomCfg.LOG_SIZE_800_MB:
            button_trace_size_800.setChecked(true);
            break;
        default:
            /* Do nothing */
            break;
        }
    }

    /* Set HSI frequency button */
    private void set_hsi_frequency_button() {
        switch (cfg.hsiFrequency) {
        case CustomCfg.HSI_FREQ_NONE:
            break;
        case CustomCfg.HSI_FREQ_78_MHZ:
            button_hsi_frequencies_78.setChecked(true);
            break;
        case CustomCfg.HSI_FREQ_156_MHZ:
            button_hsi_frequencies_156.setChecked(true);
            break;
        default:
            /* Do nothing */
            break;
        }
    }

    /* Set MUX trace checkbox state */
    private void set_checkbox_mux() {
        checkbox_mux.setChecked(cfg.muxTrace == CustomCfg.MUX_TRACE_ON);
    }

    /* Update settings menu buttons */
    private void update_settings_menu() {
        set_location_button();
        set_trace_level_button();
        set_log_size_button();
        set_hsi_frequency_button();
        set_checkbox_mux();
        invalidate();
    }

    private void invalidate() {
        invalidateFlag = true;
        checkbox_activate.setChecked(!reboot_needed());
        invalidateFlag = false;
    }

    private boolean reboot_needed() {
        CustomCfg curCfg = core.getCurCustomCfg();
        return (
            (cfg.traceLocation != curCfg.traceLocation) ||
            (cfg.traceLevel != curCfg.traceLevel) ||
            (cfg.traceFileSize != curCfg.traceFileSize) ||
            (cfg.hsiFrequency != curCfg.hsiFrequency));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        /* Trace location buttons */
        button_location_emmc = (CompoundButton) findViewById(R.id.settings_location_emmc_btn);
        button_location_sdcard = (CompoundButton) findViewById (R.id.settings_location_sdcard_btn);
        button_location_coredump = (CompoundButton) findViewById (R.id.settings_location_coredump_btn);
        button_location_usb_ape = (CompoundButton) findViewById (R.id.settings_location_usb_ape_btn);
        button_location_usb_modem = (CompoundButton) findViewById (R.id.settings_location_usb_modem_btn);

        /* Trace level buttons */
        button_level_bb = (CompoundButton) findViewById (R.id.settings_level_bb_btn);
        button_level_bb_3g = (CompoundButton) findViewById (R.id.settings_level_bb_3g_btn);
        button_level_bb_3g_digrf = (CompoundButton) findViewById (R.id.settings_level_bb_3g_digrf_btn);

        /* Log size buttons */
        button_trace_size_100 = (CompoundButton) findViewById (R.id.settings_trace_size_100_btn);
        button_trace_size_800 = (CompoundButton) findViewById (R.id.settings_trace_size_800_btn);

        /* HSI frequency buttons */
        button_hsi_frequencies_78 = (CompoundButton) findViewById (R.id.settings_hsi_frequencies_78_btn);
        button_hsi_frequencies_156 = (CompoundButton) findViewById (R.id.settings_hsi_frequencies_156_btn);

        /* Activate check box */
        checkbox_activate = (CheckBox) findViewById (R.id.activate_checkBox);
        checkbox_activate.setChecked(false);

        /* MUX traces check box */
        checkbox_mux = (CheckBox) findViewById (R.id.mux_checkBox);

        /* Get application core */
        try {
            cfg = new CustomCfg();
            this.core = AmtlCore.get();
            this.core.setContext(this.getApplicationContext());
            this.core.invalidate();
            if (core.getCurCfg() == PredefinedCfg.TRACE_DISABLE) {
                /* There is no way to disable logs from advanced settings menu */
                /* Suggest a default configuration : blue configuration */
                UIHelper.message_pop_up(this,
                    "INFO","Currently, traces are disabled.\n" +
                    "We suggest you a default configuration.\n" +
                    "Don't forget to confirm with ACTIVATE checkbox");
                cfg.traceLocation = CustomCfg.TRACE_LOC_COREDUMP;
                cfg.traceLevel = CustomCfg.TRACE_LEVEL_BB_3G;
                cfg.traceFileSize = CustomCfg.LOG_SIZE_NONE;
                cfg.hsiFrequency = CustomCfg.HSI_FREQ_NONE;
                cfg.muxTrace = CustomCfg.MUX_TRACE_OFF;
            }
            else {
                CustomCfg curCfg = core.getCurCustomCfg();
                /* Get current custom configuration */
                cfg.traceLocation = curCfg.traceLocation;
                cfg.traceLevel = curCfg.traceLevel;
                cfg.traceFileSize = curCfg.traceFileSize;
                cfg.hsiFrequency = curCfg.hsiFrequency;
                cfg.muxTrace = curCfg.muxTrace;
            }

            update_settings_menu();
        }
        catch (AmtlCoreException e) {
            /* Failed to initialize application core */
            this.core = null;
            Log.e(AmtlCore.TAG, MODULE + ": " + e.getMessage());
            UIHelper.message_pop_up(this, "ERROR",e.getMessage());
        }

        /* Listener on button_location_emmc */
        button_location_emmc.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    set_trace_file_size();
                    cfg.traceLocation = CustomCfg.TRACE_LOC_EMMC;
                    invalidate();
                }
            }
        });

        /* Listener on button_location_sdcard */
        button_location_sdcard.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    set_trace_file_size();
                    cfg.traceLocation = CustomCfg.TRACE_LOC_SDCARD;
                    invalidate();
                }
            }
        });

        /* Listener on button_location_coredump */
        button_location_coredump.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    unset_trace_file_size();
                    cfg.traceLocation = CustomCfg.TRACE_LOC_COREDUMP;
                    invalidate();
                }
            }
        });

        /* Listener on button_location_usb_ape */
        button_location_usb_ape.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    unset_trace_file_size();
                    cfg.traceLocation = CustomCfg.TRACE_LOC_USB_APE;
                    invalidate();
                }
            }
        });

        /* Listener on button_location_usb_modem */
        button_location_usb_modem.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (button_level_bb_3g_digrf.isChecked()) {
                        button_level_bb_3g.setChecked(true);
                    }
                    button_level_bb_3g_digrf.setEnabled(false);
                    unset_trace_file_size();
                    cfg.traceLocation = CustomCfg.TRACE_LOC_USB_MODEM;
                    invalidate();
                }
                else {
                    button_level_bb_3g_digrf.setEnabled(true);
                }
            }
        });

        /* Listener on button_level_bb */
        button_level_bb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    cfg.traceLevel = CustomCfg.TRACE_LEVEL_BB;
                    invalidate();
                }
            }
        });

        /* Listener on button_level_bb_3g */
        button_level_bb_3g.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    cfg.traceLevel = CustomCfg.TRACE_LEVEL_BB_3G;
                    invalidate();
                }
            }
        });

        /* Listener on button_level_bb_3g_digrf */
        button_level_bb_3g_digrf.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    cfg.traceLevel = CustomCfg.TRACE_LEVEL_BB_3G_DIGRF;
                    invalidate();
                }
            }
        });

        /* Listener on button_trace_size_100 */
        button_trace_size_100.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    cfg.traceFileSize = CustomCfg.LOG_SIZE_100_MB;
                    invalidate();
                }
            }
        });

        /* Listener on button_trace_size_800 */
        button_trace_size_800.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    cfg.traceFileSize = CustomCfg.LOG_SIZE_800_MB;
                    invalidate();
                }
            }
        });

        /* Listener on button_hsi_frequencies_78 */
        button_hsi_frequencies_78.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    cfg.hsiFrequency = CustomCfg.HSI_FREQ_78_MHZ;
                    invalidate();
                }
            }
        });

        /* Listener on button_hsi_frequencies_156 */
        button_hsi_frequencies_156.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    cfg.hsiFrequency = CustomCfg.HSI_FREQ_156_MHZ;
                    invalidate();
                }
            }
        });

        /* Listener on MUX trace Checkbox */
        checkbox_mux.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                cfg.muxTrace = (isChecked) ? CustomCfg.MUX_TRACE_ON: CustomCfg.MUX_TRACE_OFF;
                core.setMuxTrace(cfg.muxTrace);
            }
        });

        /* Listener on Activate Checkbox */
        checkbox_activate.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!invalidateFlag) {
                    if (isChecked) {
                        core.setCustomCfg(cfg);
                        UIHelper.message_pop_up(SettingsActivity.this, "WARNING", "Your board need a HARDWARE REBOOT");
                    }
                }
            }
        });
    }
}
