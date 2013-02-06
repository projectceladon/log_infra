/* Crash Report (CLOTA)
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
 * Author: Mathieu Auret <mathieu.auret@intel.com>
 */

package com.intel.crashtoolserver.bean;

//import com.intel.crashtool.crashtoolDb.shared.MessageHelper;


/**
 * Structure to wrap upload reslut
 * @author mauret
 *
 */
public class CtResult {

	public enum Result {
		RECEIVED, NOTHING_TO_SEND, INVALID, FAILURE, CANCELLED, FILE_NOT_FOUND
	}

	public enum Mode {
		Event, LogFile
	}

	private Result eventUploaded;
	private Result fileLogUploaded;

	public CtResult() {
		super();
	}

	public CtResult(Result eventUploaded, Result fileLogUploaded) {
		super();
		this.eventUploaded = eventUploaded;
		this.fileLogUploaded = fileLogUploaded;
	}

	/**
	 * @return the eventUploaded
	 */
	public Result getEventUploaded() {
		return eventUploaded;
	}

	/**
	 * @param eventUploaded the eventUploaded to set
	 */
	public void setEventUploaded(Result eventUploaded) {
		this.eventUploaded = eventUploaded;
	}

	/**
	 * @return the fileLogUploaded
	 */
	public Result getFileLogUploaded() {
		return fileLogUploaded;
	}

	/**
	 * @param fileLogUploaded the fileLogUploaded to set
	 */
	public void setFileLogUploaded(Result fileLogUploaded) {
		this.fileLogUploaded = fileLogUploaded;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "CtResult [eventUploaded=" + eventUploaded + ", fileLogUploaded="
				+ fileLogUploaded + "]";
	}


	/**
	 *
	 * @param result
	 * @param message
	 */
	/*
	public void fillUploadResult(String message, Mode mode) {

		Result result = Result.FAILURE;

		if (MessageHelper.ACK.equals(message)) {
			result = CtResult.Result.RECEIVED;
		}
		else if (MessageHelper.INVALID.equals(message)) {
			result = CtResult.Result.INVALID;
		}
		else if (MessageHelper.FAILURE.equals(message)) {
			result = CtResult.Result.FAILURE;
		}

		if (mode == Mode.Event) {
			this.setEventUploaded(result);
			if (result == Result.FAILURE || result == Result.INVALID) {
				this.setFileLogUploaded(CtResult.Result.CANCELLED);
			}
		}
		else if (mode == Mode.LogFile) {
			this.setFileLogUploaded(result);
		}
	}*/
}
