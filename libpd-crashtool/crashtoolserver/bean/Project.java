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


public class Project {
	
	private Long id;
	private Mainline mainline;
	private Variant variant;
	private String tracker;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Mainline getMainline() {
		return mainline;
	}
	public void setMainline(Mainline mainline) {
		this.mainline = mainline;
	}
	public Variant getVariant() {
		return variant;
	}
	public void setVariant(Variant variant) {
		this.variant = variant;
	}
	
	public String getTracker() {
        return tracker;
    }
    public void setTracker(String tracker) {
        this.tracker = tracker;
    }

    @Override
    public String toString() {
        return "Project [id=" + id + ", mainline=" + mainline + ", variant=" + variant + ", tracker=" + tracker + "]";
    }
	
	
}
