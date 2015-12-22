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

package com.intel.crashreport;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.intel.phonedoctor.utils.FileOps;
import com.intel.crashreport.database.EventDB;
import com.intel.crashreport.common.IEventLog;
import com.intel.crashreport.core.Logger;
import android.database.SQLException;
import android.database.sqlite.SQLiteException;
import android.content.Context;

public class CrashLogs {

	private static final int BUFFER_SIZE = 1024;
	private static final IEventLog log = Logger.getLog();

	/**
	 * Compress a crashlog directory content and return it as a zipped file. If this zipped file
	 * already exists in the application cache directory, this file directly returned.
	 *
	 * @param context is the current application context
	 * @param crashDir is the crashlog directory to be returned as a compressed zip file.
	 * @param eventId is the id of the event associated to the input crashlog directory.
	 * @return the crashlog directory compressed as a zipped file named "EVENTeventId.zip"
	 * @throws SQLException if the event crashdir database key needs to be updated but database is not available
	 * @throws SQLiteException if the event crashdir database key needs to be updated but database is not available
	 */
	public static File getCrashLogsFile(Context context, String crashDir, String eventId) throws SQLException, SQLiteException {
		if ((crashDir == null) || (crashDir.isEmpty())) {
			return null;
		}
		File cacheDir = context.getCacheDir();
		if ((cacheDir == null) || !cacheDir.exists() ) {
			return null;
		}
		String crashLogsFileName = "EVENT"+eventId+".zip";
		File crashLogsFile = new File(cacheDir, crashLogsFileName);
		crashLogsFile.deleteOnExit();
		if (crashLogsFile.exists()) {
			//A crashlog zipped file already exists in cache directory
			log.w("getCrashLogsFile: " + crashLogsFileName + " exists in cachedir");
			try {
				if (FileOps.isValidZipFile(crashLogsFile))
					return crashLogsFile;
				else
					log.w("CrashLogs: unvalid zip file in cache "
						+ "dir: " + crashLogsFile.getName());
			} catch (IOException e) {
				log.w("CrashLogs: can't read zip file in cache dir: "
					+ crashLogsFile.getName());
			}
			if (!crashLogsFile.delete())
				log.w("CrashLogs: can't delete file: "
					+ crashLogsFile.getName());
		}
		//Nominal case : the crashlog directory needs to be compressed and returned as a zipped file
		log.d("getCrashLogsFile: start "+crashLogsFileName+" creation");
		try {
			return createCrashLogsZip(crashDir, crashLogsFileName, cacheDir);
		} catch (IllegalArgumentException e) {
			log.d("CrashLogs: exception while compressing crash directory: " + e);
		} catch (UnsupportedOperationException e) {
			log.d("CrashLogs: exception while compressing crash directory: " + e);
		}

		/* Event crashdir is empty so remove it in Event DB to avoid processing it again*/
		EventDB db = new EventDB(context);
		db.open();
		db.updateEventCrashdir(eventId, "");
		db.close();
		log.i("CrashLogs: event " + eventId
			+ " \'crashdir\' key reset in Event database");
		return null;
	}

	/**
	 * Write a zip file from the input crash directory crashDirPath. This file is named fileName
	 * and stored in outDir directory
	 *
	 * @param crashDirPath is the path of the directory to compress
	 * @param fileName is a string defining the name of the zip file to create. It shall NOT be null.
	 * @param outDir is the directory where the output zip file will be stored. It shall exist and NOT be null.
	 * @return the created zip file or null if crashDirPath directory is empty or creation failed
	 * @throws IllegalArgumentException if crashDirPath is not an existing directory
	 * @throws UnsupportedOperationException if crashDirPath is an empty directory
	 */
	private static File createCrashLogsZip(String crashDirPath, String fileName, File outDir) throws IllegalArgumentException, UnsupportedOperationException {
		File crashDir = new File(crashDirPath);
		if ( !(crashDir.exists() && crashDir.isDirectory()) ) {
			log.w("CrashLogs: createCrashLogsZip input " + crashDirPath
				+ " arg doesn't exist or is not a directory");
			throw new IllegalArgumentException();
		}
		/*Check crashlog directory is not empty*/
		File fileList[] = crashDir.listFiles();
		if ((fileList != null) && fileList.length > 0) {
			File crashLogsFile = new File(outDir, fileName); //fileName necessary not null
			try {
				writeCrashLogsZip(crashLogsFile, fileList);
				if (FileOps.isValidZipFile(crashLogsFile))
					return crashLogsFile;
				else
					log.w("CrashLogs: unvalid zip file : "
						+ crashLogsFile.getName());
			} catch (FileNotFoundException e) {
				log.w("CrashLogs: file read error ", e);
			} catch (IOException e) { //catches all IOExceptions concerning stream and zip entries operations
				log.w("CrashLogs: IOException when writing "
					+ "in zipfile: "+crashLogsFile.getName(), e);
			}
			if (!crashLogsFile.delete())
				log.w("CrashLogs: can't delete file: " + crashLogsFile.getName());
		}
		else
		{
			log.w("CrashLogs: " + fileName + " creation cancelled : "
				+ crashDir.getAbsolutePath() + " is empty");
			throw new UnsupportedOperationException();
		}
		return null;
	}

