/* Copyright (C) 2019 Intel Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
