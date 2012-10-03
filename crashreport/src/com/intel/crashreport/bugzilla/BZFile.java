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

package com.intel.crashreport.bugzilla;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import com.intel.crashreport.Log;

public class BZFile {

		private String summary = "";
		private String description = "";
		private String type = "";
		private String component = "";
		private String severity = "";
		private String screenshotPath = "";
		private boolean hasScreenshotPath = false;

		private File bzFile;

		public BZFile(String path) throws FileNotFoundException {
			bzFile = openBzFile(path);
			fillBzFile(bzFile);

		}

		private File openBzFile(String path) {
			return new File(path + "/bz_description");
		}

		private void fillBzFile(File bzFile) throws FileNotFoundException {
			Scanner scan = new Scanner(bzFile);
			String field;
			while(scan.hasNext()) {
				field = scan.nextLine();
				if (field != null){
					fillField(field);
				}
			}
			scan.close();
		}

		private void fillField(String field) {
			final int MAX_FIELDS = 2;
			String name;
			String value;

			if (field.length() != 0) {
				try {
					String splitField[] = field.split("\\=", MAX_FIELDS);
					if (splitField.length == MAX_FIELDS) {
						name = splitField[0];
						value = splitField[1];

						if (name.equals("SUMMARY"))
							summary = value;
						else if (name.equals("DESCRIPTION"))
							description = value;
						else if (name.equals("TYPE"))
							type = value;
						else if (name.equals("COMPONENT"))
							component = value;
						else if (name.equals("SEVERITY"))
							severity = value;
						else if (name.equals("SCREENSHOTPATH")) {
							screenshotPath = value;
							hasScreenshotPath = true;
						}
						else
							Log.w("BzFile: field name\"" + name + "\" not recognised");
					}
				} catch (NullPointerException e) {
					Log.w("BzFile: field format not recognised : " + field);
				}
			}
		}

		public String getSummary() {
			return summary;
		}

		public void setSummary(String summary) {
			this.summary = summary;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public String getComponent() {
			return component;
		}

		public void setComponent(String component) {
			this.component = component;
		}

		public String getSeverity() {
			return severity;
		}

		public void setSeverity(String severity) {
			this.severity = severity;
		}

		public String getScreenshotPath() {
			return screenshotPath;
		}

		public void setScreenshotPath(String screenshotPath) {
			this.screenshotPath = screenshotPath;
		}

		public boolean hasScreenshotPath() {
			return hasScreenshotPath;
		}

		public void setHasScreenshotPath(boolean hasScreenshotPath) {
			this.hasScreenshotPath = hasScreenshotPath;
		}

		public File getBzFile() {
			return bzFile;
		}

		public void setBzFile(File bzFile) {
			this.bzFile = bzFile;
		}



}
