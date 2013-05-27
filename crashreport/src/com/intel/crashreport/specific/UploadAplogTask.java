/* Phone Doctor (CLOTA)
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
 * Author: Charles-Edouard Vidoine <charles.edouardx.vidoine@intel.com>
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

	public UploadAplogTask(int iLog, Context ctx){
		m_iLog = iLog;
		context = ctx;
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
		CrashlogDaemonCmdFile.CreateCrashlogdCmdFile(CrashlogDaemonCmdFile.Command.APLOG, sArgument, context);

		return null;
	}

	@Override
	protected void onProgressUpdate(Void... params) {
	}

	protected void onPostExecute(Void... params) {

	}

}
