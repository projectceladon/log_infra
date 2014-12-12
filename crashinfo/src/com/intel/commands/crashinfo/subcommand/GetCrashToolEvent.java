/*
 * Copyright (C) Intel 2015
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
 * Author: Nicolae Natea <nicolaex.natea@intel.com>
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
