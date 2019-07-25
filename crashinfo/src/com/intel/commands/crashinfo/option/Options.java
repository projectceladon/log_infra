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
