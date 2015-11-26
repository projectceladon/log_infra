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


import java.io.Writer;
import java.util.ArrayList;
import com.intel.parsing.*;

import com.intel.commands.crashinfo.ISubCommand;
import com.intel.commands.crashinfo.option.OptionData;
import com.intel.commands.crashinfo.option.Options;
import com.intel.commands.crashinfo.option.Options.Multiplicity;

public class AnalyzeEvent implements ISubCommand {

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
