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

/**
 * Bean TestCase, used to transmit TestCase data between a client and a server
 * 
 * Be aware that bean is used by CLOTA and MPTA, do not rename package and class
 * name for down ward.
 *
 * @author mauret
 */
public class TestCase implements Serializable {

	private static final long serialVersionUID = 1002484331594190958L;

	private Long id;
	private String uuid;
	private String name;
	private int iteration;
	private String dateDutString;
	private String dateHostString;
	private String engine;	
	
	public TestCase(String uuid, String name, int iteration, String dateDutString,
			String dateHostString, String engine) {
		this(null, uuid, name, iteration, dateDutString, dateHostString, engine);
	}
	
	private TestCase(Long id, String uuid, String name, int iteration,
			String dateDutString, String dateHostString, String engine) {
		super();
		this.id = id;
		this.uuid = uuid;
		this.name = name;
		this.iteration = iteration;
		this.dateDutString = dateDutString;
		this.dateHostString = dateHostString;
		this.engine = engine;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getIteration() {
		return iteration;
	}
	public void setIteration(int iteration) {
		this.iteration = iteration;
	}

	public String getEngine() {
		return engine;
	}
	public void setEngine(String engine) {
		this.engine = engine;
	}
	public String getDateDutString() {
		return dateDutString;
	}
	public void setDateDutString(String dateDutString) {
		this.dateDutString = dateDutString;
	}
	public String getDateHostString() {
		return dateHostString;
	}
	public void setDateHostString(String dateHostString) {
		this.dateHostString = dateHostString;
	}

	@Override
	public String toString() {
		return "TestCase [id=" + id + ", uuid=" + uuid + ", name=" + name
				+ ", iteration=" + iteration + ", dateDutString="
				+ dateDutString + ", dateHostString=" + dateHostString
				+ ", engine=" + engine + "]";
	}
	
}
