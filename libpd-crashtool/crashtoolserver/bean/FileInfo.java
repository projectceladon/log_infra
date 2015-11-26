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

package com.intel.crashtoolserver.bean;

import java.io.Serializable;

/**
 * Bean file info, used to transmit file meta data between a client and a server
 *
 * @author mauretx
 *
 */
public class FileInfo implements Serializable {

	/** Serial Id */
	private static final long serialVersionUID = 1505868301430587660L;

	private String name;
	private String path;
	/** File Size in byte */
	private long size;
	/** Day dir name format : yyyy-mm-dd */
	private String dayDir;
	private String eventId;
	private String origin;

	/**
	 * Default constructor
	 */
	public FileInfo() {
		super();
	}

	/**
	 *
	 * @param name
	 * @param path
	 * @param size
	 * @param dayDir
	 * @param eventId
	 */
	 @Deprecated
	public FileInfo(String name, String path, long size, String dayDir,
			String eventId) {
		 
		this.name = name;
		this.path = path;
		this.size = size;
		this.dayDir = dayDir;
		this.eventId = eventId;
		this.origin = null;
	}

	// TODO check if it is still used, if not remove
	/**
	 *
	 * @param name
	 * @param path
	 * @param size
	 */
	@Deprecated
	public FileInfo(String name, String path, long size) {
		super();
		this.name = name;
		this.path = path;
		this.size = size;
	}

	/**
	 * Main Constructor for file Info
	 *
	 * @param name
	 * @param path
	 * @param size
	 * @param dayDir
	 * @param eventId
	 */
	public FileInfo(String name, String path, long size, String dayDir,
			String eventId, Event.Origin origin) {
		super();
		this.name = name;
		this.path = path;
		this.size = size;
		this.dayDir = dayDir;
		this.eventId = eventId;
		if (origin != null) {
			this.origin = origin.name();
		}
	}
	
	/**
	 * constructor using origin as String as it is much easier than 
	 * @param name
	 * @param path
	 * @param size
	 * @param dayDir
	 * @param eventId
	 * @param origin
	 */
	public FileInfo(String name, String path, long size, String dayDir,
			String eventId, String origin) {
		
		this.name = name;
		this.path = path;
		this.size = size;
		this.dayDir = dayDir;
		this.eventId = eventId;
		this.origin = origin;
	}

	public String getDayDir() {
		return dayDir;
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

	public String getOrigin() {
		return origin;
	}

	public void setOrigin(String origin) {
		this.origin = origin;
	}

	public Event.Origin getOriginEnum() {
		if (origin == null) {
			return  null;
		}

		try {
			return Event.Origin.valueOf(origin);
		} catch (Exception e) {
			//do nothing
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "FileInfo [name=" + name + ", path=" + path + ", size=" + size + ", dayDir="
				+ dayDir + ", eventId=" + eventId + ", origin=" + origin + "]";
	}
}
