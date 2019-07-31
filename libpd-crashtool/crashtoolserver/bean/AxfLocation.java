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

public class AxfLocation implements Serializable {

    private static final long serialVersionUID = 681624506803727123L;
    private Long id;
    private String modemName;
    private String location;
    private Date date;
    
	public AxfLocation() {    
    }
    
    public AxfLocation(String modemName, String location) {
        this(null, modemName, location);
    }   

    private AxfLocation(Long id, String modemName, String location) {
        super();
        this.id = id;
        this.modemName = modemName;
        this.location = location;
    }

    public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getModemName() {
		return modemName;
	}

	public void setModemName(String modemName) {
		this.modemName = modemName;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	
	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	@Override
    public String toString() {
        return "Modem [id=" + id + ", modemName=" + modemName 
        		+ ", location=" + location + ", date=" + date + "]";
    }
}
