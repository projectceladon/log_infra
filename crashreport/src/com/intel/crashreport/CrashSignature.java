/* Phone Doctor (CLOTA)
 *
 * Copyright (C) Intel 2012
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Author: Charles-Edouard Vidoine <charles.edouardx.vidoine@intel.com>
 */
package com.intel.crashreport;

public class CrashSignature {

	private String type;
	private String data0;
	private String data1;
	private String data2;
	private String data3 = "";
	private String data4 = "";
	private String data5 = "";
	public static final String KEY_TYPE = "type";
	public static final String KEY_DATA0 = "data0";
	public static final String KEY_DATA1 = "data1";
	public static final String KEY_DATA2 = "data2";
	public static final String KEY_DATA3 = "data3";
	public static final String KEY_DATA4 = "data4";
	public static final String KEY_DATA5 = "data5";

	public CrashSignature(Event event){
		type = event.getType();
		data0 = event.getData0();
		data1 = event.getData1();
		data2 = event.getData2();
	}

	public CrashSignature(String type, String data0, String data1, String data2) {
		this.type = type;
		this.data0 = data0;
		this.data1 = data1;
		this.data2 = data2;
	}

	public String getType() {
		return type;
	}

	public String getData0() {
		return data0;
	}

	public String getData1() {
		return data1;
	}

	public String getData2() {
		return data2;
	}

	public String querySignature() {
		String query = KEY_TYPE + " = '" + type + "' and " +
                KEY_DATA0 + " = '" + data0 + "' and " +
                KEY_DATA1 + " = '" + data1 + "' and " +
                KEY_DATA2 + " = '" + data2 + "'";
		return query;
	}

	public boolean isEmpty() {
		return (data0.equals("") || data1.equals("") || data2.equals(""));
	}


}
