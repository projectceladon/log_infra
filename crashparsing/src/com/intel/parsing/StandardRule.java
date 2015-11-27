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

package com.intel.parsing;

import com.intel.crashreport.core.ParsableEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StandardRule {

	private static final String TAG_INPUT_FILE = "file";

	private String mId;
	private String mParsingMethod;
	private String mInputType;
	private String mInputValue;
	private String mMatchingPattern;
	private String mOutputType;
	private BufferedReader mReader = null;
	ArrayList<String> mPatternOptionList = new ArrayList<String>();;


	public String getId() {
		return mId;
	}

	public void setId(String mId) {
		this.mId = mId;
	}

	//proxy function for JSONbuilder
	public void setid(String mId) {
		this.setId(mId);
	}

	public String getParsingMethod() {
		return mParsingMethod;
	}

	public void setParsingMethod(String mParsingMethod) {
		this.mParsingMethod = mParsingMethod;
	}

	//proxy function for JSONbuilder
	public void setparsing_method(String mParsingMethod) {
		this.setParsingMethod(mParsingMethod);
	}

	public String getInputType() {
		return mInputType;
	}

	public void setInputType(String mInputType) {
		this.mInputType = mInputType;
	}

	//proxy function for JSONbuilder
	public void setinput_type(String mInputType) {
		this.setInputType(mInputType);
	}

	public String getInputValue() {
		return mInputValue;
	}

	public void setInputValue(String mInputValue) {
		this.mInputValue = mInputValue;
	}

	//proxy function for JSONbuilder
	public void setinput_value(String mInputValue) {
		this.setInputValue(mInputValue);
	}
	public String getMatchingPattern() {
		return mMatchingPattern;
	}

	public void setMatchingPattern(String mMatchingPattern) {
		this.mMatchingPattern = mMatchingPattern;
	}

	//proxy function for JSONbuilder
	public void setmatching_pattern(String mMatchingPattern) {
		this.setMatchingPattern(mMatchingPattern);
	}
	public String getOutputType() {
		return mOutputType;
	}

	public void setOutputType(String mOutputType) {
		this.mOutputType = mOutputType;
	}

	//proxy function for JSONbuilder
	public void setoutput_type(String mOutputType) {
		this.setOutputType(mOutputType);
	}

	public void addpattern_options(String aOption){
		mPatternOptionList.add(aOption);
	}

	public void analyzeEvent(ParsableEvent aEvent){
		//step1 : open the input for parsing
		if (getInputType().equals(TAG_INPUT_FILE)){
			if (!openInputForfileGrep(getInputValue(),aEvent.getCrashDir())){
				APLog.e("can't open Input : " + getInputValue());
				return;
			}
		}//other INPUT TYPE could be added here
		else {
			APLog.e("Unmanaged InputType : " + getInputType());
			return;
		}

		//Step2 : do the parsing depending on the rule configuration
		String sExtractData = parseReader();
		//step3 :  fill output data in appropriate field
		fillEventData(sExtractData, aEvent);
		closeGenericReader();
	}

	private void fillEventData(String sDataToUse, ParsableEvent eEventToFill) {
		if (getOutputType().equals("DATA0")) {
			eEventToFill.setData0(sDataToUse);
		} else if (getOutputType().equals("DATA1")) {
			eEventToFill.setData1(sDataToUse);
		}else if (getOutputType().equals("DATA2")) {
			eEventToFill.setData2(sDataToUse);
		}else if (getOutputType().equals("DATA3")) {
			eEventToFill.setData3(sDataToUse);
		}else if (getOutputType().equals("DATA4")) {
			eEventToFill.setData4(sDataToUse);
		}else if (getOutputType().equals("DATA5")) {
			eEventToFill.setData5(sDataToUse);
		}else  {
			APLog.e("Unknown OutputType : " + getOutputType());
		}
	}

	private String parseReader() {
		if (mReader == null){
			APLog.e("Reader null, can't parse");
			return "";
		}
		if (getParsingMethod().equals("textsearch")){
			return textSearch();
		}
		APLog.w("no ParsingMethod found");
		return "";
	}

	private String textSearch() {
		//simple parsing method for text pattern - one line result max
		Pattern aPattern = getPattern();
		// loop on reader
		String sCurLine;
		try {
			while ((sCurLine = mReader.readLine()) != null) {
				String sTmp;
				sTmp = searchLineByPattern(sCurLine, aPattern);
				if (!sTmp.isEmpty()){
					return sTmp;
				}
			}
		} catch (IOException e) {
			APLog.e( "textSearch - IOException : " + e.getMessage());
		}
		return "";
	}

	private String searchLineByPattern(String aLine, Pattern aPattern){
		Matcher simpleMatcher = aPattern.matcher(aLine);
		//Warning : this should be initialized at null
		String sResult = "";
		if (simpleMatcher.find()){
			String sGroup = simpleMatcher.group();
			if  (mPatternOptionList.contains("full_line")) {
				sResult = sGroup;
			}else {
				String sSeparator = getSeparatorForSearch();
				// TO DO in future add options management for left and iReturnIndex
				int iReturnIndex = 1;
				if (sGroup != null){
					String[] splitString = sGroup.split(sSeparator, 2);
					if (splitString.length > iReturnIndex ){
						sResult = splitString[iReturnIndex];
					}
				}
			}
		}
		//to be sure returning non null value
		if (sResult == null) return "";
		return sResult;
	}

	private String getSeparatorForSearch(){
		//TO DO in future add options management
		return ":";
	}

	private Pattern getPattern(){
		if  (mPatternOptionList.contains("prefix_search")) {
			return java.util.regex.Pattern.compile(getMatchingPattern() +".*");
		}
		//default search is "contains" regex
		return java.util.regex.Pattern.compile(".*" + getMatchingPattern() +".*");
	}

	private void closeGenericReader() {
		if (mReader != null) {
			try {
				mReader.close();
			} catch (IOException e) {
				APLog.e("IOException on close reader : " + e.getMessage());
			}
		}
	}

	private boolean openInputForfileGrep(String aPattern, String aFolder){
		//possible improvement : use a Input Interface with dedicated class
		//depending on type of input
		Pattern patternFile = java.util.regex.Pattern.compile(aPattern);

		File searchFolder = new File(aFolder );
		File[] files = searchFolder.listFiles();
		String sFileInput = "";
		if(files!=null) {
			for(File f: files) {
				Matcher matcherFile = patternFile.matcher(f.getName());
				if (matcherFile.find()){
					sFileInput = aFolder + "/" + f.getName();
					break;
				}
			}
		}
		if (!sFileInput.isEmpty()){
			try {
				mReader = new BufferedReader(new FileReader(sFileInput));
			} catch (FileNotFoundException e) {
				return false;
			}
			return true;
		}
		return false;
	}


}
