/* Phone Doctor - parsing
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
 * Author: Nicolas BENOIT <nicolasx.benoit@intel.com>
 */

package com.intel.parsing;

import java.io.FileNotFoundException;

public class LegacyParser implements EventParser {

	public boolean parseEvent(ParsableEvent aEvent) {
		KeyValueFile aCrashfile = null;
		String sDate = "";
		String sBoard = "";
		String sOperator = "";

		try {
			aCrashfile = new KeyValueFile(aEvent.getCrashDir() + "/crashfile");
			sDate = aCrashfile.getValueByName("DATE");
			sBoard = aCrashfile.getValueByName("BOARD");
			sOperator = aCrashfile.getValueByName("OPERATOR");
		} catch (FileNotFoundException e) {
			return false;
		}
		MainParser aParser = new MainParser(aEvent, sBoard, sDate, sOperator);
		if (aParser.execParsing() == 0) {
			try {
				aCrashfile = new KeyValueFile(aEvent.getCrashDir() + "/crashfile");
				aEvent.setData0(aCrashfile.getValueByName("DATA0"));
				aEvent.setData1(aCrashfile.getValueByName("DATA1"));
				aEvent.setData2(aCrashfile.getValueByName("DATA2"));
				aEvent.setData3(aCrashfile.getValueByName("DATA3"));
				aEvent.setData4(aCrashfile.getValueByName("DATA4"));
				aEvent.setData5(aCrashfile.getValueByName("DATA5"));
			} catch (FileNotFoundException e) {
				return false;
			}
			return true;
		} else {
			return false;
		}
	}

	public boolean isEventEligible(ParsableEvent aEvent) {
		if (aEvent.getEventName().equals("CRASH")) {
			return true;
		}
		return false;
	}
}
