/* INTEL CONFIDENTIAL
 * Copyright 2015 Intel Corporation
 *
 * The source code contained or described herein and all documents
 * related to the source code ("Material") are owned by Intel
 * Corporation or its suppliers or licensors. Title to the Material
 * remains with Intel Corporation or its suppliers and
 * licensors. The Material contains trade secrets and proprietary
 * and confidential information of Intel or its suppliers and
 * licensors. The Material is protected by worldwide copyright and
 * trade secret laws and treaty provisions. No part of the Material
 * may be used, copied, reproduced, modified, published, uploaded,
 * posted, transmitted, distributed, or disclosed in any way without
 * Intel's prior express written permission.
 *
 * No license under any patent, copyright, trade secret or other
 * intellectual property right is granted to or conferred upon you
 * by disclosure or delivery of the Materials, either expressly, by
 * implication, inducement, estoppel or otherwise. Any license under
 * such intellectual property rights must be express and approved by
 * Intel in writing.
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

