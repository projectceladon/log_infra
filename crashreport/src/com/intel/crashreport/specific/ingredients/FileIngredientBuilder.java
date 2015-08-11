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
import java.util.Iterator;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.SystemProperties;

import com.intel.crashreport.Log;

public class FileIngredientBuilder implements IngredientBuilder {
	private static final String UNDEFINED_CONFIG = "UNDEFINED";
	private static final String DEFAULT_MODEM_SECTION = "MODEM";

	private final String filePath;
	private JSONObject ingredients = null;

	public FileIngredientBuilder(String filePath) {
		this.filePath = filePath;
	}

	@Override
	public JSONObject getIngredients() {
		if(this.ingredients == null) {
			CrashToolNameFilter bulkFilter = new BulkCrashToolNameFilter();
			CrashToolNameFilter regularFilter = new RegularCrashToolNameFilter();
			this.ingredients = new JSONObject();
			IniFileParser ifp = new IniFileParser(this.filePath);

			loadSection(ifp.findSection("GETBULKPROPS"), bulkFilter);
			loadSection(ifp.findSection("GETPROP"), regularFilter);
			loadSection(ifp.findSection("LIBDMI"), regularFilter);

			IniFileParser.Section modemSection = ifp.findSection(getModemSectionName());
			if (modemSection != null) {
				loadSection(modemSection, regularFilter);
			}
			else {
				loadSection(ifp.findSection(DEFAULT_MODEM_SECTION), regularFilter);
			}
		}
		return this.ingredients;
	}

	private String getModemSectionName() {
		String modemConfig = SystemProperties.get("persist.radio.multisim.config", UNDEFINED_CONFIG);

		if (modemConfig.equals(UNDEFINED_CONFIG))
			return DEFAULT_MODEM_SECTION;

		return DEFAULT_MODEM_SECTION + "." + modemConfig.toUpperCase();
	}

	private void loadSection(IniFileParser.Section section, CrashToolNameFilter filter) {
		if (section == null)
			return;

		int index = section.getEntriesCount();
		while(index-- > 0) {
			filter.addEntry(section.getEntry(index));
		}
	}

	private abstract class CrashToolNameFilter {
		public abstract void addEntry(IniFileParser.KVPair entry);

		String propertyRewrite(String input) {
			int len = input.length();
			StringBuilder sb = new StringBuilder(len);
			boolean bFiltered = false;

			for(int i = 0; i < len; i++)
			{
				char c = input.charAt(i);
				if ( !Character.isLetterOrDigit(c)){
					bFiltered = true;
					//ignore this character
					continue;
				}

				if (bFiltered && sb.length() != 0) {
					// first char in lower case
					sb.append(Character.toUpperCase(c));
				} else {
					sb.append(c);
				}

				bFiltered = false;
			}
			return sb.toString();
		}
	}

	private class BulkCrashToolNameFilter extends CrashToolNameFilter {
		public void addEntry(IniFileParser.KVPair entry) {
			JSONObject value = null;
			try {
				value = getValue(entry);
			} catch (JSONException e) { }
			try {
				if (value != null)
					ingredients.put(getKey(entry), value);
				else if (entry != null)
					ingredients.put(getKey(entry),  entry.getValue());
			} catch (JSONException e) {
				Log.e("Could not add key to ingredients");
			}
		}

		private String getKey(IniFileParser.KVPair mEntry) {
			if (mEntry == null)
				return null;

			return propertyRewrite(mEntry.getKey()) + "Bulk";
		}

		private JSONObject getValue(IniFileParser.KVPair mEntry) throws JSONException {
			if (mEntry == null)
				return null;

			JSONObject formatedJSON = new JSONObject();
			String value = mEntry.getValue();

			JSONObject jsonObj= new JSONObject(value);

			Iterator<String> iter = jsonObj.keys();
			while (iter.hasNext()) {
				String key = iter.next();
				formatedJSON.put(propertyRewrite(key), jsonObj.get(key));
			}

			return formatedJSON;
		}
	}

	private class RegularCrashToolNameFilter extends CrashToolNameFilter {
		public void addEntry(IniFileParser.KVPair entry) {
			try {
				ingredients.put(getKey(entry), getValue(entry));
			} catch (JSONException e) {
				Log.e("Could not add key to ingredients");
			}
		}

		private String getKey(IniFileParser.KVPair mEntry) {
			if (mEntry == null)
				return null;

			String sResult= "";
			String sTmp = mEntry.getKey();

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
			return propertyRewrite(sTmp);
		}

		private String getValue(IniFileParser.KVPair mEntry) {
			return mEntry.getValue();
		}
	}
}

