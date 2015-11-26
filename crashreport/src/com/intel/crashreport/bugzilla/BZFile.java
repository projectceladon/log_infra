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

package com.intel.crashreport.bugzilla;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

import com.intel.crashreport.Log;

public class BZFile {

		private String summary = "";
		private String description = "";
		private String type = "";
		private String component = "";
		private String severity = "";
		private ArrayList<String> screenshotsPath;
		private boolean hasScreenshotPath = false;

		private File bzFile;

		public BZFile(String path) throws FileNotFoundException {
			bzFile = openBzFile(path);
			screenshotsPath = new ArrayList<String>();
			fillBzFile(bzFile);

		}

		private File openBzFile(String path) {
			return new File(path + "/bz_description");
		}

		private void fillBzFile(File bzFile) throws FileNotFoundException {
			Scanner scan = null;
			try {
				scan = new Scanner(bzFile);
				String field;
				while(scan.hasNext()) {
					field = scan.nextLine();
					if (field != null){
						fillField(field);
					}
				}
			} catch (IllegalStateException e) {
				Log.w("IllegalStateException : considered as file not found exception");
				throw new FileNotFoundException("Illegal state");
			} finally {
				if (scan != null) {
					scan.close();
				}
			}
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
						else if (name.equals("SCREENSHOT")) {
							screenshotsPath.add(value);
							hasScreenshotPath = true;
						}
						else if (name.equals("APLOG")) {
							//nothing to do
						}
						else if (name.equals("BPLOG")) {
							//nothing to do
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

		public ArrayList<String> getScreenshotsPath() {
			return screenshotsPath;
		}

		public String getScreenshotsPathToString() {
			String path = "";
			for(String screen:screenshotsPath) {
				if(path.equals(""))
					path = screen;
				else
					path += "," + screen;

			}
			return path;
		}

		public void setScreenshotsPath(String screenshotPath) {
			try{
				String screens[] = screenshotPath.split(",");
				if(screens.length > 0) {
					for(String screenshot:screens){
						screenshotsPath.add(screenshot);
					}
				}

			}
			catch(NullPointerException e){
				Log.w("BZ:setScreenshots: not screenshot founded");
			}
		}

		public void setScreenshotsPath(ArrayList<String> listScreenshots) {
			screenshotsPath = listScreenshots;
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
