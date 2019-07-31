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

package com.intel.crashreport.specific.ingredients;

import java.util.Vector;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import com.intel.crashreport.Log;

public class IniFileParser {
	public static final String DEFAULT_SEPARATOR = "=";
	public static final char SECTION_NAME_OPEN_BRACKET = '[';
	public static final char SECTION_NAME_CLOSE_BRACKET = ']';
	public static final String UNDEFINED_SECTION = "UNDEFINED";
	public static final char COMMENT_MARKER = '#';

	Vector<Section> mEntries;
	Section currentSection = null;

	public class Section {

		String mSectionName;
		Vector<KVPair> mEntries;

		public Section() {
			mSectionName = UNDEFINED_SECTION;
			mEntries = new Vector<KVPair>();
		}

		public Section(String name) {
			mSectionName = name;
			mEntries = new Vector<KVPair>();
		}

		public boolean loadSection(String entry) {
			entry = entry.trim();

			if (entry.charAt(0) == SECTION_NAME_OPEN_BRACKET &&
				entry.charAt(entry.length()-1) == SECTION_NAME_CLOSE_BRACKET) {

				if (entry.length() > 2) {
					mSectionName = entry.substring(1,entry.length()-1);
				}
				else {
					mSectionName = UNDEFINED_SECTION;
				}

				return true;
			}
			return false;
		}

		public KVPair findKey(String key) {
			for (KVPair entry : mEntries) {
				if (entry.getKey().equals(key))
					return entry;
			}
			return null;
		}

		public void setSectionName(String name) {
			mSectionName = name;
		}

		public String getSectionName() {
			return mSectionName;
		}

		public int getEntriesCount() {
			return mEntries.size();
		}

		public KVPair getEntry(int index) {
			return mEntries.get(index);
		}

		public void addEntry(KVPair entry) {
			mEntries.add(entry);
		}

		public void addEntry(String entry) {
			KVPair kvpair = new KVPair();
			kvpair.loadEntry(entry);
			mEntries.add(kvpair);
		}

		@Override
		public String toString() {
			StringBuffer retVal = new StringBuffer();
			retVal.append(SECTION_NAME_OPEN_BRACKET + mSectionName + SECTION_NAME_CLOSE_BRACKET + "\n");

			for (KVPair entry : mEntries) {
				retVal.append(entry);
			}
			return retVal.toString();
		}
	}

	public class KVPair {
		String mKey;
		String mValue;

		public KVPair() {
			mValue = "";
			mKey = "";
		}

		public KVPair(String key, String value) {
			mValue = value;
			mKey = key;
		}

		public boolean loadEntry(String entry, String separator) {
			entry = entry.trim();
			String[] tokens = entry.split(separator,2);

			if(tokens.length > 1) {
				mKey = tokens[0];
				mValue = tokens[1];

				return true;
			}
			return false;
		}

		public boolean loadEntry(String entry) {
			return loadEntry(entry, DEFAULT_SEPARATOR);
		}

		public void setKey(String key) {
			mKey = key;
		}

		public void setValue(String value) {
			mValue = value;
		}

		public String getKey() {
			return mKey;
		}

		public String getValue() {
			return mValue;
		}

		@Override
		public String toString() {
			return mKey+ DEFAULT_SEPARATOR + mValue + "\n";
		}
	}

	IniFileParser(String filePath) {
		mEntries = new Vector<Section>();

		if(filePath == null) {
			return;
		}

		File file = new File(filePath);
		FileReader fileReader = null;
		BufferedReader reader = null;
		try {
			if(!file.exists() || !file.canRead()) {
				Log.e("No ini file found.");
				return;
			}
			fileReader = new FileReader(file);
			reader = new BufferedReader(fileReader);
			try {
				String line;
				do {
					line = reader.readLine();
					this.clasify(line);
				} while(line != null);
			} catch(IOException io) {
				Log.e("IO error occurred while reading file: " + filePath);
			}
		} catch(FileNotFoundException notFound) {
			Log.e("File could not be found: " + filePath);
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
				else if (fileReader != null){
					fileReader.close();
				}
			} catch(IOException io) {
				Log.e("IO error occurred while closing file: " + filePath);
			}
		}
	}

	Section findSection(String name) {
		for (Section entry : mEntries) {
			if (entry.getSectionName().equals(name))
				return entry;
		}
		return null;
	}

	private void clasify(String line) {
		if (line == null)
			return;

		line = line.trim();

		if (line.isEmpty()) {
			//empty line
			return;
		}

		switch (line.charAt(0)) {

			case (COMMENT_MARKER):
				break;

			case (SECTION_NAME_OPEN_BRACKET):
				Section section = new Section();
				if (section.loadSection(line)) {
					if (findSection(section.getSectionName()) == null)
						mEntries.add(section);
					currentSection = findSection(section.getSectionName());
				}
				break;

			default:
				KVPair kvpair = new KVPair();
				if (kvpair.loadEntry(line)) {
					if (currentSection != null) {
						KVPair kvpairSection = currentSection.findKey(kvpair.getKey());
						if (kvpairSection == null)
							currentSection.addEntry(kvpair);
						else //redundancy check.
							kvpairSection.setValue(kvpair.getValue());
					}
				}
				break;
		}
	}

	@Override
	public String toString() {
		StringBuffer retVal = new StringBuffer();
		for (Section entry : mEntries) {
			retVal.append(entry);
		}

		return retVal.toString();
	}
}
