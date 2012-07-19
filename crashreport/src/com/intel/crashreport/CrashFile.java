/* Crash Report (CLOTA)
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
 * Author: Jeremy Rocher <jeremyx.rocher@intel.com>
 */

package com.intel.crashreport;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class CrashFile {

	private String eventId = "";
	private String eventName = "";
	private String type = "";
	private String data0 = "";
	private String data1 = "";
	private String data2 = "";
	private String data3 = "";
	private String data4 = "";
	private String data5 = "";
	private String date = "";
	private String buildId = "";
	private String sn = "";
	private String imei = "";
	private String uptime = "";
	private String modem = "";
	private String board = "";

	private File crashFile;

	public CrashFile(String path) throws FileNotFoundException {
		//TODO watch if SDcard is mounted : special intent
		crashFile = openCrashFile(path);
		fillCrashFile(crashFile);
	}

	private File openCrashFile(String path) {
		return new File(path + "/crashfile");
	}

	private void fillCrashFile(File crashFile) throws FileNotFoundException {
		Scanner scan = new Scanner(crashFile);
		String field;
		while(scan.hasNext()) {
			field = scan.nextLine();
			if (field != null)
				fillField(field);
		}
		scan.close();
	}

	private void fillField(String field) {
		final int MAX_FIELDS = 2;
		String name;
		String value;

		if ((field.length() != 0) && !field.equals("_END")) {
			try {
				String splitField[] = field.split("\\=", MAX_FIELDS);
				if (splitField.length == MAX_FIELDS) {
					name = splitField[0];
					value = splitField[1];

					if (name.equals("EVENT"))
						eventName = value;
					else if (name.equals("ID"))
						eventId = value;
					else if (name.equals("SN"))
						sn = value;
					else if (name.equals("IMEI"))
						imei = value;
					else if (name.equals("DATE"))
						date = value;
					else if (name.equals("UPTIME"))
						uptime = value;
					else if (name.equals("BUILD"))
						buildId = value;
					else if (name.equals("MODEM"))
						modem = value;
					else if (name.equals("BOARD"))
						board = value;
					else if (name.equals("TYPE"))
						type = value;
					else if (name.equals("DATA0"))
						data0 = value;
					else if (name.equals("DATA1"))
						data1 = value;
					else if (name.equals("DATA2"))
						data2 = value;
					else if (name.equals("DATA3"))
						data3 = value;
					else if (name.equals("DATA4"))
						data4 = value;
					else if (name.equals("DATA5"))
						data5 = value;
					else
						Log.w("CrashFile: field name\"" + name + "\" not recognised");
				}

			} catch (NullPointerException e) {
				Log.w("CrashFile: field format not recognised : " + field);
			}
		}
	}

	public String getEventId() {
		return eventId;
	}

	public void setEventId(String eventId) {
		this.eventId = eventId;
	}

	public String getEventName() {
		return eventName;
	}

	public void setEventName(String eventName) {
		this.eventName = eventName;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getData0() {
		return data0;
	}

	public void setData0(String data) {
		this.data0 = data;
	}

	public String getData1() {
		return data1;
	}

	public void setData1(String data) {
		this.data1 = data;
	}

	public String getData2() {
		return data2;
	}

	public void setData2(String data) {
		this.data2 = data;
	}

	public String getData3() {
		return data3;
	}

	public void setData3(String data) {
		this.data3 = data;
	}

	public String getData4() {
		return data4;
	}

	public void setData4(String data) {
		this.data4 = data;
	}

	public String getData5() {
		return data5;
	}

	public void setData5(String data) {
		this.data5 = data;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getBuildId() {
		return buildId;
	}

	public void setBuildId(String buildId) {
		this.buildId = buildId;
	}

	public String getSn() {
		return sn;
	}

	public void setSn(String sn) {
		this.sn = sn;
	}

	public String getUptime() {
		return uptime;
	}

	public void setUptime(String uptime) {
		this.uptime = uptime;
	}

	public String getModem() {
		return modem;
	}

	public void setModem(String modem) {
		this.modem = modem;
	}

	public String getBoard() {
		return board;
	}

	public void setBoard(String board) {
		this.board = board;
	}

	public File getCrashFile() {
		return crashFile;
	}

	public void setCrashFile(File crashFile) {
		this.crashFile = crashFile;
	}

	public String getImei() {
		return imei;
	}

	public void setImei(String imei) {
		this.imei = imei;
	}
}
