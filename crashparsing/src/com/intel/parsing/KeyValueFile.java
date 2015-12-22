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

package com.intel.parsing;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class KeyValueFile {

	private Map<String,String> parsedValue = new HashMap<String,String>();

	private File mFile;
	private String sSeparator = "\\=";


	public KeyValueFile(String sPath) throws FileNotFoundException {
		mFile = new File(sPath);
		fillKeyValueFile(mFile);
	}

	private void fillKeyValueFile(File aFile) throws FileNotFoundException {
		Scanner scan = null;
		try {
			scan = new Scanner(aFile);
			String field;
			while(scan.hasNext()) {
				field = scan.nextLine();
				if (field != null){
					fillField(field);
				}
			}
		} catch (IllegalStateException e) {
			throw new FileNotFoundException("Illegal state");
		} finally {
			if (scan != null) {
				scan.close();
			}
		}
	}

	private boolean fillField(String field) {
		final int MAX_FIELDS = 2;
		String name;
		String value;

		if (field.isEmpty())  {
			return false;
		}

		String splitField[] = field.split(sSeparator, MAX_FIELDS);
		if (splitField == null){
			return false;
		}

		if (splitField.length == MAX_FIELDS) {
			name = splitField[0];
			value = splitField[1];
			parsedValue.put(name, value);
			return true;
		}
		return false;
	}

	public String getValueByName(String aName) {
		if (parsedValue.containsKey(aName)) {
			return parsedValue.get(aName);
		} else {
			return "";
		}
	}
}