	/**
	 * Write files from fileList in crashLogsFile under zip format
	 * @param crashLogsFile is the zip file storing files from fileList. It shall NOT be null.
	 * @param fileList is the list of files to store in the zip file crashLogsFile. It shall NOT be null.
	 * @throws FileNotFoundException if crashLogsFile can't be read
	 * @throws IOException if an error occurs when storing an entry in zip file
	 */
	private static void writeCrashLogsZip(File crashLogsFile, File fileList[]) throws FileNotFoundException, IOException {
		/* We do something only if input parameters are not null */
		if(crashLogsFile == null || fileList == null) {
			String errorMessage = "Cannot write <null> file or cannot read from <null> file list.";
			log.e(errorMessage);
			throw new IOException(errorMessage);
		}
		ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(crashLogsFile)));
		File fileInfo = null;
		try {
			for(int i=0; i < fileList.length; i++) {
				log.d("Compress Adding: " + fileList[i].getName());

				if (!(fileList[i].exists() && fileList[i].canRead() && (fileList[i].getName().length() < Integer.MAX_VALUE))){
					log.w("CrashLogs: can't read file " + fileList[i].getName()
						+ " to be added in " + crashLogsFile.getName());
					FileWriter info = null;
					try{
						boolean append = true;
						if(fileInfo == null) {
							fileInfo = new File(fileList[i].getParent() + "/unavailableFiles");
							if(fileInfo.exists())
								append = false;
						}
						info = new FileWriter(fileInfo, append);
						info.write(fileList[i].getName()+"\n");
					}
					catch(IOException e){
						log.e("CrashLogs: Can't write "
							+ fileList[i].getName() + " in "
							+ fileList[i].getParent()
							+ "/unavailableFiles");
					} finally {
						if(info != null) {
							info.close();
						}
					}
					continue;
				}
				if(fileList[i] != null && !fileList[i].getName().contains("unavailableFiles"))
					addFileToZip(fileList[i], out);
			}
			if(fileInfo != null) {
				if (!(fileInfo.exists() && fileInfo.canRead() && (fileInfo.getName().length() < Integer.MAX_VALUE)))
					log.w("CrashLogs: can't read file " + fileInfo.getName()
						+ " to be added in " + crashLogsFile.getName());
				else
					addFileToZip(fileInfo, out);
			}
		}
		finally {
			out.close();
		}
	}

	/**
	 * Get the size of a given directory
	 * @param repository The directory
	 * @return size of a given directory
	 */
	public static int getCrashLogsSize(Context context, String repository, String eventId) {
		int totalSize = 0;
		try{
			File logsToUpload = getCrashLogsFile(context, repository, eventId);
			if(logsToUpload != null) {
				totalSize = (int)logsToUpload.length();
				logsToUpload.delete();
			}
		}
		catch(SQLException e){
			log.w("CrashLogs:getCrashLogsSize: can't access Database");
		}
		return totalSize;
	}

	/**
	 * Add a file in a zip file
	 * @param file the file to add
	 * @param out the zip file where add the file
	 * @throws IOException
	 */
	private static void addFileToZip(File file, ZipOutputStream out) throws FileNotFoundException,IOException{
		BufferedInputStream origin = null;
		byte data[] = new byte[BUFFER_SIZE];
		try{
			FileInputStream fi = new FileInputStream(file);
			origin = new BufferedInputStream(fi, BUFFER_SIZE);
			ZipEntry entry = new ZipEntry(file.getName());
			out.putNextEntry(entry);
			int count;
			while ((count = origin.read(data)) != -1)
				out.write(data, 0, count);
		}
		finally{
			if (origin != null)
				origin.close();
		}
	}
}
