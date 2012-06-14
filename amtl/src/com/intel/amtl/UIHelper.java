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
import android.app.AlertDialog;
import android.content.DialogInterface;

public class UIHelper {

    /* Print pop-up message with ok and cancel buttons */
    public static void message_warning(final Activity A, String title, String message) {
        new AlertDialog.Builder(A)
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                /* Nothing to do */
            }
        })
        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                /* Exit application */
                A.finish();
            }
        })
        .show();
    }

    /* Print pop-up message with ok button */
    public static void message_pop_up(Activity A, String title, String message) {
        new AlertDialog.Builder(A)
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                /* Nothing to do, waiting for user to press OK button */
            }
        })
        .show();
    }

    /* Print a dialog before exiting application */
    public static void exitDialog(final Activity A, String title, String message) {
        new AlertDialog.Builder(A)
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                A.finish();
            }
        })
        .show();
    }
}
