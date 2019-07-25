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

import java.io.Serializable;
import java.util.Date;

/**
 * Represents a datamining process in crashtool database
 * @author sbrouilx
 */
public class DataminingProcess implements Serializable {

	public static final String PENDING_STATE = "PENDING";
	public static final String PROCESSING_STATE = "PROCESSING";
	public static final String INITIALIZING_STATE = "INITIALIZING";
	public static final String SCHEDULED_STATE = "SCHEDULED";
	public static final String DONE_STATE = "DONE";
	public static final String CANCEL_REQUESTED_STATE = "CANCEL_REQUESTED";
	public static final String CANCELLED_STATE = "CANCELLED";
	public static final String REMOVED = "REMOVED";

	private static final long serialVersionUID = 8421397687883508120L;

	private Long id;
	
	private Long parentId;
	
	private String regex;
	
	private String eventType;
	
	private Date fromDate;
	
	private String tracmorPrototypeModel;
	
	private Long projectId;
	
	private String state;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getParentId() {
		return parentId;
	}
	
	public void setParentId(Long parentId) {
		this.parentId = parentId;
	}
	
	public String getRegex() {
		return regex;
	}

	public void setRegex(String regex) {
		this.regex = regex;
	}

	public String getEventType() {
		return eventType;
	}

	public void setEventType(String eventType) {
		this.eventType = eventType;
	}
	
	public Date getFromDate() {
		return fromDate;
	}

	public void setFromDate(Date fromDate) {
		this.fromDate = fromDate;
	}
	
	public String getTracmorPrototypeModel() {
        return tracmorPrototypeModel;
    }
	
	public void setTracmorPrototypeModel(String tracmorPrototypeModel) {
        this.tracmorPrototypeModel = tracmorPrototypeModel;
    }
	
	public Long getProjectId() {
        return projectId;
    }
	
	public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }
	
	public String getState() {
		return state;
	}
	
	public void setState(String state) {
		this.state = state;
	}

	@Override
	public String toString() {
		return "DataminingProcess [id=" + id + ", parentId=" + parentId
				+ ", regex=" + regex + ", eventType=" + eventType
				+ ", fromDate=" + fromDate + ", state=" + state + "]";
	}

}
