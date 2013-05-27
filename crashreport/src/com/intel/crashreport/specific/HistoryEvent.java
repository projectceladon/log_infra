/* Phone Doctor (CLOTA)
 *
 * Copyright (C) Intel 2012
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
 * Author: Jeremy Rocher <jeremyx.rocher@intel.com>
 */

package com.intel.crashreport.specific;

import com.intel.crashreport.Log;

public class HistoryEvent {
	private static final int MIN_EVENT = 4;
	private static final int HAS_OPTION = 5;

	private String eventName = "";
	private String eventId = "";
	private String date = "";
	private String type = "";
	private String option = "";
	private boolean eventCorrupted = false;

	public HistoryEvent() {}

	public HistoryEvent(String histEvent) {
		fillEvent(histEvent);
	}

	public void fillEvent(String event) {
		if (event.length() != 0) {
			try {
				String eventList[] = event.split("\\s+");
				if (eventList.length < MIN_EVENT)
				{
					Log.w("HistoryEvent: not enough columns : " + event);
					eventCorrupted = true;
				}else if (eventList.length == MIN_EVENT) {
					eventName = eventList[0];
					eventId = eventList[1];
					date = eventList[2];
					type = eventList[3];
				}else if (eventList.length == HAS_OPTION) {
					eventName = eventList[0];
					eventId = eventList[1];
					date = eventList[2];
					type = eventList[3];
					option = eventList[4];
				} else {
					Log.w("HistoryEvent: too many columns : " + event);
					eventCorrupted = true;
				}
			} catch (NullPointerException e) {
				Log.w("HistoryEvent: event format not recognised : " + event);
				eventCorrupted = true;
			}
		}
	}

	public String getEventName() {
		return eventName;
	}

	public void setEventName(String eventName) {
		this.eventName = eventName;
	}

	public String getEventId() {
		return eventId;
	}

	public void setEventId(String eventId) {
		this.eventId = eventId;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getOption() {
		return option;
	}

	public void setOption(String option) {
		this.option = option;
	}

	public boolean isCorrupted() {
		return eventCorrupted;
	}

}
