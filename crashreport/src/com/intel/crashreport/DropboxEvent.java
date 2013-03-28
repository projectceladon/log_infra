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
* Author: Jean THIRY <jeanx.thiry@intel.com>
*/

package com.intel.crashreport;

import java.io.File;
import java.io.FileNotFoundException;

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