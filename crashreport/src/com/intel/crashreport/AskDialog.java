package com.intel.crashreport;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;


public class AskDialog extends DialogFragment {

	private static final int DIALOG_ASK_UPLOAD_ID = 0;
	private static final int DIALOG_ASK_UPLOAD_SAVE_ID = 1;

    public static AskDialog newInstance(int num) {
        AskDialog frag = new AskDialog();
        Bundle args = new Bundle();
        args.putInt("num", num);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int mNum = getArguments().getInt("num");
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
        ((StartServiceActivity)getActivity()).cancel(getArguments().getInt("num"));
        super.onCancel(dialog);
    }
}

