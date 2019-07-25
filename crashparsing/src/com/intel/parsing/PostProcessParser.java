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

import java.util.ArrayList;
import java.util.List;
import com.intel.crashreport.core.ParsableEvent;

public class PostProcessParser implements EventParser {

	private String mData0;
	private String mData1;
	private String mData2;
	private String mData3;
	private String mData4;
	private String mData5;
	private String mEventType;
	private String mDescription;
	List<PostProcessRule> mRulesList;

	public PostProcessParser() {
		mRulesList = new ArrayList<PostProcessRule>();
	}

	public boolean parseEvent(ParsableEvent aEvent) {

		if (mRulesList.isEmpty()) {
			return false;
		}
		APLog.i("PostProcessing event : " + aEvent.getEventId() + " with "
				+ mDescription);
		for (PostProcessRule curRule : mRulesList) {
			curRule.analyzeEvent(aEvent);
		}
		return true;
	}

	public boolean isEventEligible(ParsableEvent aEvent) {
		boolean bResult = false;
		// check only on not empty data
		if ((mData0 != null) && (!mData0.isEmpty())) {
			if (!aEvent.getData0().contains(mData0)) {
				return false;
			}
			bResult = true;
		}
		if ((mData1 != null) && (!mData1.isEmpty())) {
			if (!aEvent.getData1().contains(mData1)) {
				return false;
			}
			bResult = true;
		}
		if ((mData2 != null) && (!mData2.isEmpty())) {
			if (!aEvent.getData2().contains(mData2)) {
				return false;
			}
			bResult = true;
		}
		if ((mData3 != null) && (!mData3.isEmpty())) {
			if (!aEvent.getData3().contains(mData3)) {
				return false;
			}
			bResult = true;
		}
		if ((mData4 != null) && (!mData4.isEmpty())) {
			if (!aEvent.getData4().contains(mData4)) {
				return false;
			}
			bResult = true;
		}
		if ((mData5 != null) && (!mData5.isEmpty())) {
			if (!aEvent.getData5().contains(mData5)) {
				return false;
			}
			bResult = true;
		}

		return bResult;
	}

	public void addRule(PostProcessRule aRule) {
		mRulesList.add(aRule);
	}

	public String getEventType() {
		return mEventType;
	}

	public void setEventType(String mEventType) {
		this.mEventType = mEventType;
	}

	public String getmData0() {
		return mData0;
	}

	public void setmData0(String mData0) {
		this.mData0 = mData0;
	}

	public String getmData1() {
		return mData1;
	}

	public void setmData1(String mData1) {
		this.mData1 = mData1;
	}

	public String getmData2() {
		return mData2;
	}

	public void setmData2(String mData2) {
		this.mData2 = mData2;
	}

	public String getmData3() {
		return mData3;
	}

	public void setmData3(String mData3) {
		this.mData3 = mData3;
	}

	public String getmData4() {
		return mData4;
	}

	public void setmData4(String mData4) {
		this.mData4 = mData4;
	}

	public String getmData5() {
		return mData5;
	}

	public void setmData5(String mData5) {
		this.mData5 = mData5;
	}

	// proxy functions for JSON Builder

	public void setdata0(String mData0) {
		setmData0(mData0);
	}

	public void setdata1(String mData1) {
		setmData1(mData1);
	}

	public void setdata2(String mData2) {
		setmData2(mData2);
	}

	public void setdata3(String mData3) {
		setmData3(mData3);
	}

	public void setdata4(String mData4) {
		setmData4(mData4);
	}

	public void setdata5(String mData5) {
		setmData5(mData5);
	}

	public String getmDescription() {
		return mDescription;
	}

	public void setmDescription(String mDescription) {
		this.mDescription = mDescription;
	}

	public void setdescription(String mDescription) {
		setmDescription(mDescription);
	}

}
