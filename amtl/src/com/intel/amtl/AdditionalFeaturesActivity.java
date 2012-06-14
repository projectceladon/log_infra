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
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class AdditionalFeaturesActivity extends Activity {

    private static final String MODULE = "AdditionalFeaturesActivity";

    private static final int USB_TO_MODEM_WAIT = 1000;
    private static final int USB_TO_APE_WAIT = 1000;

    private CompoundButton button_usb_modem;
    private CompoundButton button_usb_ape;
    private Button button_toggle_on1;

    private char[] inputBuffer;
    private String data;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.additional_features);

        button_usb_modem = (CompoundButton) findViewById(R.id.additional_features_modem_btn);
        button_usb_ape = (CompoundButton) findViewById(R.id.additional_features_ape_btn);
        button_toggle_on1 = (Button) findViewById(R.id.additional_features_toggle_on1_btn);

        /* Read usbswitch.conf file */
        FileInputStream fIn = null;
        InputStreamReader isr = null;
        try {
            fIn = openFileInput(AmtlCore.OUTPUT_FILE);
            isr = new InputStreamReader(fIn);
            inputBuffer = new char[255];
            isr.read(inputBuffer);
            data = new String(inputBuffer);

            /* Set button according to usbswitch.conf file */
            if (data.contains("1")) {
                button_usb_modem.setChecked(true);
            }
            else {
                button_usb_ape.setChecked(true);
            }
        }
        catch (Exception e1) {
            Log.e(AmtlCore.TAG, MODULE + ": can't read the file usbswitch.conf");
        }
        finally {
            try {
                if (fIn != null)
                    fIn.close();
                if (isr != null)
                    isr.close();
            }
            catch (IOException e) {
                Log.e(AmtlCore.TAG, MODULE + ": " + e.getMessage());
            }
        }

        /* Listener on button_usb_modem : USB ape to modem */
        button_usb_modem.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                if (isChecked) {
                    try {
                        AmtlCore.rtm.exec("start usb_to_modem");
                        android.os.SystemClock.sleep(USB_TO_MODEM_WAIT);
                    }
                    catch (IOException e) {
                        Log.e(AmtlCore.TAG, MODULE + ": can't enable usbswitch MODEM");
                    }
                }
            }
        });

        /* Listener on button_usb_ape : USB modem to ape */
        button_usb_ape.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                if (isChecked) {
                    try {
                        AmtlCore.rtm.exec("start usb_to_ape");
                        android.os.SystemClock.sleep(USB_TO_APE_WAIT);
                    }
                    catch (IOException e) {
                        Log.e(AmtlCore.TAG, MODULE + ": can't enable usbswitch APE");
                    }
                }
            }
        });

        /* Listener on button_toggle_on1 */
        button_toggle_on1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                try {
                    AmtlCore.rtm.exec("start toggle_on1");
                    Toast toast = Toast.makeText(AdditionalFeaturesActivity.this, "Toggle pin on1 DONE", Toast.LENGTH_LONG);
                    toast.show();
                }
                catch (IOException e) {
                    Log.e(AmtlCore.TAG, MODULE + ": toggle pin can't apply");
                }
            }
        });
    }
}
