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
* Author: Charles-Edouard Vidoine <charles.edouardx.vidoine@intel.com>
*/

package com.intel.crashreport;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

public class AnrEvent {
	private String traceFileId = "";
	private File anrFile;
	private boolean fileIdPresent = false;

	public AnrEvent(String path) throws FileNotFoundException,IOException {

		anrFile = openAnrFile(path);
		readAnrFile(anrFile);
	}

	private File openAnrFile(String path) throws FileNotFoundException{
		File dir = new File(path);
		if(dir.exists() && dir.isDirectory()) {
			File[] dirFiles = dir.listFiles();
			if(dirFiles != null) {
				for(File file:dirFiles){
					if(file.getName().startsWith("system_app_anr"))
						return file;
				}
			}
		}
		throw new FileNotFoundException();
	}

	private void readAnrFile(File anrFile) throws FileNotFoundException,IOException {
		BufferedReader scanner;
		if(anrFile.getName().endsWith(".gz")) {
			GZIPInputStream gzipInputStream = new GZIPInputStream(new FileInputStream(anrFile));
			scanner = new BufferedReader(new InputStreamReader (gzipInputStream));
		}
		else
			scanner = new BufferedReader(new InputStreamReader (new FileInputStream(anrFile)));

		String field;
		while((field = scanner.readLine()) != null) {
			if (field != null){
				findTraceFileId(field);
				if(fileIdPresent)
					break;
			}
		}
		scanner.close();
	}

	private void findTraceFileId(String field) {
		final int MAX_FIELDS = 2;
		String name;
		String value;

		if ((field.length() != 0)) {
			try {
				String splitField[] = field.split("\\:", MAX_FIELDS);
				if (splitField.length == MAX_FIELDS) {
					name = splitField[0];
					value = splitField[1];

					if (name.equals("Trace file")) {
						traceFileId = value;
						fileIdPresent = true;
					}
				}
			} catch (NullPointerException e) {
				Log.w("anrFile: field format not recognised : " + field);
			}
		}
	}

	public boolean tracefileIdIsPresent() {
		return fileIdPresent;
	}

	public String getTraceFileId() {
		return traceFileId;
	}
}
