package com.intel.crashtoolserver.bean;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author mauret
 *
 */
public class Mainline implements Serializable {

	private static final long serialVersionUID = 6854769745208757379L;

	private Long id;
	private String name;
	private Date startingDate;
	private boolean defaultMainline;
	private String version;
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
	/**
	 * @return the startingDate
	 */
	public Date getStartingDate() {
		return startingDate;
	}
	/**
	 * @param startingDate the startingDate to set
	 */
	public void setStartingDate(Date startingDate) {
		this.startingDate = startingDate;
	}
	/**
	 * @return the defaultMainline
	 */
	public boolean isDefaultMainline() {
		return defaultMainline;
	}
	/**
	 * @param defaultMainline the defaultMainline to set
	 */
	public void setDefaultMainline(boolean defaultMainline) {
		this.defaultMainline = defaultMainline;
	}
	/**
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}
	/**
	 * @param version the version to set
	 */
	public void setVersion(String version) {
		this.version = version;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Mainline [id=" + id + ", name=" + name + ", startingDate=" + startingDate
				+ ", defaultMainline=" + defaultMainline + ", version=" + version + "]";
	}


}
