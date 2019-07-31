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
 * @author mauret
 */
public class Variant implements Serializable {

	private static final long serialVersionUID = 2123982837115733180L;

	private Long id;
	private String name;
	private String displayedName;
	private boolean defaultVariant;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDisplayedName() {
		return displayedName;
	}
	public void setDisplayedName(String displayedName) {
		this.displayedName = displayedName;
	}
	
	public boolean isDefaultVariant() {
		return defaultVariant;
	}
	public void setDefaultVariant(boolean defaultVariant) {
		this.defaultVariant = defaultVariant;
	}
	@Override
	public String toString() {
		return "Variant [id=" + id + ", name=" + name + ", displayedName="
				+ displayedName + ", defaultVariant=" + defaultVariant + "]";
	}


}
