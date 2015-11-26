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

package com.intel.commands.crashinfo.subcommand;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;

import android.util.Log;

import com.intel.parsing.*;
import com.intel.commands.crashinfo.CrashInfo;
import com.intel.commands.crashinfo.DBManager;
import com.intel.commands.crashinfo.ISubCommand;
import com.intel.commands.crashinfo.option.OptionData;
import com.intel.commands.crashinfo.option.Options;
import com.intel.commands.crashinfo.option.Options.Multiplicity;
import com.intel.crashtoolserver.bean.Event;

public class GetCrashToolEvent implements ISubCommand {

	String[] myArgs;
	Options myOptions;
	Writer myOutput = null;

	public GetCrashToolEvent(){

	}

	@Override
	public int execute() throws IOException {
		OptionData mainOp = myOptions.getMainOption();
		if (mainOp == null){
			return execGetCrashToolEvent();
		}else if (mainOp.getKey().equals(Options.HELP_COMMAND)){
			myOptions.generateHelp();
			return 0;
		}else{
			System.out.println("error : unknown op - " + mainOp.getKey());
			return -1;
		}
	}


	private int execGetCrashToolEvent() throws IOException {
		String sCrashID = "";
		String sCacheDir = "";
		Object output = null;

		ArrayList<OptionData> mySubOptions = myOptions.getSubOptions();
		for (OptionData aSubOption : mySubOptions) {
			if (aSubOption.getKey().equals("--key")){
				sCrashID = aSubOption.getValues(0);
			}
			if (aSubOption.getKey().equals("--fileinfo")){
				sCacheDir = aSubOption.getValues(0);
			}
		}

		DBManager aDB = new DBManager();
		if (!aDB.isOpened()){
			throw new IOException("Database not opened!");
		}

		if (sCacheDir != null && !sCacheDir.isEmpty()) {
			File cacheDir = new File(sCacheDir);
			if ((cacheDir == null) || !cacheDir.exists()) {
				throw new IOException("Supplied path does not exist");
			}

			output = aDB.getFileInfo(sCrashID, cacheDir);
		}
		else {
			output = aDB.getEvent(sCrashID);
		}

		if (output == null)
			return 0;

		aDB.printToJsonFormat(output);
		return 0;
	}


	@Override
	public void setArgs(String[] subArgs) {
		myArgs = subArgs;
		myOptions = new Options(subArgs, "GetCrashToolEvent is used to generate an output similar to crashtool");
		myOptions.addSubOption("--key", "-k", ".*", true, Multiplicity.ZERO_OR_ONE, "Indicates the ID of the event");
		myOptions.addSubOption("--fileinfo", "-f", ".*", true, Multiplicity.ZERO_OR_ONE, "Outputs a FileInfo object for the specified crash, given a target path, where the actual crashfile will be created.");
	}

	@Override
	public boolean checkArgs() {
		return myOptions.check();
	}
}
