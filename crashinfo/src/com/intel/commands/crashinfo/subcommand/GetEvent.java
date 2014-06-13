/* crashinfo - getEvent manages display of crashreport event (connection to database)
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

import java.io.IOException;

import java.util.ArrayList;

import com.intel.commands.crashinfo.DBManager;
import com.intel.commands.crashinfo.ISubCommand;
import com.intel.commands.crashinfo.option.OptionData;
import com.intel.commands.crashinfo.option.Options;
import com.intel.commands.crashinfo.option.Options.Multiplicity;

public class GetEvent implements ISubCommand {

	public static final String OPTION_TIME = "--filter-time";
	public static final String OPTION_ID = "--filter-id";
	public static final String OPTION_TYPE = "--filter-type";
	public static final String OPTION_NAME = "--filter-name";
	public static final String OPTION_UPLOADED = "--uploaded";
	public static final String OPTION_FULL = "--full";
	public static final String OPTION_DETAIL = "--detail";
	public static final String OPTION_LAST = "--last";
	public static final String OPTION_REVERSE= "--reverse";
	public static final String OPTION_HEADER= "--header";
	public static final String OPTION_JSON = "--json";

	String[] myArgs;
	Options myOptions;
	@Override
	public int execute() throws IOException{
		DBManager aDB = new DBManager();
		if (!aDB.isOpened()){
			throw new IOException("Database not opened!");
		}

		int iResult = 0;
		OptionData mainOp = myOptions.getMainOption();
		ArrayList<OptionData> mySubOptions = myOptions.getSubOptions();
		try{
			if (mainOp == null){
				aDB.getEvent(DBManager.EventLevel.BASE,mySubOptions);
			}else if (mainOp.getKey().equals(OPTION_FULL)){
				aDB.getEvent(DBManager.EventLevel.FULL,mySubOptions);
			}else if (mainOp.getKey().equals(OPTION_DETAIL)){
				aDB.getEvent(DBManager.EventLevel.DETAIL,mySubOptions);
			}else if (mainOp.getKey().equals(Options.HELP_COMMAND)){
				myOptions.generateHelp();
			}else{
				System.out.println("error : unknown op - " + mainOp.getKey());
				iResult = -1;
			}
		}
		catch (Exception e){
			iResult = -3;
		}
		finally {
			if (aDB != null){
				aDB.close();
			}
		}

		return iResult;
	}

	@Override
	public void setArgs(String[] subArgs) {
		myArgs = subArgs;
		myOptions = new Options(subArgs, "Getevent  display content of crash events datables. It is possible to filter the result with options");
		myOptions.addMainOption(OPTION_FULL, "-f", "", false, Multiplicity.ZERO_OR_ONE, "Gives all columns of events");
		myOptions.addMainOption(OPTION_DETAIL, "-d", "", false, Multiplicity.ZERO_OR_ONE, "Gives detail columns of events");
		myOptions.addSubOption(OPTION_LAST, "-l", "", false, Multiplicity.ZERO_OR_ONE, "Returns the last event");
		myOptions.addSubOption(OPTION_ID, "-i", "(\\d)*", true, Multiplicity.ZERO_OR_ONE, "Filter by row_id given");
		myOptions.addSubOption(OPTION_TYPE, "-t", ".*", true, Multiplicity.ZERO_OR_ONE, "Filter by type given");
		myOptions.addSubOption(OPTION_NAME, "-n", ".*", true, Multiplicity.ZERO_OR_ONE, "Filter by name given");
		myOptions.addSubOption(OPTION_UPLOADED, "-u", "0|1", true, Multiplicity.ZERO_OR_ONE, "Filter by event uploaded or not depending on value given (0 or 1)");
		myOptions.addSubOption(OPTION_TIME, "-t", ".*", true, Multiplicity.ZERO_OR_ONE, "Filter by event occured after time given (time format example:2012-05-29/13:33:41)");
		myOptions.addSubOption(OPTION_REVERSE, "-r", "", false, Multiplicity.ZERO_OR_ONE, "Change display order");
		myOptions.addSubOption(OPTION_HEADER, "-a", "", false, Multiplicity.ZERO_OR_ONE, "Add crashinfo TAG at beginning of the output");
		myOptions.addSubOption(OPTION_JSON, "-j", "", false, Multiplicity.ZERO_OR_ONE, "Output under GSON format");
	}

	@Override
	public boolean checkArgs() {
		return myOptions.check();
	}
}
