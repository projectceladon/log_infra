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

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import com.intel.crashreport.Log;
import com.intel.phonedoctor.Constants;

public class HistoryEventFile {

	private static final String HISTORY_EVENT_FILE_PATH = Constants.LOGS_DIR + "/history_event";

	private Scanner scanner;

	public HistoryEventFile() {
	}

	public Boolean hasNext() throws  FileNotFoundException {
		try{
			return scanner.hasNext();
		} catch (IllegalStateException e) {
			Log.w("IllegalStateException : considered as file not found exception");
			throw new FileNotFoundException("Illegal state");
		}
	}

	public String getNextEvent() throws  FileNotFoundException {
		String line;
		try {
			while(scanner.hasNext()) {
				line = scanner.nextLine();
				if (line != null){
					if ((line.length() > 0) && (line.charAt(0) != '#')) {
						return line;
					}
				}
			}
		} catch (IllegalStateException e) {
			Log.w("IllegalStateException : considered as file not found exception");
			throw new FileNotFoundException("Illegal state");
		}
		return "";
	}

	public void open() throws FileNotFoundException {
		String path = HISTORY_EVENT_FILE_PATH;
		File histFile = new File(path);
		if (!histFile.canRead())
			Log.w("HistoryEventFile: can't read file : " + path);
		scanner = new Scanner(histFile);
	}

	public void close() {
		scanner.close();
	}

	protected void finalize() throws Throwable {
		try {
			scanner.close();        // close open files
		} finally {
			super.finalize();
		}
	}

}
