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


/**
 * Wraps all Intel Tracker Device's information
 * @author tpadovax
 * 
 */
public class DeviceTracker {
	
    private String location;
	private String model;
	private String ownerEmail;
	private String ownerFirstName;
	private String ownerLastName;
	private String serialNumber;
	private String status;
	
    public String getLocation() {
	    return location;
	}
	public void setLocation(String location) {
	    this.location = location;
	}
    public String getModel() {
        return model;
    }
    public void setModel(String model) {
        this.model = model;
    }
    public String getOwnerEmail() {
        return ownerEmail;
    }
    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
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
    public String getSerialNumber() {
        return serialNumber;
    }
    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    
    @Override
    public String toString() {
        return "DeviceTracker [location=" + location + ", model=" + model + ", ownerEmail=" + ownerEmail
                + ", ownerFirstName=" + ownerFirstName + ", ownerLastName=" + ownerLastName + ", serialNumber="
                + serialNumber + ", status=" + status + "]";
    }

	
}
