/* Phone Doctor (CLOTA)
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
 * Author: Jeremy Rocher <jeremyx.rocher@intel.com>
 */

package com.intel.crashreport;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import android.content.Context;

public class CrashLogs {

	private static final int BUFFER_SIZE = 1024;

	public static File getCrashLogsFile(Context context, String crashDir, String eventId) {
		try {
			if (crashDir != null){
				if (!crashDir.isEmpty()){
					File cacheDir = context.getExternalCacheDir();
					if ((cacheDir != null) && cacheDir.exists()) {
						String crashLogsFileName = "EVENT"+eventId+".zip";
						File crashLogsFile = new File(cacheDir, crashLogsFileName);
						if (crashLogsFile.exists())
							return crashLogsFile;
						else {
							return createCrashLogsZip(crashDir, crashLogsFileName, cacheDir);
						}
					}else{
						Log.w("getExternalCacheDir Null : using cachedir");
						cacheDir = context.getCacheDir();
						if ((cacheDir != null) && cacheDir.exists()) {
							String crashLogsFileName = "EVENT"+eventId+".zip";
							File crashLogsFile = new File(cacheDir, crashLogsFileName);
							crashLogsFile.deleteOnExit();
							if (crashLogsFile.exists())
								return crashLogsFile;
							else {
								return createCrashLogsZip(crashDir, crashLogsFileName, cacheDir);
							}
						}
					}
				}
			}
		} catch (Exception e) {
			return null;
		}
		return null;
	}

	private static File createCrashLogsZip(String crashDirPath, String fileName, File outDir) {
		try {
			File crashDir = new File(crashDirPath);
			if ((crashDir != null) && crashDir.exists() && crashDir.isDirectory()) {
				File fileList[] = crashDir.listFiles();
				if (fileList != null) {
					File crashLogsFile = new File(outDir, fileName);
					BufferedInputStream origin = null;
					ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(crashLogsFile)));
					if (out != null) {
						byte data[] = new byte[BUFFER_SIZE];

						for(int i=0; i < fileList.length; i++) {
							Log.d("Compress Adding: " + fileList[i].getName());
							FileInputStream fi = new FileInputStream(fileList[i]);
							origin = new BufferedInputStream(fi, BUFFER_SIZE);
							ZipEntry entry = new ZipEntry(fileList[i].getName());
							out.putNextEntry(entry);
							int count;
							while ((count = origin.read(data)) != -1)
								out.write(data, 0, count);
							origin.close();
						}
						out.close();
						return crashLogsFile;
					} else {
						Log.w("CrashLogs: Can't create zip file : "+crashLogsFile);
						return null;
					}
				} else {
					Log.w("CrashLogs: No files in : "+crashDirPath);
					return null;
				}
			} else {
				Log.w("CrashLogs: Error when creating zip of : "+crashDirPath);
				return null;
			}
		} catch(Exception e) {
			Log.w("CrashLogs: Error when creating zip of : "+crashDirPath);
			return null;
		}
	}

	public static void deleteCrashLogsZipFiles(Context context) {
		File cacheDir = context.getExternalCacheDir();
		if ((cacheDir != null) && cacheDir.isDirectory()) {
			File fileList[] = cacheDir.listFiles();
			if (fileList != null)
				for (int i=0; i<fileList.length; i++) {
					File cFile = fileList[i];
					if ((cFile != null) && cFile.isFile())
						cFile.delete();
				}
		}
	}

}
