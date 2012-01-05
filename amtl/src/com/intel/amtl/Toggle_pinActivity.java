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

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class Toggle_pinActivity extends Activity {

    private Button button_toggle_on1;
    Runtime rtm=java.lang.Runtime.getRuntime();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.toggle);

        button_toggle_on1 = (Button) findViewById(R.id.button_apply_toggle_on1);

        /*Listener for button_toggle_on1*/
        button_toggle_on1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                try {
                    rtm.exec("start toggle_on1");
                    android.os.SystemClock.sleep(1000);
                    rtm.exec("stop toggle_on1");
                    Toast toast=Toast.makeText(Toggle_pinActivity.this, "Toggle pin on1 DONE", Toast.LENGTH_LONG);
                    toast.show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
