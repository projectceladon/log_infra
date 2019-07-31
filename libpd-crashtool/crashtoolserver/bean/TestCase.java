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

/**
 * Bean TestCase, used to transmit TestCase data between a client and a server
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
