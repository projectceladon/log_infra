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

package com.intel.crashreport.specific;

import android.content.Context;
import android.os.AsyncTask;

/**
 * This class processes an aplogs upload request.
 * It creates the aplog_trigger that contains an APLOG
 * field if the user wants to upload all the aplogs or
 * an empty file if the user wants the default aplogs number.
 *
 */
public class UploadAplogTask extends AsyncTask<Void, Void, Void> {
	private int m_iLog = -1;
	private Context context;
	private String mMessage;

	public UploadAplogTask(int iLog, Context ctx, String message){
		m_iLog = iLog;
		context = ctx;
		mMessage = message;
	}

	public UploadAplogTask(int iLog, Context ctx){
		this(-1,ctx, "");
	}

	public UploadAplogTask(Context ctx){
		this(-1,ctx);
	}

	@Override
	protected Void doInBackground(Void... params) {

		//Create a file to trigger crashlog daemon
		String sArgument = "";
		if (m_iLog > 0)
			sArgument="APLOG=" + m_iLog + "\n";
		sArgument += "DATA0=" + mMessage + "\n";
		CrashlogDaemonCmdFile.createCrashlogdCmdFile(CrashlogDaemonCmdFile.Command.APLOG, sArgument, context);

		return null;
	}

	@Override
	protected void onProgressUpdate(Void... params) {
	}

	protected void onPostExecute(Void... params) {

	}

}
