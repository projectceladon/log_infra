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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {

    private Button button_mts;
    private Button button_usb;
    private Button button_toggle;
    private Button button_coredump;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        button_mts = (Button) findViewById(R.id.mts_button);
        button_usb = (Button) findViewById(R.id.usb_button);
        button_toggle = (Button) findViewById(R.id.toggle_button);
        button_coredump = (Button) findViewById(R.id.coredump_button);

        /*Listener for modem trace server*/
        button_mts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent i = new Intent(MainActivity.this, ModemtraceserverActivity.class);
                startActivity(i);
            }
        });

        /*Listener for usbswitch*/
        button_usb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent i = new Intent(MainActivity.this, UsbswitchActivity.class);
                startActivity(i);
            }
        });

        /*Listener for toggle pin on1*/
        button_toggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent i = new Intent(MainActivity.this, Toggle_pinActivity.class);
                startActivity(i);
            }
        });

        /*Listener for trace in coredump*/
        button_coredump.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent i = new Intent(MainActivity.this, Trace_in_coredumpActivity.class);
                startActivity(i);
            }
        });
    }
}