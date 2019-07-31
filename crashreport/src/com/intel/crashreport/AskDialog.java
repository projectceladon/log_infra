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

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;


public class AskDialog extends DialogFragment {

	private static final int DIALOG_ASK_UPLOAD_ID = 0;
	private static final int DIALOG_ASK_UPLOAD_SAVE_ID = 1;
	private static final int DIALOG_ASK_INVALID = -1;

    public static AskDialog newInstance(int num) {
        AskDialog frag = new AskDialog();
        Bundle args = new Bundle();
        args.putInt("num", num);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
	Bundle args = getArguments();
        int mNum = (args != null) ? args.getInt("num") : DIALOG_ASK_INVALID;
        Dialog dialog;
        switch(mNum){
        case DIALOG_ASK_UPLOAD_ID:
            dialog = ((StartServiceActivity)getActivity()).createAskForUploadDialog();
            break;
        case DIALOG_ASK_UPLOAD_SAVE_ID:
            dialog = ((StartServiceActivity)getActivity()).createAskForUploadSaveDialog();
            break;
        default:
            dialog = null;
            break;
        }
        return dialog;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
	Bundle args = getArguments();
        if (args != null)
	        ((StartServiceActivity)getActivity()).cancel(args.getInt("num"));
        super.onCancel(dialog);
    }
}

