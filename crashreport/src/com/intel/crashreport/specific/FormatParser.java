/* Phone Doctor (CLOTA)
 *
 * Copyright (C) Intel 2014
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
 * Author: Nicolas BENOIT <nicolasx.benoit@intel.com>
 */

package com.intel.crashreport.specific;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FormatParser{

	private Event mEvent = null;



	public FormatParser(Event aEvent){
		mEvent = aEvent;

	}

	public void execFormat(){

		if (mEvent.getType().equals("SELINUX_VIOLATION")) {
			auditFormat();
		}
	}



	private void auditFormat(){

		//main source of data is in DATA5
		String sSourceTrace = mEvent.getData5();
		Matcher simpleMatcher;
		String sGroup;
		String[] splitString;

		//DATA 0 : should contain the content of comm part of the SELinux violation
		Pattern patternData0 = java.util.regex.Pattern.compile("comm=\".*");
		simpleMatcher = patternData0.matcher(sSourceTrace);
		if (simpleMatcher.find()){
			sGroup = simpleMatcher.group();
			if(sGroup != null) {
				splitString = sGroup.split("\"");
				if (splitString.length >= 2 ) {
					mEvent.setData0(splitString[1]);
				}
			}
		}

		//DATA 1 : should contain the content of tcontext part of the SELinux violation
		Pattern patternData1 = java.util.regex.Pattern.compile("tcontext=.* ");
		simpleMatcher = patternData1.matcher(sSourceTrace);
		if (simpleMatcher.find()){
			sGroup = simpleMatcher.group();
			if(sGroup != null) {
				splitString = sGroup.split(" ");
				if (splitString.length >= 1 ) {
					mEvent.setData1(splitString[0]);
				}
			}
		}

		//DATA 2 : should contain the action part of the SELinux violation
		Pattern patternData2 = java.util.regex.Pattern.compile("\\{.*\\} ");
		simpleMatcher = patternData2.matcher(sSourceTrace);
		if (simpleMatcher.find()){
			sGroup = simpleMatcher.group();
			if(sGroup != null) {
				sGroup = sGroup.replace("}", "");
				mEvent.setData2(sGroup.replace("{", ""));
			}
		}

		//DATA 3 : should contain the content of scontext of the SELinux violation
		Pattern patternData3 = java.util.regex.Pattern.compile("scontext=.* ");
		simpleMatcher = patternData3.matcher(sSourceTrace);
		if (simpleMatcher.find()){
			sGroup = simpleMatcher.group();
			if(sGroup != null) {
				splitString = sGroup.split(" ");
				if (splitString.length >= 1 ) {
					mEvent.setData3(splitString[0]);
				}
			}
		}

		//DATA 4 : should contain the pid of the SELinux violation
		Pattern patternData4 = java.util.regex.Pattern.compile("pid=[0-9]* ");
		simpleMatcher = patternData4.matcher(sSourceTrace);
		if (simpleMatcher.find()){
			sGroup = simpleMatcher.group();
			if(sGroup != null) {
				splitString = sGroup.split("=");
				if (splitString.length >= 2 ) {
					mEvent.setData4(splitString[1]);
				}
			}
		}
	}

}
