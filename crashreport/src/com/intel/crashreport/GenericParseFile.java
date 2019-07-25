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

package com.intel.crashreport;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class GenericParseFile {

	private Map<String,String> parsedValue = new HashMap<String,String>();

	private File genFile;


	public GenericParseFile(String sPath, String sExt) throws FileNotFoundException {
		genFile = openGenFile(sPath,sExt);
		fillGenFile(genFile);
	}

	private File openGenFile(String sPath, String sFileExt) throws FileNotFoundException {
		//TO DO open a file depending on a schema
		File dir = new File(sPath);
		if(dir.exists() && dir.isDirectory()) {
			File[] dirFiles = dir.listFiles();
			if(dirFiles != null) {
				for(File file:dirFiles){
					if(file.getName().endsWith(sFileExt))
						return file;
				}
			}
		}
		throw new FileNotFoundException();
		//return new File(path + "/TO DO");
	}

	private void fillGenFile(File genFile) throws FileNotFoundException {
		Scanner scan = null;
		try {
			scan = new Scanner(genFile);
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

		if (!field.isEmpty())  {
			String splitField[] = field.split("\\=", MAX_FIELDS);
			if (splitField.length == MAX_FIELDS) {
				name = splitField[0];
				value = splitField[1];
				parsedValue.put(name, value);
			}
		}
	}

	public String getValueByName(String aName){
		if (parsedValue.containsKey(aName)){
			return parsedValue.get(aName);
		}else{
			return "";
		}
	}
}
