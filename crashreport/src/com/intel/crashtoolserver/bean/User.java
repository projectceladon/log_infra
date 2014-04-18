package com.intel.crashtoolserver.bean;

public class User {

	private Long id;
	
	private Long wwid;
	
	private String name;

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
	 * @return the wwid
	 */
	public Long getWwid() {
		return wwid;
	}

	/**
	 * @param wwid the wwid to set
	 */
	public void setWwid(Long wwid) {
		this.wwid = wwid;
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
}
