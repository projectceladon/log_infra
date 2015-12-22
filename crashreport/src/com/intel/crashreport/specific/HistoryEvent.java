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
		if (event != null && !event.isEmpty()) {
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
