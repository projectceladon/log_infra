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
