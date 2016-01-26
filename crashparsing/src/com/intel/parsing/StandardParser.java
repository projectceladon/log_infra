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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import com.intel.crashreport.core.ParsableEvent;

public class StandardParser implements EventParser {

	private boolean mCritical;
	private String mEventName;
	private String mEventType;
	ArrayList<StandardRule> mRulesList = null;

	public StandardParser(){
		mRulesList = new ArrayList<StandardRule>();
	}


	public boolean parseEvent(ParsableEvent aEvent) {
		if (mRulesList.isEmpty()) {
			return false;
		}
		APLog.i("Parsing event : " + aEvent.getEventId() + " as " + mEventType) ;
		for (StandardRule curRule : mRulesList) {
			curRule.analyzeEvent(aEvent);
		}
		setCritical(aEvent);
		return true;
	}

	public void setCritical(ParsableEvent aEvent) {
		StringBuilder sb = new StringBuilder(255);
		String terminator = String.format("%n");
	        File file = new File(aEvent.getCrashDir(), "crashfile");
		FileWriter fw = null;
		BufferedReader br = null;

		try {
			br = new BufferedReader(new FileReader(file));
			String line = br.readLine();
			while (line != null) {
				if (!line.startsWith("CRITICAL="))
					sb.append(line + terminator);

				line = br.readLine();
			}
		} catch (IOException e) {
			APLog.e("error while reading file : " + e);
		} finally {
			if (br != null)
				try {
					br.close();
				} catch (IOException e) {
					APLog.e("error while closing file : " + e);
				}
		}

		sb.insert(0, "CRITICAL=" + ((mCritical) ? "YES" : "NO") + terminator);

		try {
			fw = new FileWriter(file);
			fw.write(sb.toString());
		} catch (IOException e) {
			APLog.e("error writing to file : " + e);
		} finally {
			if (fw != null)
				try {
					fw.close();
				} catch (IOException e) {
					APLog.e("error while closing file : " + e);
				}
		}

		aEvent.setCritical(mCritical);
	}

	public boolean isEventEligible(ParsableEvent aEvent) {
		if (aEvent.getEventName().equals(mEventName) && aEvent.getType().equals(mEventType)) {
			return true;
		}
		return false;
	}

	public void addRulle(StandardRule aRule){
		mRulesList.add(aRule);
	}

	public boolean isCritical() {
		return mCritical;
	}

	public void setCritical(boolean mCriticalClass) {
		this.mCritical = mCriticalClass;
	}

	public String getEventName() {
		return mEventName;
	}

	public void setEventName(String mEventClass) {
		this.mEventName = mEventClass;
	}

	public String getEventType() {
		return mEventType;
	}

	public void setEventType(String mEventType) {
		this.mEventType = mEventType;
	}

}
