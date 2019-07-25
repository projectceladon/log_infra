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
