package com.intel.crashtoolserver.bean;

/**
 * Represents a datamining result in crashtool databse
 * @author sbrouilx
 *
 */
public class DataminingResult {

	private Long eventId;
	
	private String lineText;
	
	private Integer lineNumber;
	
	private String fileName;

	public Long getEventId() {
		return eventId;
	}

	public void setEventId(Long eventId) {
		this.eventId = eventId;
	}

	public String getLineText() {
		return lineText;
	}

	public void setLineText(String lineText) {
		this.lineText = lineText;
	}

	public Integer getLineNumber() {
		return lineNumber;
	}

	public void setLineNumber(Integer lineNumber) {
		this.lineNumber = lineNumber;
	}
	
	public String getFileName() {
		return fileName;
	}
	
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
}
