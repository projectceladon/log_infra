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

package com.intel.crashreport;

public class HomeScreenElement {

	private int elementID;
	private String elementName;
	private int elementImageResource;
	private int elementPosition;
	private boolean enabled;

	public HomeScreenElement(int id, String name) {
		this.elementID = id;
		this.elementName = name;
		this.elementImageResource = -1;
		this.enabled = false;
	}

	public HomeScreenElement(int id, String name, int imageResource, int position, boolean enabled) {
		this(id, name);
		this.elementImageResource = imageResource;
		this.elementPosition = position;
		this.enabled = enabled;
	}

	public int getElementID() {
		return this.elementID;
	}

	public String getElementName() {
		return this.elementName;
	}

	public int getElementImageResource() {
		return this.elementImageResource;
	}

	public int getElementPosition() {
		return this.elementPosition;
	}

	public boolean isEnabled() {
		return this.enabled;
	}
}
