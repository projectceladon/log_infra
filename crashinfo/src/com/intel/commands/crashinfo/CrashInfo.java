/* crashinfo - entry point for command
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


import com.intel.commands.crashinfo.subcommand.*;

import android.os.Debug;
import android.os.Process;
import android.util.Log;

/**
 * Application managing crash information
 */
public class CrashInfo {

	public static final String TAG_HEADER= "<crashinfo>";
	public static final String Module = "crashinfo : ";
	/* Define the crashinfo version. Read by Crashinfo python library on host to
	 * get available functionalities and to manage backward compatibilities between
	 * Crashinfo running on device and CrashInfo python library running on host.
	 * Versions descriptions :
	 *  - Version 1 : - 'get_device' command added
	 *                - JSON output format for get_device and get_event commands
	 *                - Status command updated to return Api version
	 */
	public static final String API_VERSION = "3";

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
		if (resultCode != 0) {
			System.exit(resultCode);
		}
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

		try{
			return processArgs();
		}
		catch (Exception e) {
			System.err.println("Process Exception : " + e.toString());
			Log.e("crashinfo","Process Exception : " + e.toString());
			return -1;
		}
	}


	/**
	 * Print how to use this command.
	 */
	private void showUsage() {
		StringBuffer usage = new StringBuffer();
		usage.append("usage: crashinfo getevent [--detail --full --last --filter-(id/time/type/name) --uploaded --json) ]\n");
		usage.append("                 getctevent [--key <event id> --fileinfo <output folder>]\n");
		usage.append("                 status [--uptime]\n");
		usage.append("                 buildid [--spec]\n");
		usage.append("                 clean [--filter-id --filter-time]\n");
		usage.append("                 uploadstate [--filter-id --invalid-(event/log) --log]\n");
		usage.append("                 analyzeevent [--help for parameter list]\n");
		usage.append("                 getbz\n");
		usage.append("                 getdevice [--json]\n");

		System.err.println(usage.toString());
	}

	private int processArgs() throws Exception {
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
			}else if (sCurArg.equals("getctevent")) {
				mySubCommand = new GetCrashToolEvent();
			}else if (sCurArg.equals("status")) {
				mySubCommand = new Status();
			}else if (sCurArg.equals("clean")) {
				mySubCommand = new Clean();
			}else if (sCurArg.equals("uploadstate")) {
				mySubCommand = new UploadState();
			}
			else if (sCurArg.equals("analyzeevent")) {
				mySubCommand = new AnalyzeEvent();
			}
			else if (sCurArg.equals("getbz")) {
				mySubCommand = new GetBZ();
			}
			else if (sCurArg.equals("getdevice")) {
				mySubCommand = new GetDevice();
			}
			if (mySubCommand != null){
				mySubCommand.setArgs(getSubArgs());
				if (mySubCommand.checkArgs()){
					Log.i("crashinfo","execution of : " + getFullArgsInString());
					iResultCode = mySubCommand.execute();
				}else{
					iResultCode = -2;
					Log.i("crashinfo","checkArgs failed : " + getFullArgsInString());
					System.err.println("checkArgs failed : ");
					showUsage();
				}
			}else{
				Log.i("crashinfo","unknown command : " + getFullArgsInString());
				showUsage();
			}
		}
		Log.i("crashinfo","Result code: " + iResultCode );
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

	private String getFullArgsInString() {
		String result = "";
		for (int i = 0; i < mArgs.length; i++) {
			result += " " +  mArgs[i];
		}
		return result;
	}

	private String[] getSubArgs() {
		if (mNextArg >= mArgs.length) {
			return null;
		}
		String[] result = new String[mArgs.length -mNextArg];
		for (int i = mNextArg; i < mArgs.length; i++) {
			result[i - mNextArg ] = mArgs[i];
		}
		return result;
	}

	public static void outputCrashinfo(String sOutput, boolean bUseTag) {
		if (bUseTag){
			System.out.println(TAG_HEADER+sOutput);
		}else{
			System.out.println(sOutput);
		}
	}

}
