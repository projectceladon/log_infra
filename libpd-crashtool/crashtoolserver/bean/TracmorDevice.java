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
	private String notes;	

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

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
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
				+ ", notes=" + notes + "]";
	}
}
