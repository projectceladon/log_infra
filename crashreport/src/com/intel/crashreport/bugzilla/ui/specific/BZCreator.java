package com.intel.crashreport.bugzilla.ui.specific;

import java.util.ArrayList;

import android.content.Context;

import com.intel.crashreport.specific.CrashlogDaemonCmdFile;

public enum BZCreator {

	INSTANCE;

	public boolean createBZ(ArrayList<String> sArguments, Context context) {
		return CrashlogDaemonCmdFile.CreateCrashlogdCmdFile(CrashlogDaemonCmdFile.Command.BZ, sArguments, context);
	}
}
