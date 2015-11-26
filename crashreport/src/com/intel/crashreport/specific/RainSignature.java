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
	private String data3 = "";
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
		data2 = event.getData2();

		if (type.equals("TOMBSTONE")) {
			data3 = event.getData3();
		}
	}

	/**
	 * Constructor for a rain signature computed from a crash signature
	 *
	 * @param crashSignature from which the signature is computed
	 */
	public RainSignature(RainSignature crashSignature){

		type = crashSignature.getType();
		data0 = crashSignature.getData0();
		data1 = crashSignature.getData1();
		data2 = crashSignature.getData2();
		data3 = crashSignature.getData3();
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
		this.data2 = data2;

		if (this.type.equals("TOMBSTONE")) {
			this.data3 = data3;
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

	private String replaceEscapeSequence(String sequence){
		return sequence.replace("'", "''");
	}

	/**
	 * @brief return the query (with rain signature) for requests on rain of
	 * crashes database
	 *
	 * @return the string containing the query
	 */
	public String querySignature() {
		String query = KEY_TYPE + " = '" + replaceEscapeSequence(type) + "' and " +
			KEY_DATA0 + " = '" + replaceEscapeSequence(data0) + "' and " +
			KEY_DATA1 + " = '" + replaceEscapeSequence(data1) + "' and " +
			KEY_DATA2 + " = '" + replaceEscapeSequence(data2) + "'";

		if (this.type.equals("TOMBSTONE")) {
			query += " and " +
				KEY_DATA3 + " = '" + replaceEscapeSequence(data3) + "'";
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
			return (data0.equals("") || data1.equals("") || (data2.equals("") && data3.equals("")));
		else
			return (data0.equals("") || data1.equals("") || data2.equals(""));
	}
}
