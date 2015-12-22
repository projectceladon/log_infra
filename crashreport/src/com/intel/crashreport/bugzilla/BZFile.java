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
