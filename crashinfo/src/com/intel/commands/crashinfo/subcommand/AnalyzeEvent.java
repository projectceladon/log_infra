/* crashinfo - AnalyzeEvent is a copy of analyze_crash script converted in JAVA
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

package com.intel.commands.crashinfo.subcommand;


import java.io.Writer;
import java.util.ArrayList;
import com.intel.parsing.*;

import com.intel.commands.crashinfo.ISubCommand;
import com.intel.commands.crashinfo.option.OptionData;
import com.intel.commands.crashinfo.option.Options;
import com.intel.commands.crashinfo.option.Options.Multiplicity;

public class AnalyzeEvent implements ISubCommand {

	public static final String PATH_UUID = "/logs/uuid.txt";
	String[] myArgs;
	Options myOptions;
	Writer myOutput = null;

	public AnalyzeEvent(){

	}

	@Override
	public int execute() {
		OptionData mainOp = myOptions.getMainOption();
		if (mainOp == null){
			return execAnalyzeEvent();
		}else if (mainOp.getKey().equals(Options.HELP_COMMAND)){
			myOptions.generateHelp();
			return 0;
		}else{
			System.out.println("error : unknown op - " + mainOp.getKey());
			return -1;
		}
	}


	private int execAnalyzeEvent() {
		String sOutput = null;
		String sTag = "";
		String sCrashID = "";
		String sUptime = "";
		String sBuild = "";
		String sBoard = "";
		String sDate = "";
		String sImei = "";


		ArrayList<OptionData> mySubOptions = myOptions.getSubOptions();
		for (OptionData aSubOption : mySubOptions) {
			if (aSubOption.getKey().equals("--path")){
				sOutput = aSubOption.getValues(0);
			}else if (aSubOption.getKey().equals("--type")){
				sTag = aSubOption.getValues(0);
			}else if (aSubOption.getKey().equals("--key")){
				sCrashID = aSubOption.getValues(0);
			}else if (aSubOption.getKey().equals("--uptime")){
				sUptime = aSubOption.getValues(0);
			}else if (aSubOption.getKey().equals("--footprint")){
				sBuild = aSubOption.getValues(0);
			}else if (aSubOption.getKey().equals("--boardversion")){
				sBoard = aSubOption.getValues(0);
			}else if (aSubOption.getKey().equals("--date")){
				sDate = aSubOption.getValues(0);
			}else if (aSubOption.getKey().equals("--imei")){
				sImei = aSubOption.getValues(0);
			}
		}

		if ((sOutput!=null)&& (sTag!=null)){
			MainParser aParser = new MainParser(sOutput, sTag, sCrashID, sUptime,sBuild, sBoard, sDate, sImei);
			return aParser.execParsing();
		}
		else
		{
			System.out.println("Path or Type is null");
			return -1;
		}
	}


	@Override
	public void setArgs(String[] subArgs) {
		myArgs = subArgs;
		myOptions = new Options(subArgs, "AnalyzeEvent is used for event process generation (crashlogd)");
		myOptions.addSubOption("--type", "-t", ".*", true, Multiplicity.ONCE, "Gives the type of the event");
		myOptions.addSubOption("--path", "-p", ".*", true, Multiplicity.ONCE, "Gives the path for crashfile generation");
		myOptions.addSubOption("--key", "-k", ".*", true, Multiplicity.ONCE, "Gives the ID of the event");
		myOptions.addSubOption("--uptime", "-u", ".*", true, Multiplicity.ONCE, "Gives the uptime of the event");
		myOptions.addSubOption("--footprint", "-f", ".*", true, Multiplicity.ONCE, "Gives the footprint of the event");
		myOptions.addSubOption("--boardversion", "-b", ".*", true, Multiplicity.ONCE, "Gives the board version of the event");
		myOptions.addSubOption("--date", "-d", ".*", true, Multiplicity.ONCE, "Gives the date of the event");
		myOptions.addSubOption("--imei", "-i", ".*", true, Multiplicity.ONCE, "Gives the imei of the event");
	}

	@Override
	public boolean checkArgs() {
		return myOptions.check();
	}
}
