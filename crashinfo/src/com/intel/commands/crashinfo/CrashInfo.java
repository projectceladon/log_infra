/* crashinfo
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
 * Author: Nicolas Benoit <nicolasx.benoit@intel.com>
 */

package com.intel.commands.crashinfo;

import com.intel.commands.crashinfo.ISubCommand;
import com.intel.commands.crashinfo.subcommand.*;


import android.os.Debug;
import android.os.Process;
import android.util.Log;

/**
 * Application that manage crash information
 */
public class CrashInfo {

	/**
	 * Crashinfo command
	 * <p>
	 */

	/** Command line arguments */
	private String[] mArgs;

	/** Current argument being parsed */
	private int mNextArg;




	/**
	 * Command-line entry point.
	 *
	 * @param args The command-line arguments
	 */
	public static void main(String[] args) {
		// Set the process name showing in "ps" or "top"
		Process.setArgV0("com.intel.commands.crashinfo");

		int resultCode = (new CrashInfo()).run(args);
		System.exit(resultCode);
	}

	/**
	 * Run the command!
	 *
	 * @param args The command-line arguments
	 * @return Returns a posix-style result code. 0 for no error.
	 */
	private int run(String[] args) {
		// Super-early debugger wait
		for (String s : args) {
			if ("--wait-dbg".equals(s)) {
				Debug.waitForDebugger();
			}
		}

		mArgs = args;
		mNextArg = 0;

		return processArgs();

	}


	/**
	 * Print how to use this command.
	 */
	private void showUsage() {
		StringBuffer usage = new StringBuffer();
		usage.append("usage: crashinfo [getevent Filter ...]\n");
		usage.append("                 [status]\n");
		usage.append("                 [buildid --spec]\n");
		usage.append("                 [clean]\n");

		System.err.println(usage.toString());
	}

	private int processArgs() {
		int iResultCode = -1;
		// quick (throwaway) check for unadorned command
		if (mArgs.length < 1) {
			Log.w("crashinfo","Command run without argument. ");
			showUsage();
			return -1;
		}
		ISubCommand mySubCommand = null;
		String sCurArg = nextArg();
		if (sCurArg == null){
			showUsage();
		}else if (sCurArg.equals("-h") || sCurArg.equals("--help")) {
			showUsage();
		}else{
			if (sCurArg.equals("buildid")) {
				mySubCommand = new BuildId();
			}else if (sCurArg.equals("getevent")) {
				mySubCommand = new GetEvent();
			}else if (sCurArg.equals("status")) {
				mySubCommand = new Status();
			}else if (sCurArg.equals("clean")) {
				mySubCommand = new Clean();
			}
			if (mySubCommand != null){
				mySubCommand.setArgs(GetSubArgs());
				if (mySubCommand.checkArgs()){
					iResultCode = mySubCommand.execute();
				}else{
					System.err.println("checkArgs failed");
					showUsage();
				}
			}else{
				showUsage();
			}
		}
		return iResultCode;
	}

	private String nextArg() {
		if (mNextArg >= mArgs.length) {
			return null;
		}
		String arg = mArgs[mNextArg];
		mNextArg++;
		return arg;
	}

	private String[] GetSubArgs() {
		if (mNextArg >= mArgs.length) {
			return null;
		}
		String[] result = new String[mArgs.length -mNextArg];
		for (int i = mNextArg; i < mArgs.length; i++) {
			result[i - mNextArg ] = mArgs[i];
		}
		return result;
	}

}
