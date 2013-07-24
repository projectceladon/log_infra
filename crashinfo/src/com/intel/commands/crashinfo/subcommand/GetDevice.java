/*
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
/**
 * @Brief This class manages getDevice subcommand by reading 'device' table
 * from crashreport DB to return device info such as ssn, imei...
 */

package com.intel.commands.crashinfo.subcommand;

import com.intel.commands.crashinfo.ISubCommand;
import com.intel.commands.crashinfo.option.OptionData;
import com.intel.commands.crashinfo.option.Options;
import com.intel.commands.crashinfo.option.Options.Multiplicity;
import com.intel.commands.crashinfo.DBManager;

public class GetDevice implements ISubCommand {

	private static final String OPTION_JSON = "--json";
	String[] myArgs;
	Options myOptions;

	@Override
	public int execute() {
		OptionData mainOp = myOptions.getMainOption();
		if (mainOp == null){
			DBManager aDB = new DBManager();
			aDB.getDeviceInfo(DBManager.outputFormat.STANDARD);
		}
		else if (mainOp.getKey().equals(OPTION_JSON)){
			DBManager aDB = new DBManager();
			aDB.getDeviceInfo(DBManager.outputFormat.JSON);
		}
		else if (mainOp.getKey().equals(Options.HELP_COMMAND)){
			myOptions.generateHelp();
			return 0;
		}else{
			System.err.println("error : unknown op - " + mainOp.getKey());
			return -1;
		}
		return 0;
	}

	@Override
	public void setArgs(String[] subArgs) {
		myArgs = subArgs;
		myOptions = new Options(subArgs, "getdevice gives informations characterizing the device");
		myOptions.addMainOption(OPTION_JSON, "-j", "", false, Multiplicity.ZERO_OR_ONE, "Output under JSON format");
	}

	@Override
	public boolean checkArgs() {
		return myOptions.check();
	}
}