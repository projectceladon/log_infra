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
