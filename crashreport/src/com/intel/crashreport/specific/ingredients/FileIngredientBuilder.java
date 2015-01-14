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

import android.os.SystemProperties;

import com.intel.crashreport.Log;

public class FileIngredientBuilder implements IngredientBuilder {
	private static final String UNDEFINED_CONFIG = "UNDEFINED";
	private static final String DEFAULT_MODEM_SECTION = "MODEM";

	private final String filePath;
	private Map<String, String> ingredients = null;

	public FileIngredientBuilder(String filePath) {
		this.filePath = filePath;
	}

	@Override
	public Map<String, String> getIngredients() {
		if(this.ingredients == null) {
			this.ingredients = new HashMap<String, String>();
			IniFileParser ifp = new IniFileParser(this.filePath);

			loadSection(ifp.findSection("GETPROP"));
			loadSection(ifp.findSection("LIBDMI"));

			IniFileParser.Section modemSection = ifp.findSection(getModemSectionName());
			if (modemSection != null) {
				loadSection(modemSection);
			}
			else {
				loadSection(ifp.findSection(DEFAULT_MODEM_SECTION));
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

	private void loadSection(IniFileParser.Section section) {
		if (section == null)
			return;

		int index = section.getEntriesCount();
		while(index-- > 0) {
			IniFileParser.KVPair entry = section.getEntry(index);
			ingredients.put(filterNameForCrashtool(entry.getKey()), entry.getValue());
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

