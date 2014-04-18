package com.intel.crashtoolserver.bean;

import java.util.Date;

/**
 * Wraps all tracmor Device's information
 * @author mauret
 * 
 */
public class TracmorDevice {
	
	private long id;
	private String prototypeModelCode;
	private String serialNumber;
	private String ownershipStatus;
	private Long categoryId;
	private String category;
	private Long locationId;
	private String location;
	private String imei;
	private Date creationDate;
	private Date modifiedDate;
	private String ownerFirstName;
	private String ownerLastName;
	private String ownerEmail;
	private String idsid;
	private String labOwner;
	
	

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getPrototypeModelCode() {
		return prototypeModelCode;
	}

	public void setPrototypeModelCode(String prototypeModelCode) {
		this.prototypeModelCode = prototypeModelCode;
	}

	public String getSerialNumber() {
		return serialNumber;
	}

	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}

	public String getOwnershipStatus() {
		return ownershipStatus;
	}

	public void setOwnershipStatus(String ownershipStatus) {
		this.ownershipStatus = ownershipStatus;
	}

	public Long getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(Long categoryId) {
		this.categoryId = categoryId;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public Long getLocationId() {
		return locationId;
	}

	public void setLocationId(Long locationId) {
		this.locationId = locationId;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getImei() {
		return imei;
	}

	public void setImei(String imei) {
		this.imei = imei;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public String getOwnerFirstName() {
		return ownerFirstName;
	}

	public void setOwnerFirstName(String ownerFirstName) {
		this.ownerFirstName = ownerFirstName;
	}

	public String getOwnerLastName() {
		return ownerLastName;
	}

	public void setOwnerLastName(String ownerLastName) {
		this.ownerLastName = ownerLastName;
	}

	public String getOwnerEmail() {
		return ownerEmail;
	}

	public void setOwnerEmail(String ownerEmail) {
		this.ownerEmail = ownerEmail;
	}

	public String getIdsid() {
		return idsid;
	}

	public void setIdsid(String idsid) {
		this.idsid = idsid;
	}

	public String getLabOwner() {
		return labOwner;
	}

	public void setLabOwner(String labOwner) {
		this.labOwner = labOwner;
	}

	public Date getModifiedDate() {
		return modifiedDate;
	}

	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
	}
	
	@Override
	public String toString() {
		return "TracmorDevice [id=" + id + ", prototypeModelCode="
				+ prototypeModelCode + ", serialNumber=" + serialNumber
				+ ", ownershipStatus=" + ownershipStatus + ", categoryId="
				+ categoryId + ", category=" + category + ", locationId="
				+ locationId + ", location=" + location + ", imei=" + imei
				+ ", creationDate=" + creationDate + ", modifiedDate="
				+ modifiedDate + ", ownerFirstName=" + ownerFirstName
				+ ", ownerLastName=" + ownerLastName + ", ownerEmail="
				+ ownerEmail + ", idsid=" + idsid + ", labOwner=" + labOwner
				+ "]";
	}
}
