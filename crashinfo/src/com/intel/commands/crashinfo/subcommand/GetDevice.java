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

import com.intel.commands.crashinfo.ISubCommand;
import com.intel.commands.crashinfo.option.OptionData;
import com.intel.commands.crashinfo.option.Options;
import com.intel.commands.crashinfo.option.Options.Multiplicity;
import com.intel.commands.crashinfo.DBManager;

import java.io.IOException;

public class GetDevice implements ISubCommand {

	private static final String OPTION_JSON = "--json";
	String[] myArgs;
	Options myOptions;

	@Override
	public int execute() throws IOException{
		int iResult = 0;
		DBManager aDB = null;
		OptionData mainOp = myOptions.getMainOption();
		if (mainOp == null){
			aDB = new DBManager();
			if (!aDB.isOpened()){
				throw new IOException("Database not opened!");
			}

			aDB.getDeviceInfo(DBManager.outputFormat.STANDARD);
			aDB.close();
		}
		else if (mainOp.getKey().equals(OPTION_JSON)){
			aDB = new DBManager();
			if (!aDB.isOpened()){
				throw new IOException("Database not opened!");
			}

			aDB.getDeviceInfo(DBManager.outputFormat.JSON);
			aDB.close();
		}
		else if (mainOp.getKey().equals(Options.HELP_COMMAND)){
			myOptions.generateHelp();
		}else{
			System.err.println("error : unknown op - " + mainOp.getKey());
			iResult = -1;
		}
		return iResult;
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
