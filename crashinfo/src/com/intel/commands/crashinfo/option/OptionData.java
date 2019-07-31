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

package com.intel.commands.crashinfo.option;

public class OptionData {

	private String                      key          = null;
	private String                      shortKey       = null;
	private boolean                     bValue        = false;
	private Options.Multiplicity        multiplicity = null;
	private java.util.regex.Pattern     pattern      = null;
	private java.util.ArrayList<String> values       = null;
	private java.util.ArrayList<String> resultKeys      = null;
	private boolean					bMainOption = false;
	private String						helpValue	=null;

	/**
	 * @param sKey  : Name of the option
	 * @param sShortKey  : Short name of the option
	 * @param sPattern : regex pattern to check value found for the option
	 * @param value : indicates if a is required with the option
	 * @param multiplicity : indicates how many options are allowed
	 * @param bMainOption : indicates if the option is a main option
	 */
	public OptionData(String sKey,
			String sShortKey,
			String sPattern,
			boolean value,
			Options.Multiplicity multiplicity,
			boolean bMainOption,
			String sHelpValue) {
		if (sKey          == null) throw new IllegalArgumentException("OptionData: key may not be null");
		if (multiplicity == null) throw new IllegalArgumentException("OptionData: multiplicity may not be null");

		//.... The data describing the option

		this.key          = sKey;
		this.shortKey    = sShortKey;
		this.bValue        = value;
		this.multiplicity = multiplicity;
		this.bMainOption = bMainOption;
		this.helpValue = sHelpValue;

		//.... Create the pattern to match this option
		if (value) {
			pattern = java.util.regex.Pattern.compile(sPattern);
		}

		//.... Structures to hold result data
		resultKeys  = new java.util.ArrayList<String>();
		if (value) {
			values = new java.util.ArrayList<String>();
		}
	}

	/**
	 * @return the helpValue
	 */
	public String getHelpValue() {
		return helpValue;
	}

	/**
	 * @param helpValue the helpValue to set
	 */
	public void setHelpValue(String helpValue) {
		this.helpValue = helpValue;
	}

	/**
	 * Add a result to the optionData
	 * @param sResultData (string found for matching arg)
	 * @param sValueData (value to use if present)
	 */
	public void addResult(String sResultData, String sValueData) {
		if (sResultData == null) throw new IllegalArgumentException("OptionData: detailData may not be null");
		resultKeys.add(sResultData);
		if (bValue) {
			if (sValueData == null) throw new IllegalArgumentException("OptionData: valueData may not be null");
			values.add(sValueData);
		}
	}

	/**
	 * @return if the option is a main Option
	 */
	public boolean isMainOption()
	{
		return bMainOption;
	}

	/**
	 *  Getter method for <code>key</code> property
	 * <p>
	 * @return The value for the <code>key</code> property
	 */
	public String getKey() {
		return key;
	}

	/**
	 * @param iIndex
	 * @return string valeu depending on index
	 */
	public String getValues(int iIndex) {
		if (iIndex < values.size()){
			return values.get(iIndex);
		}else{
			return null;
		}
	}

	/**
	 * Getter method for <code>value</code> property
	 * <p>
	 * @return The value for the <code>value</code> property
	 */

	boolean useValue() {
		return bValue;
	}

	/**
	 * Getter method for <code>multiplicity</code> property
	 * <p>
	 * @return The value for the <code>multiplicity</code> property
	 */

	Options.Multiplicity getMultiplicity() {
		return multiplicity;
	}

	/**
	 * Getter method for <code>pattern</code> property
	 * <p>
	 * @return The value for the <code>pattern</code> property
	 */

	java.util.regex.Pattern getPattern() {
		return pattern;
	}

	/**
	 * Get the number of results found for this option, which is number of times the key matched
	 * <p>
	 * @return The number of results
	 */

	public int getResultCount() {
		return resultKeys.size();
	}

	/**
	 * @param myArg
	 * @return if myArg matches the current option Data
	 */
	public boolean matchArg(String myArg){
		if ((myArg == "")|| (myArg==null)){
			return false;
		}
		else if (myArg.equals(key)){
			return true;
		} else if (myArg.equals(shortKey)){
			return true;
		}
		return false;
	}

	/**
	 * @return the shortKey
	 */
	public String getShortKey() {
		return shortKey;
	}
}
