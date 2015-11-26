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

import java.util.ArrayList;

import com.intel.commands.crashinfo.CrashInfo;
import com.intel.commands.crashinfo.DBManager;
import com.intel.commands.crashinfo.ISubCommand;
import com.intel.commands.crashinfo.option.OptionData;
import com.intel.commands.crashinfo.option.Options;
import com.intel.commands.crashinfo.option.Options.Multiplicity;

public class UploadState implements ISubCommand {

	String[] myArgs;
	Options myOptions;

	public static final String OPTION_UPLOADED_LOG = "--log";
	public static final String OPTION_INVALID_EVENT = "--invalid-event";
	public static final String OPTION_INVALID_LOG = "--invalid-log";

	public UploadState(){

	}

	@Override
	public int execute() throws IOException{
		OptionData mainOp = myOptions.getMainOption();
		if (mainOp == null){
			return -1;
		}else if (mainOp.getKey().equals("--filter-id")){
			String sTmpValue =  mainOp.getValues(0);
			if (sTmpValue != null){
				int iRowId;
				try{
					iRowId = Integer.parseInt(sTmpValue);
				}
				catch (Exception e) {
					System.out.println("error : parsingId - " + sTmpValue);
					return -1;
				}
				return updateUploadByID(iRowId);
			}
			return -1;
		}else if (mainOp.getKey().equals(Options.HELP_COMMAND)){
			myOptions.generateHelp();
			return 0;
		}else{
			System.out.println("error : unknown op - " + mainOp.getKey());
			return -1;
		}
	}

	private int updateUploadByID(int rowId) throws IOException{
		DBManager aDB = null;
		int iResult = 0;
		boolean bLog = false;
		boolean bLogInvalid = false;
		boolean bEventInvalid = false;
		ArrayList<OptionData> mySubOptions = myOptions.getSubOptions();
		/* Get input options*/
		for (OptionData aSubOption : mySubOptions) {
			bLog |= aSubOption.getKey().equals(OPTION_UPLOADED_LOG);
			bEventInvalid |= aSubOption.getKey().equals(OPTION_INVALID_EVENT);
			bLogInvalid |= aSubOption.getKey().equals(OPTION_INVALID_LOG);
		}
		/* Displays options incompatibility error messages if necessary and exits*/
		if (bLog && bEventInvalid) {
			System.out.println(CrashInfo.Module+ "Error : option \"" + OPTION_UPLOADED_LOG + "\" is incompatible with option \"" + OPTION_INVALID_EVENT + "\"");
			iResult = -1;
		}
		if (bLog && bLogInvalid) {
			System.out.println(CrashInfo.Module+ "Error : option \"" + OPTION_UPLOADED_LOG + "\" is incompatible with option \"" + OPTION_INVALID_LOG + "\"");
			iResult = -1;
		}
		if (bEventInvalid && bLogInvalid) {
			System.out.println(CrashInfo.Module+ "Error : option \"" + OPTION_INVALID_LOG + "\" is incompatible with option \"" + OPTION_INVALID_EVENT + "\"");
			iResult = -1;
		}
		if (iResult == 0){
			aDB = new DBManager(true);
			if (!aDB.isOpened()){
				throw new IOException("Database not opened!");
			}

			/* Performs actions*/
			if (bEventInvalid)
				aDB.updateUploadStateByID(rowId, DBManager.eventUploadState.EVENT_INVALID);
			else if (bLogInvalid)
				aDB.updateUploadStateByID(rowId, DBManager.eventUploadState.LOG_INVALID);
			else if (bLog)
				aDB.updateUploadStateByID(rowId, DBManager.eventUploadState.LOG_UPLOADED);
			else
				aDB.updateUploadStateByID(rowId, DBManager.eventUploadState.EVENT_UPLOADED);
			aDB.close();
		}
		return iResult;
	}

	@Override
	public void setArgs(String[] subArgs) {
		myArgs = subArgs;
		myOptions = new Options(subArgs, "uploadState enable to update upload state in database");
		myOptions.addMainOption("--filter-id", "-i",  "(\\d)*", true, Multiplicity.ONCE, "ID to change for upload state");
		myOptions.addSubOption(OPTION_UPLOADED_LOG, "-l",  "", false, Multiplicity.ZERO_OR_ONE, "Set Event logfile upload state to Uploaded");
		myOptions.addSubOption(OPTION_INVALID_EVENT, "",  "", false, Multiplicity.ZERO_OR_ONE, "Set Event and logfile upload state to Invalid");
		myOptions.addSubOption(OPTION_INVALID_LOG, "",  "", false, Multiplicity.ZERO_OR_ONE, "Set Event upload state to Uploaded and logfile upload state to Invalid");
	}

	@Override
	public boolean checkArgs() {
		return myOptions.check();
	}

}
