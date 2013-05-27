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
package com.intel.crashreport.specific;

/**
 * @brief Crashsignature represents a signature for a crash according to its
 * characteristic data
 */
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

	/**
	 * Constructor for a crash signature
	 *
	 * @param event from which the signature is computed
	 */
	public CrashSignature(Event event){

		type = event.getType();
		data0 = event.getData0();
		data1 = event.getData1();
		if (type.equals("TOMBSTONE"))
		{
			data2 = "";
			data3 = event.getData3();
		}
		else
			data2 = event.getData2();
	}

	/**
	 * Constructor for a crash signature
	 *
	 * @param type is the crash type
	 * @param data0 is the crash data0
	 * @param data1 is the crash data1
	 * @param data2 is the crash data2
	 */
	public CrashSignature(String type, String data0, String data1, String data2) {
		this.type = type;
		this.data0 = data0;
		this.data1 = data1;
		this.data2 = data2;
	}

	/**
	 * Constructor for a crash signature
	 *
	 * @param type is the crash type
	 * @param data0 is the crash data0
	 * @param data1 is the crash data1
	 * @param data2 is the crash data2
	 * @param data3 is the crash data3
	 */
	public CrashSignature(String type, String data0, String data1, String data2, String data3) {
		this.type = type;
		this.data0 = data0;
		this.data1 = data1;
		if (!type.equals("TOMBSTONE")){
			this.data2 = data2;
		}
		else
			this.data2 ="";
			this.data3 = data3;
	}

	/** Return type passed to the constructor. */
	public String getType() {
		return type;
	}

	/** Return data0 passed to the constructor. */
	public String getData0() {
		return data0;
	}

	/** Return data1 passed to the constructor. */
	public String getData1() {
		return data1;
	}

	/** Return data2 passed to the constructor. */
	public String getData2() {
		return data2;
	}

	/** Return data3 passed to the constructor. */
	public String getData3() {
		return data3;
	}

	/**
	 * @brief return the query (with crash signature) for requests on events
	 * database table
	 *
	 * @return the string containing the query
	 */
	public String querySignature() {
		String query;
		if (!type.equals("TOMBSTONE"))
			query = KEY_TYPE + " = '" + type + "' and " +
					KEY_DATA0 + " = '" + data0 + "' and " +
					KEY_DATA1 + " = '" + data1 + "' and " +
					KEY_DATA2 + " = '" + data2 + "'";
		else
			query = KEY_TYPE + " = '" + type + "' and " +
					KEY_DATA0 + " = '" + data0 + "' and " +
					KEY_DATA1 + " = '" + data1 + "' and " +
					KEY_DATA3 + " = '" + data3 + "'";
		return query;
	}

	/**
	 * Indicate if a signature is empty (or not) and could be then used (or not)
	 *
	 * @return true if at least one relevant data field is null, false otherwise
	 */
	public boolean isEmpty() {

		if (!type.equals("TOMBSTONE"))
			return (data0.equals("") || data1.equals("") || data2.equals(""));
		else
			return (data0.equals("") || data1.equals("") || data3.equals(""));
	}

}
