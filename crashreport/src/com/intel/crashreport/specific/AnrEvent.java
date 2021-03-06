/* Copyright (C) 2019 Intel Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.intel.crashreport.specific;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

import com.intel.crashreport.Log;

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
		FileInputStream f = new FileInputStream(anrFile);
		try {
			if(anrFile.getName().endsWith(".gz")) {
				GZIPInputStream gzipInputStream = new GZIPInputStream(f);
				scanner = new BufferedReader(new InputStreamReader (gzipInputStream));
			}
			else
				scanner = new BufferedReader(new InputStreamReader (f));

			String field;
			while((field = scanner.readLine()) != null) {
				findTraceFileId(field);
				if(fileIdPresent)
					break;
			}
			scanner.close();
		}
		finally{
			f.close();
		}
	}

	private void findTraceFileId(String field) {
		final int MAX_FIELDS = 2;
		String name;
		String value;

		if (!field.isEmpty()) {
			String splitField[] = field.split("\\:", MAX_FIELDS);
			if (splitField.length == MAX_FIELDS) {
				name = splitField[0];
				value = splitField[1];

				if (name.equals("Trace file")) {
					traceFileId = value;
					fileIdPresent = true;
				}
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
