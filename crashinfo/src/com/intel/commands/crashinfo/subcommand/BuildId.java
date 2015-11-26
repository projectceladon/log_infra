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

import com.intel.commands.crashinfo.CrashInfo;
import com.intel.commands.crashinfo.ISubCommand;
import com.intel.commands.crashinfo.option.OptionData;
import com.intel.commands.crashinfo.option.Options;
import com.intel.commands.crashinfo.option.Options.Multiplicity;

import android.os.SystemProperties;
import android.util.Log;

public class BuildId implements ISubCommand {
	public static final String OPTION_HEADER= "--header";
	String[] myArgs;
	Options myOptions;
	boolean bUseTag;

	@Override
	public int execute() {
		bUseTag = false;
		for (OptionData aSubOption : myOptions.getSubOptions()){
			if (aSubOption.getKey().equals(OPTION_HEADER)){
				bUseTag = true;
			}
		}
		OptionData mainOp = myOptions.getMainOption();
		if (mainOp == null){
			generateBuildSignature();
		}else if (mainOp.getKey().equals("--spec")){
			generateSpec();
		}else if (mainOp.getKey().equals(Options.HELP_COMMAND)){
			myOptions.generateHelp();
			return 0;
		}else{
			System.err.println("error : unknown op - " + mainOp.getKey());
			return -1;
		}
		return 0;
	}

	private void generateBuildSignature(){
		String sBuildId = android.os.Build.VERSION.INCREMENTAL;
		String sFingerPrint = android.os.Build.FINGERPRINT;
		String sKernelVersion = getProperty("sys.kernel.version");
		String sBuildUserHostname = getProperty("ro.build.user")+"@"+getProperty("ro.build.host");
		String sModemVersion = getProperty("gsm.version.baseband");
		String sIfwiVersion = getProperty("sys.ifwi.version");
		String sIafwVersion = getProperty("sys.ia32.version");
		String sScufwVersion = getProperty("sys.scu.version");
		String sPunitVersion = getProperty("sys.punit.version");
		String sValhooksVersion = getProperty("sys.valhooks.version");

		String sSeparator = ",";
		String sCompleteBuildId = sBuildId + sSeparator
				+ sFingerPrint + sSeparator
				+ sKernelVersion + sSeparator
				+ sBuildUserHostname + sSeparator
				+ sModemVersion + sSeparator
				+ sIfwiVersion + sSeparator
				+ sIafwVersion + sSeparator
				+ sScufwVersion + sSeparator
				+ sPunitVersion + sSeparator
				+ sValhooksVersion;
		CrashInfo.outputCrashinfo(sCompleteBuildId,bUseTag);
	}

	private void generateSpec(){
		CrashInfo.outputCrashinfo("Build signature is composed of :",bUseTag);
		CrashInfo.outputCrashinfo("BuildId",bUseTag);
		CrashInfo.outputCrashinfo("FingerPrint",bUseTag);
		CrashInfo.outputCrashinfo("KernelVersion",bUseTag);
		CrashInfo.outputCrashinfo("BuildUserHostname",bUseTag);
		CrashInfo.outputCrashinfo("ModemVersion",bUseTag);
		CrashInfo.outputCrashinfo("IfwiVersion",bUseTag);
		CrashInfo.outputCrashinfo("IafwVersion",bUseTag);
		CrashInfo.outputCrashinfo("ScufwVersion",bUseTag);
		CrashInfo.outputCrashinfo("PunitVersion",bUseTag);
		CrashInfo.outputCrashinfo("ValhooksVersion",bUseTag);
	}

	private String getProperty(String name) {
		try {
			String property = SystemProperties.get(name, "");
			return property;
		} catch (IllegalArgumentException e) {
			Log.w("crashinfo","Propery not available : "+name);
		}
		return "";
	}

	@Override
	public void setArgs(String[] subArgs) {
		myArgs = subArgs;
		myOptions = new Options(subArgs, "Buildid gives the build signature of the device");
		myOptions.addMainOption("--spec", "-s", "", false, Multiplicity.ZERO_OR_ONE, "Displays specification of build signature");
		myOptions.addSubOption(OPTION_HEADER, "-a", "", false, Multiplicity.ZERO_OR_ONE, "Add crashinfo TAG at beginning of the output");
	}

	@Override
	public boolean checkArgs() {
		return myOptions.check();
	}
}
