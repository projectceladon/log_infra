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
