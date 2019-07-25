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

package com.intel.crashreport.bugzilla;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

import com.intel.crashreport.core.BZ;
import com.intel.crashreport.Log;

public class BZFile extends BZ {

		private File bzFile;

		public BZFile(String path) throws FileNotFoundException {
			bzFile = openBzFile(path);
			screenshots = new ArrayList<String>();
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

			if (!field.isEmpty()) {
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
						screenshots.add(value);
						hasScreenshot = true;
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
			}
		}

		public File getBzFile() {
			return bzFile;
		}

		public void setBzFile(File bzFile) {
			this.bzFile = bzFile;
		}
}
