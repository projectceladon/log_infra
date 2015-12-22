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

				// first char in lower case
				if (sb.length() == 0) {
					sb.append(Character.toLowerCase(c));
				} else if (bFiltered) {
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
			try {
				JSONObject value = getValue(entry);
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

