/* crashinfo - uploadState enable to update upload state in database
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
	public int execute() {
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

	private int updateUploadByID(int rowId){
		DBManager aDB = new DBManager(true);
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
			return -1;
		}
		if (bLog && bLogInvalid) {
			System.out.println(CrashInfo.Module+ "Error : option \"" + OPTION_UPLOADED_LOG + "\" is incompatible with option \"" + OPTION_INVALID_LOG + "\"");
			return -1;
		}
		if (bEventInvalid && bLogInvalid) {
			System.out.println(CrashInfo.Module+ "Error : option \"" + OPTION_INVALID_LOG + "\" is incompatible with option \"" + OPTION_INVALID_EVENT + "\"");
			return -1;
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
		return 0;
	}

	@Override
	public void setArgs(String[] subArgs) {
		myArgs = subArgs;
		myOptions = new Options(subArgs, "uploadState enable to update upload state in database");
		myOptions.addMainOption("--filter-id", "-i",  "(\\d)*", true, Multiplicity.ONCE, "ID to change for upload state");
		myOptions.addSubOption(OPTION_UPLOADED_LOG, "-l",  "", false, Multiplicity.ZERO_OR_ONE, "Set Event logfile upload state to Uploaded");
		myOptions.addSubOption(OPTION_INVALID_EVENT, "",  "", false, Multiplicity.ZERO_OR_ONE, "Set Event and logfile upload state to Invalid");
		myOptions.addSubOption(OPTION_INVALID_LOG, "",  "", false, Multiplicity.ZERO_OR_ONE, "Set Event logfile upload state to Invalid");
	}

	@Override
	public boolean checkArgs() {
		return myOptions.check();
	}

}