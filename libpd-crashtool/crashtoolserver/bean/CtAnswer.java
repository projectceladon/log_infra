package com.intel.crashtoolserver.bean;

public class CtAnswer {


	private boolean success;
	private String message;
	private int httpStatus;

	public int getHttpStatus() {
		return httpStatus;
	}
	public void setHttpStatus(int httpStatus) {
		this.httpStatus = httpStatus;
	}
	/**
	 * @return the success
	 */
	public boolean isSuccess() {
		return success;
	}
	/**
	 * @param success the success to set
	 */
	public void setSuccess(boolean success) {
		this.success = success;
	}
	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}
	/**
	 * @param message the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * @param success
	 * @param message
	 */
	public CtAnswer(boolean success, String message) {
		this(success, message, -1);
	}
	
	/**
	 * @param success
	 * @param message
	 */
	public CtAnswer(boolean success, String message, int httpStatus) {
		this.success = success;
		this.message = message;
		this.httpStatus = httpStatus;
	}

	@Override
	public String toString() {
		return "CtAnswer [success=" + success + ", message=" + message
				+ ", httpStatus=" + httpStatus + "]";
	}
}
