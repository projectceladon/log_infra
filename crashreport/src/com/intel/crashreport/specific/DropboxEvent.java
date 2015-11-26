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

import com.intel.crashreport.Log;

/**
 * This class manages the origin logfile associated to a dropbox event.
 */
public class DropboxEvent {
	private static final String Module = "DropboxEvent: ";

	/* Patterns contained in (origin) dropbox logfile for each kind of dropbox events*/
	public static final String ANR_DROPBOX_TAG= "anr";
	public static final String JAVACRASH_DROPBOX_TAG= "crash";
	public static final String UIWDT_DROPBOX_TAG= "system_server_watchdog";

	private File dropboxFile;
	private String dropboxTag = "";

	/**
	 * Constructor
	 *
	 * @param path is the event crashlog directory path
	 * @param eventType is event type
	 * @throws FileNotFoundException
	 */
	public DropboxEvent(String path, String eventType) throws FileNotFoundException{

		if (eventType.equals("ANR"))
			dropboxTag = ANR_DROPBOX_TAG;
		else if (eventType.equals("JAVACRASH"))
			dropboxTag = JAVACRASH_DROPBOX_TAG;
		else if (eventType.equals("UIWDT"))
			dropboxTag = UIWDT_DROPBOX_TAG;
		else {
			Log.w(Module + eventType + " is not a dropbox event");
			return;
		}
		dropboxFile = openDropboxFile(path);
	}

	/**
	 * Returns the dropbox origin logfile associated to the dropbox event
	 * @param path id the event crashlog directory
	 * @return a reference on the found file. Throws exception otherwise.
	 * @throws FileNotFoundException
	 */
	private File openDropboxFile(String path) throws FileNotFoundException{
		File dir = new File(path);
		if(dir.exists() && dir.isDirectory()) {
			File[] dirFiles = dir.listFiles();
			if(dirFiles != null) {
				for(File file:dirFiles){
					//It is assumed all origin dropbox file shall contain at least the tag and the '@' char
					if( file.getName().contains(dropboxTag) && file.getName().indexOf("@")!=-1 )
						return file;
				}
			}
		}
		throw new FileNotFoundException();
	}

	/**
	 * Accessor returning the dropbox logfile name
	 *
	 * @return the logfile name
	 */
	public String getDropboxFileName() {
		if (dropboxFile != null)
			return dropboxFile.getName();
		else
			return "";
	}
}
