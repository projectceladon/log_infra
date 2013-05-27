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
 * Author: Jean Thiry <jeanx.thiry@intel.com>
 */
package com.intel.crashreport.specific;

/**
 * @brief RainSignature represents a signature for a rain of crashes. It is made
 * of the characteristic data of the events composing this rain.
 */
public class RainSignature {

	/** Private class attributes */
	private String type;
	private String data0;
	private String data1;
	private String data2;
	private String data3;
	/** Constants used to build database queries*/
	public static final String KEY_TYPE = "type";
	public static final String KEY_DATA0 = "data0";
	public static final String KEY_DATA1 = "data1";
	public static final String KEY_DATA2 = "data2";
	public static final String KEY_DATA3 = "data3";
	/**
	 * Constructor for a rain signature computed from an event
	 *
	 * @param event of which the signature is computed
	 */
	public RainSignature(Event event){

		type = event.getType();
		data0 = event.getData0();
		data1 = event.getData1();
		if (type.equals("TOMBSTONE")) {
			data2 = "";
			data3 = event.getData3();
		}
		else {
			data2 = event.getData2();
			data3 = "";
		}
	}

	/**
	 * Constructor for a rain signature computed from a crash signature
	 *
	 * @param crashSignature from which the signature is computed
	 */
	public RainSignature(CrashSignature crashSignature){

		type = crashSignature.getType();
		data0 = crashSignature.getData0();
		data1 = crashSignature.getData1();
		if (type.equals("TOMBSTONE")) {
			data2 = "";
			data3 = crashSignature.getData3();
		}
		else {
			data2 = crashSignature.getData2();
			data3 = "";
		}
	}

	/**
	 * Constructor for a rain signature
	 *
	 * @param type must be the crash type
	 * @param data0 must contains the crash data0
	 * @param data1 must contains the crash data1
	 * @param data2 must contains the crash data2
	 * @param data3 must contains the crash data3
	 */
	public RainSignature(String type, String data0, String data1, String data2, String data3) {
		this.type = type;
		this.data0 = data0;
		this.data1 = data1;
		if (this.type.equals("TOMBSTONE")) {
			this.data2 = "";
			this.data3 = data3;
		}
		else {
			this.data2 = data2;
			this.data3 = "";
		}
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

	/** Return data2 passed to the constructor. */
	public String getData3() {
		return data3;
	}

	/**
	 * @brief return the query (with rain signature) for requests on rain of
	 * crashes database
	 *
	 * @return the string containing the query
	 */
	public String querySignature() {
		String query;
		if (this.type.equals("TOMBSTONE")) {
			query = KEY_TYPE + " = '" + type + "' and " +
					KEY_DATA0 + " = '" + data0 + "' and " +
					KEY_DATA1 + " = '" + data1 + "' and " +
					KEY_DATA3 + " = '" + data3 + "'";
		}
		else {
			query = KEY_TYPE + " = '" + type + "' and " +
				KEY_DATA0 + " = '" + data0 + "' and " +
				KEY_DATA1 + " = '" + data1 + "' and " +
				KEY_DATA2 + " = '" + data2 + "'";
		}
		return query;
	}

	/**
	 * Indicate if a signature is empty (or not) and could be then used (or not)
	 *
	 * @return true if at least one relevant data field is null, false otherwise
	 */
	public boolean isEmpty() {
		if (this.type.equals("TOMBSTONE"))
			return (data0.equals("") || data1.equals("") || data3.equals(""));
		else
			return (data0.equals("") || data1.equals("") || data2.equals(""));
	}
}
