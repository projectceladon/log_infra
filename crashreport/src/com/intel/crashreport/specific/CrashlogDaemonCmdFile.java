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
 * Author: Jean Thiry <jeanx.thiry@intel.com>
 */

package com.intel.crashreport.specific;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import com.intel.crashreport.ApplicationPreferences;
import com.intel.crashreport.Log;
import com.intel.phonedoctor.Constants;

import android.content.Context;

/**
 * This class allows to create a file that shall trigger crashlog native daemon.
 * The file name and its content allows to specify various kind of actions
 * that will be performed by crashlog daemon.
 */
public class CrashlogDaemonCmdFile {
	private static final String Module = "CrashlogDaemonCmdFile: ";

	private static File mCommandFile;

	/* Define aplogs directory that is watched by crashlogd**/
	public static final String APLOGS_DIR = Constants.LOGS_DIR + "/aplogs/";
	public static final String TRIGGER = "_trigger";
	public static final String CMD = "_cmd";

	/** Define possible values for commands **/
	public static enum Command {
		/** Create an aplog trigger file for aplogs uploading*/
		APLOG,
		/** Create a BZ trigger file for BZ event creation*/
		BZ,
		/** Create a command file for deleting crashlogxx directories*/
		DELETE}

	/**
	 * Creates a file that shall trigger crashlog daemon watcher.
	 * Those files have different names depending on input CmdType that
	 * involves different behavior of crashlog daemon.
	 *
	 * @param CmdType is the type of the file to create
	 * @param Argument is a line written in the file
	 * @param aContext is the caller context
	 */
	public static boolean CreateCrashlogdCmdFile(Command CmdType, String Argument, Context aContext) {
		ArrayList<String> sArguments = new ArrayList<String>();
		sArguments.add(Argument);
		return CreateCrashlogdCmdFile( CmdType, sArguments, aContext);
	}

	/**
	 * Creates a file that shall trigger crashlog daemon watcher.
	 * Those files have different names depending on input CmdType that
	 * involves different behavior of crashlog daemon.
	 *
	 * @param CmdType is the type of the file to create
	 * @param Arguments shall contains line(s) written in the file
	 * @param aContext is the caller context
	 */
	public static synchronized boolean CreateCrashlogdCmdFile(Command CmdType, ArrayList<String> Arguments, Context aContext) {

		String sfilePath ="";
		switch (CmdType) {
		case APLOG:
		case BZ:
			sfilePath=(APLOGS_DIR+CmdType.toString().toLowerCase()+TRIGGER);
			break;
		case DELETE:
			sfilePath=(APLOGS_DIR+CmdType.toString().toLowerCase()+CMD);
			Arguments.add(0, "ACTION=DELETE\n");
			break;
		}
		/**Create file and manage case where previous file already exist and has not been
		 * treated yet by crashlogd */
		mCommandFile = new File(sfilePath);
		if (CmdType == Command.DELETE && mCommandFile.exists()) {
			int filenameIndex = new ApplicationPreferences(aContext).getNewTriggerFileIndex();
			sfilePath=(APLOGS_DIR + CmdType.toString().toLowerCase()+ filenameIndex + CMD);
			mCommandFile = new File(sfilePath);
		}
		FileOutputStream f = null;
		try {
			f = new FileOutputStream(mCommandFile);
			BufferedOutputStream write = new BufferedOutputStream(f);
			for (int i = 0;i < Arguments.size() ; i++){
				write.write(Arguments.get(i).getBytes());
			}
			write.close();
			//wait for 10 seconds and check if file has been consumed
			try{
				Thread.sleep(10000);
			}
			catch(InterruptedException e){
				Log.d("CreateCrashlogdCmdFile : Interrupted Exception");
			}
			if (mCommandFile.exists()) {
				//warning : sleep time should be coherent with crashlogd processing time
				return false;
			}
		} catch (FileNotFoundException e) {
			Log.e(Module + "file "+sfilePath+" is not found");
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			Log.e(Module + "can't write in file "+sfilePath);
			e.printStackTrace();
			return false;
		}
		finally {
			if (f != null) {
				try {
					f.close();
				} catch (IOException e) {
					Log.w("IOException : " + e.getMessage());
				}
			}
		}
		return true;
	};
}