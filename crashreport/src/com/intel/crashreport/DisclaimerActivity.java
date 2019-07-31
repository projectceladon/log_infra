/* Copyright (C) 2019 Intel Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.intel.crashreport;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;

import android.app.Activity;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.provider.Settings;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.content.pm.ActivityInfo;

public class DisclaimerActivity extends Activity {
    private static String TAG = "CollectionDisclaimer";
    private WizardBypass bypass = null;

    private String readAsset(String assetPath) {
        StringBuilder input = new StringBuilder();
        try {
            String buffer;
            InputStream inputStream = getResources().getAssets().open(assetPath);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream,"UTF8"));

            while ((buffer = reader.readLine()) != null)
                input.append(buffer);
        } catch (IOException exception) {
            String message = getString(R.string.alert_disclaimer_text_error);
            Log.e(TAG, "Exception while loading asset: " + exception);
            input.append(message);
        }
        return input.toString();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        //resume rotation
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.disclaimer);
        bypass = new WizardBypass();

        final Button button = (Button)findViewById(R.id.button_accept_disclaimer);
        final TextView disclaimer = (TextView)findViewById(R.id.text_disclaimer);

        if (disclaimer != null) {
            disclaimer.setText(Html.fromHtml(readAsset("disclaimer.htm")));
            disclaimer.setMovementMethod(LinkMovementMethod.getInstance());
        }

        if (button != null) {
            button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    closeActivity(false);
                }
            });
        }

        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    private void closeActivity(boolean skipGoogleSetup) {
        PackageManager pm = getPackageManager();
        pm.setComponentEnabledSetting(new ComponentName(getPackageName(),
            DisclaimerActivity.class.getName()), PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP);

        if (skipGoogleSetup) {
            pm.setComponentEnabledSetting(new ComponentName("com.google.android.setupwizard",
                "com.google.android.setupwizard.SetupWizardActivity"), PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);

            // system settings need to be updated in order to enable the home screen
            Settings.Secure.putInt(getContentResolver(), Settings.Secure.DEVICE_PROVISIONED, 1);
            Settings.Secure.putInt(getContentResolver(), Settings.Secure.USER_SETUP_COMPLETE, 1);
        }

        finish();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (bypass.couldBypass(ev))
            closeActivity(true);

        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onBackPressed() {
        closeActivity(false);
    }

    private class WizardBypass {
        private int mWidth = 0;
        private int mHeight = 0;
        // a corner will be considered a tap within a range of 20% of the height/width of the view
        private double radius = 0.2;
        // indicates the maximum number of move events before an action is not considered a tap
        private int mMoves = 0, mMaxTolerance = 9;
        private boolean topLeft = false, topRight = false, bottomLeft = false, bottomRight = false;

        WizardBypass() {
            Point point = new Point();
            DisclaimerActivity.this.getWindowManager().getDefaultDisplay().getRealSize(point);
            mHeight = point.y;
            mWidth = point.x;
        }

        private void resetState() {
            topLeft = false;
            topRight = false;
            bottomLeft = false;
            bottomRight = false;
        }

        public boolean couldBypass(MotionEvent event) {

            int action = event.getAction();
            if (action == MotionEvent.ACTION_UP) {
                if (mMoves > 0) {
                    int x = (int)event.getX();
                    int y = (int)event.getY();

                    if (x < mWidth * radius) {
                        if (y < mHeight * radius) {
                            topLeft = true;
                        }
                        else if (y > mHeight - mHeight * radius) {
                            bottomLeft = true;
                        }
                        else {
                            resetState();
                            return false;
                        }
                    }
                    else if (x > mWidth - mWidth * radius) {
                        if (y < mHeight * radius) {
                            topRight = true;
                        }
                        else if (y > mHeight - mHeight * radius) {
                            bottomRight = true;
                        }
                        else {
                            resetState();
                            return false;
                        }
                    }
                    else {
                            resetState();
                            return false;
                    }
                    return topLeft && topRight && bottomLeft && bottomRight;
                }
                else {
                    resetState();
                }
            }
            else if (action == MotionEvent.ACTION_MOVE) {
                mMoves--;
            }
            else if (action == MotionEvent.ACTION_DOWN) {
                mMoves = mMaxTolerance;
            }
            return false;
        }
    }
}
