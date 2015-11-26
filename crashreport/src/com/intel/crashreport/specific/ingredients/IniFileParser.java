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
			String retVal = SECTION_NAME_OPEN_BRACKET + mSectionName + SECTION_NAME_CLOSE_BRACKET + "\n";

			for (KVPair entry : mEntries) {
				retVal += entry;
			}
			return retVal;
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
				String line = null;
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

		if (line.length() == 0) {
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
		String retVal = "";
		for (Section entry : mEntries) {
			retVal += entry;
		}

		return retVal;
	}
}
