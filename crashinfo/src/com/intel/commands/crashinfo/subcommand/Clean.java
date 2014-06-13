/* crashinfo - Clean manages the cleaning of log folders
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

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;

import com.intel.commands.crashinfo.DBManager;
import com.intel.commands.crashinfo.ISubCommand;
import com.intel.commands.crashinfo.option.OptionData;
import com.intel.commands.crashinfo.option.Options;
import com.intel.commands.crashinfo.option.Options.Multiplicity;

public class Clean implements ISubCommand {
	public static final String OPTION_CLEAN_TIME = "--filter-time";
	public static final String OPTION_CLEAN_FORCE = "--force-all";
	public static final String OPTION_CLEAN_ID = "--filter-id";


	String[] myArgs;
	Options myOptions;

	@Override
	public int execute() throws IOException{
		OptionData mainOp = myOptions.getMainOption();
		if (mainOp == null){
			baseClean();
		}else if (mainOp.getKey().equals(OPTION_CLEAN_FORCE)){
			forceClean();
		}else if (mainOp.getKey().equals(OPTION_CLEAN_TIME)){
			String sTmpValue = mainOp.getValues(0);
			timeClean(sTmpValue);
		}else if (mainOp.getKey().equals(OPTION_CLEAN_ID)){
			String sTmpValue =  mainOp.getValues(0);
			if (sTmpValue != null){
				idClean(Integer.parseInt(sTmpValue));
			}
		}else if (mainOp.getKey().equals(Options.HELP_COMMAND)){
			myOptions.generateHelp();
			return 0;
		}else{
			System.out.println("error : unknown op - " + mainOp.getKey());
			return -1;
		}
		System.out.println("Clean : OK");
		return 0;
	}

	@Override
	public void setArgs(String[] subArgs) {
		myArgs = subArgs;
		myOptions = new Options(subArgs, "Clean enable to delete log folder. It also cleans crashdir in database");
		myOptions.addMainOption(OPTION_CLEAN_FORCE, "-f", "", false, Multiplicity.ZERO_OR_ONE,"Clean all logs");
		myOptions.addMainOption(OPTION_CLEAN_TIME, "-t", ".*", true, Multiplicity.ZERO_OR_ONE,"Clean all log folder before the time given (time format example:2012-05-29/13:33:41)");
		myOptions.addMainOption(OPTION_CLEAN_ID, "-i", "(\\d)*", true, Multiplicity.ZERO_OR_ONE,"Clean log folder found for row_id given");
	}

	@Override
	public boolean checkArgs() {
		return myOptions.check();
	}

	private void baseClean() throws IOException{
		DBManager aDB = new DBManager();
		if (!aDB.isOpened()){
			throw new IOException("Database not opened!");
		}

		String[] usedLogsDir = aDB.getAllLogsDir();
		aDB.close();
		if (usedLogsDir != null){
			File logFolder = new File(Status.PATH_LOGS);
			File SDlogFolder = new File(Status.PATH_SD_LOGS);
			folderClean(usedLogsDir,logFolder);
			folderClean(usedLogsDir,SDlogFolder);
		}
	}

	private void folderClean(String[] ExceptLogsDir, File folderToClean){
		if (folderToClean.isDirectory()){
			String[] sLogFiles = folderToClean.list();
			if (sLogFiles!= null){
				java.util.regex.Pattern  pattern = java.util.regex.Pattern.compile("(stats\\d(\\d)*)|(crashlog\\d(\\d)*)");
				for (int i = 0; i < sLogFiles.length; i++) {
					boolean bToClean = true;
					//Only clean stats and crashlog pattern
					Matcher tmpMatcher = pattern.matcher(sLogFiles[i]);
					if (tmpMatcher.matches()){
						File curFile = new File(folderToClean.getAbsolutePath() + File.separator +  sLogFiles[i]);
						if (curFile.isDirectory()){
							for (int j = 0; j < ExceptLogsDir.length; j++) {
								if (curFile.getAbsolutePath().equals(ExceptLogsDir[j])){
									//Found, not to be cleaned
									bToClean = false;
									break;
								}
							}
							if (bToClean){
								System.out.println("Cleaning file : " + i);
								deleteFolder(curFile);
							}
						}
					}
				}
			}
		}
	}

	private void timeClean(String sTimeValue) throws IOException{
		DBManager aDB = new DBManager(true);
		if (!aDB.isOpened()){
			throw new IOException("Database not opened!");
		}

		String[] logsToclean = aDB.getLogsDirByTime(sTimeValue);
		if (logsToclean != null){
			for(String sLog : logsToclean){
				File logFolder = new File(sLog);
				if (logFolder.isDirectory()){
					deleteFolder(logFolder);
					System.out.println(sLog + " cleaned ");
				}
			}
			aDB.cleanCrashDirByTime(sTimeValue);
		}
		aDB.close();
	}

	private void idClean(int iIDtoClean) throws IOException{
		System.out.println("ID clean " + iIDtoClean);
		DBManager aDB = new DBManager(true);
		if (!aDB.isOpened()){
			throw new IOException("Database not opened!");
		}

		String sDirToClean = aDB.getLogDirByID(iIDtoClean);
		if (sDirToClean.equals("")){
			System.out.println("Nothing to clean");
		}else{
			File logFolder = new File(sDirToClean);
			if (logFolder.isDirectory()){
				deleteFolder(logFolder);
				System.out.println(sDirToClean + " cleaned ");
			}
			aDB.cleanCrashDirByID(iIDtoClean);
		}
		aDB.close();
	}

	private void forceClean(){
		System.out.println("Force clean not implemented yet");
	}

	public static void deleteFolder(File folder) {
		if (folder!=null){
			File[] files = folder.listFiles();
			if(files!=null) {
				for(File f: files) {
					if(f.isDirectory()) {
						deleteFolder(f);
					} else {
						f.delete();
					}
				}
			}
			folder.delete();
		}
	}
}
