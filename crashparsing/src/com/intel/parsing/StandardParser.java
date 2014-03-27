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

import java.util.ArrayList;

public class StandardParser implements EventParser {

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
		return true;
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
