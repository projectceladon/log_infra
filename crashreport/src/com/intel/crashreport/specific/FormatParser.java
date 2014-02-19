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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import com.intel.crashreport.Log;


public class FormatParser{

	private Event mEvent = null;



	public FormatParser(Event aEvent){
		mEvent = aEvent;

	}

	public void execFormat(){

		if (mEvent.getType().equals("SELINUX_VIOLATION")) {
			auditFormat();
		} else if (mEvent.getType().equals("FABRIC_RECOV")) {
			recoverableFabricFormat();
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

	/**
	 * Parses a line: if the line matches the criteria,
	 * we save the next line.
	 */
	private boolean parseLineTakeNextLine(String line, Pattern criteria,
						BufferedReader fileToRead,
						StringBuffer result) {
		Matcher simpleMatcher;

		if(line.isEmpty())
			return false;

		simpleMatcher = criteria.matcher(line);
		if (simpleMatcher == null || !simpleMatcher.find())
			return false;

		// We skip the line
		try {
			line = fileToRead.readLine();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		if (line == "")
			return false;

		result.append(line);
		result.append(" / ");

		return true;
	}

	/**
	 * Parses a line: if the line matches the criteria,
	 * we perform a split and save the result.
	 */
	private boolean parseLineTakeSplit(String line, Pattern criteria, String separator,
						int part, StringBuffer result) {
		Matcher simpleMatcher;
		String sGroup;
		String[] splitString;

		if(line.isEmpty() || separator.isEmpty())
			return false;

		simpleMatcher = criteria.matcher(line);
		if (simpleMatcher == null || !simpleMatcher.find())
			return false;

		sGroup = simpleMatcher.group();
		if(sGroup == null)
			return false;

		splitString = sGroup.split(separator);
		if (splitString.length < part+1)
			return false;

		result.append(splitString[part]);
		result.append(" / ");
		return true;
	}

	/**
	 * Parses a line: if the line matches the include criteria,
	 * but not the exclude criteria, we save the result.
	 */
	private boolean parseLineTakeNotContaining(String line, Pattern criteriaInc,
						Pattern criteriaExc, StringBuffer result) {
		Matcher simpleMatcher;

		if(line.isEmpty())
			return false;

		// Search for exclusion pattern
		simpleMatcher = criteriaExc.matcher(line);
		if (simpleMatcher == null || simpleMatcher.find())
			return false;

		// Search for inclusion pattern
		simpleMatcher = criteriaInc.matcher(line);
		if (simpleMatcher == null || !simpleMatcher.find())
			return false;

		result.append(line);
		result.append(" / ");
		return true;
	}

	/**
	 * Parses the Recoverable Fabric Error file to overwrite the data0->data5 fields of the event.
	 */
	private void recoverableFabricFormat() {

		StringBuffer data0StrBuffer = new StringBuffer ("");
		StringBuffer data1StrBuffer = new StringBuffer ("");
		StringBuffer data2StrBuffer = new StringBuffer ("");
		StringBuffer data3StrBuffer = new StringBuffer ("");
		StringBuffer data4StrBuffer = new StringBuffer ("");

		String sFabricFile = fileGrepSearch(".*ipanic_fabric_recv_err.*", mEvent.getCrashDir());
		if (sFabricFile == "") {
			Log.w("File \"ipanic_fabric_recv_err\" not found in event dir\n");
		}

		try {
			BufferedReader bufFabricFile = new BufferedReader(new FileReader(sFabricFile));

			Pattern patternData0 = Pattern.compile("\\*.*Fabric Flag Status.*");
			Pattern patternData1 = Pattern.compile(".*Init ID:.*");
			Pattern patternData2 = Pattern.compile(".*Command Type:.*");
			Pattern patternData3 = Pattern.compile("Associated\\ 32bit\\ Address:.*");
			Pattern patternData4 = Pattern.compile("\\*\\ .*");

			String sCurLine;
			while ((sCurLine = bufFabricFile.readLine()) != null) {

				parseLineTakeNextLine(sCurLine, patternData0, bufFabricFile, data0StrBuffer);
				parseLineTakeSplit(sCurLine, patternData1, ":", 1, data1StrBuffer);
				parseLineTakeSplit(sCurLine, patternData2, ":", 1, data2StrBuffer);
				parseLineTakeSplit(sCurLine, patternData3, ":", 1, data3StrBuffer);
				parseLineTakeNotContaining(sCurLine, patternData4, patternData0, data4StrBuffer);
			}

			// If a match occured, there is " / " even if the string was empty
			if (data0StrBuffer.length() > 3)
				mEvent.setData0(data0StrBuffer.substring(0, data0StrBuffer.length()-3));

			if (data1StrBuffer.length() > 3)
				mEvent.setData1(data1StrBuffer.substring(0, data1StrBuffer.length()-3));

			if (data2StrBuffer.length() > 3)
				mEvent.setData2(data2StrBuffer.substring(0, data2StrBuffer.length()-3));

			if (data3StrBuffer.length() > 3)
				mEvent.setData3(data3StrBuffer.substring(0, data3StrBuffer.length()-3));

			if (data4StrBuffer.length() > 3)
				mEvent.setData4(data4StrBuffer.substring(0, data4StrBuffer.length()-3));

			bufFabricFile.close();
		}
		catch (Exception e) {
			System.err.println( "Fab_recov : " + e);
			e.printStackTrace();
		}
	}

	/**
	 * Searches for a filename pattern inside a folder.
	 *
	 * @return sFileResult	the exact filename of the first file matching the pattern; empty otherwise.
	 */
	private String fileGrepSearch(String aPattern, String aFolder) {
		Pattern patternFile = java.util.regex.Pattern.compile(aPattern);

		File searchFolder = new File(aFolder);
		File[] files = searchFolder.listFiles();
		String sFileResult = "";
		if (files!=null) {
			for (File f: files) {
				Matcher matcherFile = patternFile.matcher(f.getName());
				if (matcherFile.find()) {
					sFileResult = aFolder + "/" + f.getName();
					break;
				}
			}
		}
		return sFileResult;
	}
}
