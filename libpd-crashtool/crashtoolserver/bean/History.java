package com.intel.crashtoolserver.bean;

public abstract class History {

	private Long userId;
	
	private String action;
	
	private Long entityModifiedId;
	
	private String description;

	/**
	 * @return the userId
	 */
	public Long getUserId() {
		return userId;
	}

	/**
	 * @param userId the userId to set
	 */
	public void setUserId(Long userId) {
		this.userId = userId;
	}

	/**
	 * @return the action
	 */
	public String getAction() {
		return action;
	}

	/**
	 * @param action the action to set
	 */
	public void setAction(String action) {
		this.action = action;
	}
	
	public Long getEntityModifiedId() {
		return entityModifiedId;
	}
	
	public void setEntityModifiedId(Long entityModifiedId) {
		this.entityModifiedId = entityModifiedId;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getHistoryType() {
		return getClass().getSimpleName();
	}
}
