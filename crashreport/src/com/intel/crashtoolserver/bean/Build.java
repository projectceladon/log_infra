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

import java.io.Serializable;
import java.util.Date;

public class Build implements Serializable {

	private static final long serialVersionUID = 5286879029277574763L;

	private static final String DEFAULT_VALUE = "";
	public static final int MAX_SIZE_BUILDID = 40;
	public final static String DEFAULT_BUILD_TYPE = "dev";
	public final static String DEFAULT_BUILD_VARIANT = "unknown";

	private Long id;
	private String buildId;
	private String name = DEFAULT_VALUE;
	private String fingerPrint = DEFAULT_VALUE;
	private String kernelVersion = DEFAULT_VALUE;
	private String buildUserHostname = DEFAULT_VALUE;
	private String modemVersion = DEFAULT_VALUE;
	private String ifwiVersion = DEFAULT_VALUE;
	private String iafwVersion  = DEFAULT_VALUE;
	private String scufwVersion  = DEFAULT_VALUE;
	private String punitVersion  = DEFAULT_VALUE;
	private String valhooksVersion  = DEFAULT_VALUE;
	private String variant;
	private String type;

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	@Deprecated
	private Long mainlineId;
	private Mainline mainline;

	// used for backwork compatibility
	public Build(String buildId) {
		super();
		this.buildId = buildId;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}


	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	private Date date;

	/**
	 * Default constructor
	 */
	public Build() {}


	/**
	 * Instantiate a build
	 *
	 * @param buildId
	 * @param fingerPrint
	 * @param kernelVersion
	 * @param buildUserHostname
	 * @param modemVersion
	 * @param ifwiVersion
	 * @param iafwVersion
	 * @param scufwVersion
	 * @param punitVersion
	 * @param valhooksVersion
	 */
	public Build(String buildId, String fingerPrint, String kernelVersion,
			String buildUserHostname, String modemVersion, String ifwiVersion, String iafwVersion,
			String scufwVersion, String punitVersion, String valhooksVersion) {
		super();
		this.buildId = buildId;
		this.fingerPrint = fingerPrint;
		this.kernelVersion = kernelVersion;
		this.buildUserHostname = buildUserHostname;
		this.modemVersion = modemVersion;
		this.ifwiVersion = ifwiVersion;
		this.iafwVersion = iafwVersion;
		this.scufwVersion = scufwVersion;
		this.punitVersion = punitVersion;
		this.valhooksVersion = valhooksVersion;
	}

	/**
	 * @return the buildUserHostname
	 */
	public String getBuildUserHostname() {
		return buildUserHostname;
	}

	/**
	 * @param buildUserHostname the buildUserHostname to set
	 */
	public void setBuildUserHostname(String buildUserHostname) {
		this.buildUserHostname = buildUserHostname;
	}

	/**
	 * @return the ifwiVersion
	 */
	public String getIfwiVersion() {
		return ifwiVersion;
	}

	/**
	 * @param ifwiVersion the ifwiVersion to set
	 */
	public void setIfwiVersion(String ifwiVersion) {
		this.ifwiVersion = ifwiVersion;
	}

	/**
	 * @return the fingerPrint
	 */
	public String getFingerPrint() {
		return fingerPrint;
	}

	/**
	 * @param fingerPrint the fingerPrint to set
	 */
	public void setFingerPrint(String fingerPrint) {
		this.fingerPrint = fingerPrint;
	}

	/**
	 * @return the buildId
	 */
	public String getBuildId() {
		return buildId;
	}

	/**
	 * @param buildId the buildId to set
	 */
	public void setBuildId(String buildId) {
		this.buildId = buildId;
	}
	/**
	 * @return the kernelVersion
	 */
	public String getKernelVersion() {
		return kernelVersion;
	}
	/**
	 * @param kernelVersion the kernelVersion to set
	 */
	public void setKernelVersion(String kernelVersion) {
		this.kernelVersion = kernelVersion;
	}
	/**
	 * @return the modemVersion
	 */
	public String getModemVersion() {
		return modemVersion;
	}
	/**
	 * @param modemVersion the modemVersion to set
	 */
	public void setModemVersion(String modemVersion) {
		this.modemVersion = modemVersion;
	}
	/**
	 * @return the iafwVersion
	 */
	public String getIafwVersion() {
		return iafwVersion;
	}
	/**
	 * @param iafwVersion the iafwVersion to set
	 */
	public void setIafwVersion(String iafwVersion) {
		this.iafwVersion = iafwVersion;
	}
	/**
	 * @return the scufwVersion
	 */
	public String getScufwVersion() {
		return scufwVersion;
	}
	/**
	 * @param scufwVersion the scufwVersion to set
	 */
	public void setScufwVersion(String scufwVersion) {
		this.scufwVersion = scufwVersion;
	}
	/**
	 * @return the punitVersion
	 */
	public String getPunitVersion() {
		return punitVersion;
	}
	/**
	 * @param punitVersion the punitVersion to set
	 */
	public void setPunitVersion(String punitVersion) {
		this.punitVersion = punitVersion;
	}
	/**
	 * @return the valhooksVersion
	 */
	public String getValhooksVersion() {
		return valhooksVersion;
	}
	/**
	 * @param valhooksVersion the valhooksVersion to set
	 */
	public void setValhooksVersion(String valhooksVersion) {
		this.valhooksVersion = valhooksVersion;
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
	 * @return the date
	 */
	public Date getDate() {
		return date;
	}

	/**
	 * @param date the date to set
	 */
	public void setDate(Date date) {
		this.date = date;
	}

	/**
	 * @return the mainlineId
	 */
	@Deprecated
	public Long getMainlineId() {
		return mainlineId;
	}


	/**
	 * @param mainlineId the mainlineId to set
	 */
	@Deprecated
	public void setMainlineId(Long mainlineId) {
		this.mainlineId = mainlineId;
	}

	/**
	 * @return the mainline
	 */
	public Mainline getMainline() {
		return mainline;
	}

	/**
	 * @param mainline the mainline to set
	 */
	public void setMainline(Mainline mainline) {
		this.mainline = mainline;
	}

	/**
	 * @return the variant
	 */
	public String getVariant() {
		return variant;
	}

	/**
	 * @param variant the variant to set
	 */
	public void setVariant(String variant) {
		this.variant = variant;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Build [id=" + id + ", buildId=" + buildId + ", name=" + name + ", fingerPrint="
				+ fingerPrint + ", kernelVersion=" + kernelVersion + ", buildUserHostname="
				+ buildUserHostname + ", modemVersion=" + modemVersion + ", ifwiVersion="
				+ ifwiVersion + ", iafwVersion=" + iafwVersion + ", scufwVersion=" + scufwVersion
				+ ", punitVersion=" + punitVersion + ", valhooksVersion=" + valhooksVersion
				+ ", variant=" + variant + ", mainlineId=" + mainlineId + ", mainline=" + mainline
				+ ", date=" + date + "]";
	}


}
