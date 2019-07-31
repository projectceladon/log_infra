/* Copyright (C) 2019 Intel Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
