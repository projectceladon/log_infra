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

package com.intel.commands.crashinfo.option;

import java.util.ArrayList;
import java.util.regex.Matcher;


public class Options {

	public static String HELP_COMMAND = "--help";

	public enum Multiplicity {

		/**
		 * Option needs to occur exactly once
		 */

		ONCE,


		/**
		 * Option needs to occur either once or not at all
		 */

		ZERO_OR_ONE,

	}

	private String[] arguments = null;
	private String helpValue;
	private java.util.ArrayList<OptionData> optionSet = null;
	private int iMaxMainOptions=1;

	public Options(String args[],String sHelp){
		if (args == null){
			arguments =  null;
		}else{
			arguments = args.clone();
		}
		optionSet = new java.util.ArrayList<OptionData>();
		helpValue = sHelp;
		//adding default help options
		addMainOption(Options.HELP_COMMAND, "-h", "", false, Multiplicity.ZERO_OR_ONE, "Displays current help");
	}

	public boolean check() {
		boolean result = true;
		boolean bHelpRequested = false;

		if (arguments == null){
			//nothing to do
		}else{
			int i = 0;
			while (i < arguments.length) {
				boolean bMatched = false;
				for (OptionData optionData : optionSet) {
					if (optionData.matchArg(arguments[i])){
						bMatched = true;
						if (optionData.getKey().equals(HELP_COMMAND)){
							bHelpRequested = true;
						}
						if (optionData.useValue()){
							String sCurKey = arguments[i];
							i++;
							if (i < arguments.length){
								Matcher tmpMatcher = optionData.getPattern().matcher(arguments[i]);
								if (tmpMatcher.matches()){
									optionData.addResult(sCurKey, arguments[i]);
								}else{
									System.err.println("Unmatching pattern : " + arguments[i] );
									result = false;
									break;
								}
							}
							else {
								System.err.println("Mandatory value missing for : " + sCurKey );
								result = false;
								break;
							}
						}else{
							optionData.addResult(arguments[i], "");
						}
					}
				}
				if (!bMatched){
					result = false;
					break;
				}
				i++;
			}
		}

		String key = "";
		int iCountMainOpt = 0;

		for (OptionData optionData : optionSet) {

			key = optionData.getKey();

			if ((optionData.isMainOption())&&(optionData.getResultCount()>0)){
				iCountMainOpt+=optionData.getResultCount();
			}

			switch (optionData.getMultiplicity()) {
			case ONCE:         if (optionData.getResultCount() != 1) {
				result = false;
				System.err.println("Mandatory argument missing : " + key );
			}
			break;
			case ZERO_OR_ONE:  if (optionData.getResultCount() > 1){
				result = false;
				System.err.println("Wrong number of occurences found for argument " + key );
			}
			break;
			}
		}
		//HELP override multiplicity
		if ((bHelpRequested) && (iCountMainOpt == 1)){
			result = true;
		}

		if (iCountMainOpt>iMaxMainOptions){
			result = false;
			System.err.println("Too many main options :  " + iCountMainOpt );
		}
		return result;

	}

	public void addMainOption(String key,
			String detail,
			String sPattern,
			boolean value,
			Options.Multiplicity multiplicity,
			String sHelp){
		optionSet.add(new OptionData(key, detail, sPattern, value, multiplicity, true, sHelp));
	}



	public void addSubOption(String key,
			String detail,
			String sPattern,
			boolean value,
			Options.Multiplicity multiplicity,
			String sHelp){
		optionSet.add(new OptionData(key, detail, sPattern, value, multiplicity, false,sHelp));
	}

	public OptionData getMainOption(){
		ArrayList<OptionData> tmpList = getMainOptions();
		if (tmpList.size() >0){
			return tmpList.get(0);
		}
		return null;
	}

	public ArrayList<OptionData> getMainOptions(){
		ArrayList<OptionData> resultList = new ArrayList<OptionData>();
		for (OptionData optionData : optionSet) {
			if (optionData.isMainOption() && optionData.getResultCount()>0){
				resultList.add(optionData);
			}
		}
		return resultList;
	}

	public ArrayList<OptionData> getSubOptions(){
		ArrayList<OptionData> resultList = new ArrayList<OptionData>();
		for (OptionData optionData : optionSet) {
			if (!optionData.isMainOption() && optionData.getResultCount()>0){
				resultList.add(optionData);
			}
		}
		return resultList;
	}


	public int getiMaxMainOptions() {
		return iMaxMainOptions;
	}


	public void setiMaxMainOptions(int iMaxMainOptions) {
		this.iMaxMainOptions = iMaxMainOptions;
	}

	public void generateHelp(){
		System.out.println("Summary: " );
		System.out.println(helpValue);
		System.out.println(" " );
		System.out.println("Options allowed for the command: " );
		boolean bFound = false;
		System.out.println("  --- Main options --- " );
		for (OptionData optionData : optionSet) {
			if (optionData.isMainOption()){
				System.out.println("   " +  optionData.getShortKey() +"," +optionData.getKey()
						+ " - " + optionData.getHelpValue());
				bFound = true;
			}
		}
		if (!bFound){
			System.out.println("No main options");
		}
		System.out.println("  --- Sub options --- " );
		bFound = false;
		for (OptionData optionData : optionSet) {
			if (!optionData.isMainOption()){
				System.out.println("   " +  optionData.getShortKey() +"," +optionData.getKey()
						+ " - " + optionData.getHelpValue());
				bFound = true;
			}
		}
		if (!bFound){
			System.out.println("No sub options");
		}
	}

}
