package com.intel.crashtoolserver.bean;

import java.io.Serializable;

/**
 * Bean Uptime
 *
 * @author greg
 *
 */
public class Uptime implements Serializable {

	private static final long serialVersionUID = -3933669314600924927L;

	private Long id;
	private Long buildId;
	private Long deviceId;
	private long uptimeReboot;
	private long uptimeOther;

	/**
	 * Default constructor
	 */
	public Uptime() {
		// do nothing
		uptimeReboot = 0;
		uptimeOther = 0;
	}

	/**
	 * Key Constructor
	 *
	 * @param buildId Build id
	 * @param deviceId Device id
	 */
	public Uptime(Long buildId, Long deviceId) {
		this();
		this.buildId = buildId;
		this.deviceId = deviceId;
	}

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return the buildId
	 */
	public Long getBuildId() {
		return buildId;
	}

	/**
	 * @param buildId the buildId to set
	 */
	public void setBuildId(Long buildId) {
		this.buildId = buildId;
	}

	/**
	 * @return the deviceId
	 */
	public Long getDeviceId() {
		return deviceId;
	}

	/**
	 * @param deviceId the deviceId to set
	 */
	public void setDeviceId(Long deviceId) {
		this.deviceId = deviceId;
	}

	/**
	 * @return the uptimeReboot
	 */
	public long getUptimeReboot() {
		return uptimeReboot;
	}

	/**
	 * @param uptimeReboot the uptimeReboot to set
	 */
	public void setUptimeReboot(long uptimeReboot) {
		this.uptimeReboot = uptimeReboot;
	}

	/**
	 * @return the uptimeOther
	 */
	public long getUptimeOther() {
		return uptimeOther;
	}

	/**
	 * @param uptimeOther the uptimeOther to set
	 */
	public void setUptimeOther(long uptimeOther) {
		this.uptimeOther = uptimeOther;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Uptime [id=" + id + ", buildId=" + buildId + ", deviceId=" + deviceId
				+ ", uptimeReboot=" + uptimeReboot + ", uptimeOther=" + uptimeOther + "]";
	}
}
