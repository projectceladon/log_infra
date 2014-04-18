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
