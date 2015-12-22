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
