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
