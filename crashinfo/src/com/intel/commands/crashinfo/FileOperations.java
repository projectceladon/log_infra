/* crashinfo - FileOperations contains methods to process crashdata
 *
 * Copyright (C) Intel 2015
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
 * Author: Nicolae Natea <nicolaex.natea@intel.com>
 */

package com.intel.commands.crashinfo;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

public class FileOperations {
	private static final int BUFFER_SIZE = 1024;

	/**
	 * Check if the input file is a valid zip file.
	 *
	 * @param file to check
	 * @return true is the file is a valid Zip file. False otherwise.
	 * @throws IOException when zip integrity check fails.
	 */
	public static boolean isValidZipFile(final File file) throws IOException {
		ZipFile zipFile = null;
		try {
			zipFile = new ZipFile(file);
			zipFile.close();
			return true;
		} catch (ZipException e) {
			return false;
		}
	}

	private static void writeCrashLogsZip(File crashLogsFile, File fileList[]) throws FileNotFoundException, IOException {
		/* We do something only if input parameters are not null */
		if(crashLogsFile == null || fileList == null) {
			throw new IOException("Cannot write <null> file or cannot read from <null> file list.");
		}
		ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(crashLogsFile)));
		File fileInfo = null;
		try {
			for(int i=0; i < fileList.length; i++) {

				if (!(fileList[i].exists() && fileList[i].canRead() && (fileList[i].getName().length() < Integer.MAX_VALUE))){
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
					catch(IOException e){ }
					finally {
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
				if (fileInfo.exists() && fileInfo.canRead() && (fileInfo.getName().length() < Integer.MAX_VALUE))
					addFileToZip(fileInfo, out);
			}
		}
		finally {
			out.close();
		}
	}

	/**
	 * Add a file in a zip file
	 * @param file the file to add
	 * @param out the zip file where add the file
	 * @throws FileNotFoundException, IOException
	 */
	private static void addFileToZip(File file, ZipOutputStream out) throws FileNotFoundException, IOException {
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

	private static File createCrashLogsZip(String crashDirPath, String fileName, File outDir) throws IOException {
		File crashDir = new File(crashDirPath);
		if ( !(crashDir.exists() && crashDir.isDirectory()) ) {
			throw new IOException("Specified path (" + crashDir + ") does not exist or is not a directory");
		}

		/*Check crashlog directory is not empty*/
		File fileList[] = crashDir.listFiles();
		if ((fileList != null) && fileList.length > 0) {
			File crashLogsFile = new File(outDir, fileName); //fileName necessary not null
			try {
				writeCrashLogsZip(crashLogsFile, fileList);
				if (isValidZipFile(crashLogsFile))
					return crashLogsFile;
			}
			catch (FileNotFoundException e) { }
			catch (IOException e) { }
			crashLogsFile.delete();
		}
		return null;
	}

	public static File getCrashLogsFile(File cacheDir, String crashDir, String eventId) throws IOException {
		if ((crashDir == null) || (crashDir.isEmpty()) || (cacheDir == null) || !cacheDir.exists()) {
			return null;
		}

		String crashLogsFileName = "EVENT"+eventId+".zip";
		File crashLogsFile = new File(cacheDir, crashLogsFileName);
		crashLogsFile.deleteOnExit();
		if (crashLogsFile.exists()) {
			crashLogsFile.delete();
		}
		return createCrashLogsZip(crashDir, crashLogsFileName, cacheDir);
	}
}
