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
