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
import com.intel.commands.crashinfo.DBManager;
import com.intel.commands.crashinfo.ISubCommand;
import com.intel.commands.crashinfo.option.OptionData;
import com.intel.commands.crashinfo.option.Options;

import java.io.IOException;

public class GetBZ implements ISubCommand {
	Options myOptions;
	String[] myArgs;

	public GetBZ(){

	}

	@Override
	public int execute() throws Exception{
		OptionData mainOp = myOptions.getMainOption();
		if (mainOp == null){
			return execGetBZ();
		}else if (mainOp.getKey().equals(Options.HELP_COMMAND)){
			myOptions.generateHelp();
			return 0;
		}else{
			System.out.println("error : unknown op - " + mainOp.getKey());
			return -1;
		}
	}

	private int execGetBZ() throws IOException{
		DBManager aDB = new DBManager();
		if (!aDB.isOpened()){
			throw new IOException("Database not opened!");
		}

		int iResult = 0;
		try {
			aDB.getBz();
		}catch (Exception e) {
			System.out.println("Exception : "+e.toString());
			iResult = -1;
		}
		aDB.close();
		return iResult;
	}

	@Override
	public void setArgs(String[] subArgs) {
		myArgs = subArgs;
		myOptions = new Options(subArgs, "GetBZ gives content of BZ database");
	}

	@Override
	public boolean checkArgs() {
		return myOptions.check();
	}

}
