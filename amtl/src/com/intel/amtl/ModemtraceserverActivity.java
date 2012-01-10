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
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class ModemtraceserverActivity extends Activity {

    private Button button_configure;
    private Button button_activate;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mts);

        button_configure = (Button) findViewById(R.id.config_button);
        button_activate = (Button) findViewById(R.id.activate_button);

        /*Listener for configure_trace_modem button*/
        button_configure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent i = new Intent(ModemtraceserverActivity.this, Configure_trace_modemActivity.class);
                startActivity(i);
            }
        });

        /*Listener for activate_trace_modem button*/
        button_activate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent i = new Intent(ModemtraceserverActivity.this, Activate_trace_modemActivity.class);
                startActivity(i);
            }
        });
    }
}