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

import java.io.IOException;

import java.util.concurrent.TimeUnit;

import com.intel.commands.crashinfo.CrashInfo;
import com.intel.commands.crashinfo.DBManager;
import com.intel.commands.crashinfo.ISubCommand;
import com.intel.commands.crashinfo.option.OptionData;
import com.intel.commands.crashinfo.option.Options;
import com.intel.commands.crashinfo.option.Options.Multiplicity;

import android.os.SystemProperties;

public class Status implements ISubCommand {

	public static final String PATH_LOGS = SystemProperties.get("persist.crashlogd.root", "/logs") + "/";
	public static final String PATH_SD_LOGS = "/mnt/sdcard/logs";
	String[] myArgs;
	Options myOptions;

	public Status(){

	}

	@Override
	public int execute() throws IOException{
		OptionData mainOp = myOptions.getMainOption();
		if (mainOp == null){
			return execBaseStatus();
		}else if (mainOp.getKey().equals("--uptime")){
			return execUptime();
		}else if (mainOp.getKey().equals(Options.HELP_COMMAND)){
			myOptions.generateHelp();
			return 0;
		}else{
			System.out.println("error : unknown op - " + mainOp.getKey());
			return -1;
		}
	}


	private int execUptime() throws IOException{
		DBManager aDB = new DBManager();
		if (!aDB.isOpened()){
			throw new IOException("Database not opened!");
		}

		long duration = aDB.getCurrentUptime() ;
		aDB.close();
		if (duration >=0){
			long seconds = TimeUnit.SECONDS.toSeconds(duration) % 60;
			long days = TimeUnit.SECONDS.toDays(duration);
			long hours = TimeUnit.SECONDS.toHours(duration)% 24;
			long minutes = TimeUnit.SECONDS.toMinutes(duration) % 60;
			System.out.println( "Uptime since the last software update (SWUPDATE event):");
			if (days > 0) {
				System.out.println( days + " days");
			}
			if (hours > 0) {
				System.out.println( hours + " hours");
			}
			if (minutes > 0) {
				System.out.println( minutes + " minutes");
			}
			System.out.println(seconds + " seconds");
		}else{
			System.out.println("Error : No UPTIME found");
		}

		return 0;
	}

	private int execBaseStatus(){
		try {
			displayDbstatus();
			System.out.println("Main Path for logs : " + PATH_LOGS);
			System.out.println("Api version : "  + CrashInfo.API_VERSION);
			System.out.println("Organization : "  + com.intel.parsing.ParsableEvent.ORGANIZATION_MCG);
		}catch (Exception e) {
			System.out.println("Exception : "+e.toString());
			return -1;
		}
		return 0;
	}

	@Override
	public void setArgs(String[] subArgs) {
		myArgs = subArgs;
		myOptions = new Options(subArgs, "Status gives general information about crash events");
		myOptions.addMainOption("--uptime", "-u", "", false, Multiplicity.ZERO_OR_ONE, "Gives the uptime of the phone sonce last swupdate");
	}

	@Override
	public boolean checkArgs() {
		return myOptions.check();
	}

	private void displayDbstatus() throws IOException{
		DBManager aDB = new DBManager();
		if (!aDB.isOpened()){
			throw new IOException("Database not opened!");
		}

		System.out.println("Version database : "  + aDB.getVersion());
		System.out.println("Number of critical crash : "  +aDB.getNumberEventByCriticty(true));
		System.out.println("Number of events : "  +aDB.getNumberEventByCriticty(false));
		aDB.close();
	}
}
