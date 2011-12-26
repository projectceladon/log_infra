package com.intel.crashreport;

public class HistoryEvent {
	private static final int HAS_OPTION = 5;

	private String eventName = "";
	private String eventId = "";
	private String date = "";
	private String type = "";
	private String option = "";

	public HistoryEvent() {}

	public HistoryEvent(String histEvent) {
		fillEvent(histEvent);
	}

	public void fillEvent(String event) {
		if (event.length() != 0) {
			try {
				String eventList[] = event.split("\\s+");
				if (eventList.length == HAS_OPTION) {
					eventName = eventList[0];
					eventId = eventList[1];
					date = eventList[2];
					type = eventList[3];
					option = eventList[4];
				} else {
					eventName = eventList[0];
					eventId = eventList[1];
					date = eventList[2];
					type = eventList[3];
				}
			} catch (NullPointerException e) {
				Log.w("HistoryEvent: event format not recognised : " + event);
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

}
