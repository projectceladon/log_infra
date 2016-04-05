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
