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

import com.intel.crashreport.core.ParsableEvent;
import java.io.FileNotFoundException;

public class LegacyParser implements EventParser {

	public boolean parseEvent(ParsableEvent aEvent) {
		KeyValueFile aCrashfile;
		String sDate;
		String sBoard;
		String sOperator;

		try {
			aCrashfile = new KeyValueFile(aEvent.getCrashDir() + "/crashfile");
			updateEventDataByCrashfile(aCrashfile,aEvent);
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
				updateEventDataByCrashfile(aCrashfile,aEvent);
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

	private void updateEventDataByCrashfile(KeyValueFile aCrashfile, ParsableEvent aEvent ) {
		String sValue;

		//update only if a value is present in crashfile
		sValue = aCrashfile.getValueByName("DATA0");
		if (!sValue.isEmpty()) {
			aEvent.setData0(sValue);
		}
		sValue = aCrashfile.getValueByName("DATA1");
		if (!sValue.isEmpty()) {
			aEvent.setData1(sValue);
		}
		sValue = aCrashfile.getValueByName("DATA2");
		if (!sValue.isEmpty()) {
			aEvent.setData2(sValue);
		}
		sValue = aCrashfile.getValueByName("DATA3");
		if (!sValue.isEmpty()) {
			aEvent.setData3(sValue);
		}
		sValue = aCrashfile.getValueByName("DATA4");
		if (!sValue.isEmpty()) {
			aEvent.setData4(sValue);
		}
		sValue = aCrashfile.getValueByName("DATA5");
		if (!sValue.isEmpty()) {
			aEvent.setData5(sValue);
		}
		sValue = aCrashfile.getValueByName("CRITICAL");
		if (!sValue.isEmpty()) {
			aEvent.setCritical(sValue.equals("YES"));
		}
		sValue = aCrashfile.getValueByName("MODEMVERSIONUSED");
		if (!sValue.isEmpty()) {
			aEvent.setModemVersionUsed(sValue);
		}
	}
}
