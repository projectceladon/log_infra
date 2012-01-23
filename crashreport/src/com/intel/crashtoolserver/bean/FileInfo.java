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
 * Author: Mathieu Auret <mathieux.auret@intel.com>
 */

package com.intel.crashtoolserver.bean;

import java.io.Serializable;

/**
 * Bean file info
 *
 * @author mauretx
 *
 */
public class FileInfo implements Serializable {

	private static final long serialVersionUID = 1505868301430587660L;

	private String name;
	private String path;
	private long size;
	private String dayDir;
	private String eventId;

	public String getDayDir() {
		return dayDir;
	}

	public FileInfo(String name, String path, long size, String dayDir,
			String eventId) {
		super();
		this.name = name;
		this.path = path;
		this.size = size;
		this.dayDir = dayDir;
		this.eventId = eventId;
	}

	public void setDayDir(String dayDir) {
		this.dayDir = dayDir;
	}

	public String getEventId() {
		return eventId;
	}

	public String getEventDir() {
		return eventId;
	}

	public void setEventId(String eventId) {
		this.eventId = eventId;
	}

	public String getName() {
		return name;
	}

	public FileInfo(String name, String path) {
		super();
		this.name = name;
		this.path = path;
	}

	public FileInfo(String name, String path, long size) {
		super();
		this.name = name;
		this.path = path;
		this.size = size;
	}

	public void setName(String fileName) {
		this.name = fileName;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long fileSize) {
		this.size = fileSize;
	}

	@Override
	public String toString() {
		return "FileInfo [name=" + name + ", path=" + path + ", size=" + size
				+ ", dayDir=" + dayDir + ", eventId=" + eventId + "]";
	}
}
