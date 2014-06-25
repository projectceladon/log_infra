/* Phone Doctor (CLOTA)
 *
 * Copyright (C) Intel 2014
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
 * Author: Adrien Sebbane <adrienx.sebbane@intel.com>
 */
package com.intel.crashreport.specific.ingredients;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.intel.crashreport.Log;

public class FileIngredientBuilder implements IngredientBuilder {
	public static final String DEFAULT_SEPARATOR = "=";

	private final String filePath;
	private final String separator;
	private Map<String, String> ingredients = null;

	public FileIngredientBuilder(String filePath) {
		this.filePath = filePath;
		this.separator = DEFAULT_SEPARATOR;
	}

	public FileIngredientBuilder(String filePath, String separator) {
		this.filePath = filePath;
		if(separator == null) {
			this.separator = DEFAULT_SEPARATOR;
		} else {
			this.separator = separator;
		}
	}

	@Override
	public Map<String, String> getIngredients() {
		if(this.ingredients == null) {
			this.ingredients = new HashMap<String, String>();
			this.parseFile();
		}
		return this.ingredients;
	}

	private void parseFile() {
		if(this.filePath == null) {
			return;
		}
		File file = new File(this.filePath);
		FileReader fileReader = null;
		BufferedReader reader = null;
		try {
			if(!file.exists() || !file.canRead()) {
				Log.e("No ingredients file found.");
				return;
			}
			fileReader = new FileReader(file);
			reader = new BufferedReader(fileReader);
			try {
				String line = null;
				do {
					line = reader.readLine();
					this.processLine(line, this.ingredients);
				} while(line != null);
			} catch(IOException io) {
				Log.e("IO error occurred while reading file: " + this.filePath);
			}
		} catch(FileNotFoundException notFound) {
			Log.e("File could not be found: " + this.filePath);
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
				else if (fileReader != null){
					fileReader.close();
				}
			} catch(IOException io) {
				Log.e("IO error occurred while closing file: " + this.filePath);
			}
		}
	}

	public String getSeparator() {
		return this.separator;
	}

	private void processLine(String line, Map<String, String> container) {
		if(line == null || container == null) {
			return;
		}
		//ignore # comment line
		if (line.startsWith("#")) {
			return;
		}
		Log.d("Processing line:" + line);
		String separator = this.getSeparator();
		String[] tokens = line.split(separator,2);
		if(tokens.length > 1) {
			//first part should be filtered
			container.put(filterNameForCrashtool(tokens[0]), tokens[1]);
		}
	}

	private String filterNameForCrashtool(String aName) {
		String sResult= "";
		String sTmp = aName;

		//first we remove "version" suffix
		String sCheckString = "version";
		if (sTmp.toLowerCase().endsWith(sCheckString)) {
			sTmp = sTmp.substring(0, sTmp.length() - sCheckString.length());
		}
		// remove sys prefix
		sCheckString = "sys";
		if (sTmp.toLowerCase().startsWith(sCheckString)) {
			sTmp = sTmp.substring(sCheckString.length(), sTmp.length());
		}
		boolean bFiltered = false;
		for(int i = 0; i < sTmp.length(); i++)
		{
			char c = sTmp.charAt(i);
			if ( !Character.isLetterOrDigit(c)){
				bFiltered = true;
				//ignore this character
				continue;
			}

			if (bFiltered) {
				c = Character.toUpperCase(c);
			}
			bFiltered = false;
			if (sResult.isEmpty()){
				// first chat in lower case
				sResult += c;
				sResult = sResult.toLowerCase();
			} else {
				sResult += c;
			}
		}
		return sResult;
	}
}

