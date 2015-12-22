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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Scanner;

import com.intel.crashreport.CustomizableEventData;
import com.intel.crashreport.GeneralEventGenerator;
import com.intel.crashreport.Log;
import com.intel.parsing.*;

public class CrashFile {

	private static final String EVENT_TYPE_PARSING = "PARSING";

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
	private int dataReady = 1;
	private String operator="";
	private String modemVersionUsed="";

	private File crashFile;

	//Internal Value for crashfile generation
	boolean bMissingData0;
	boolean bMissingData1;
	boolean bMissingData2;

	public CrashFile(String path) throws FileNotFoundException {
		this(path,true);
	}

	public CrashFile(String path, boolean toParse) throws FileNotFoundException{
		//TODO watch if SDcard is mounted : special intent
		crashFile = openCrashFile(path);
		bMissingData0 = true;
		bMissingData1 = true;
		bMissingData2 = true;
		fillCrashFile(crashFile);
		if(toParse) {
			//if data are not present, we try to generate it
			if(type.isEmpty()) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				fillCrashFile(crashFile);
			}
			if (bMissingData0 && bMissingData1 && bMissingData2)
			{

				Log.i(type + ": Missing Data, try to regenerate crashfile in " + path);
				if (!type.isEmpty()){
					MainParser aParser = new MainParser(path, type, eventId, uptime,buildId, board, date, imei,dataReady,operator);
					if (aParser.execParsing() == 0){
						crashFile = openCrashFile(path);
						fillCrashFile(crashFile);
					}else{
						Log.w("Error while parsing crashfile");
						//generating an error event to track this parsing error
						CustomizableEventData mEvent = EventGenerator.INSTANCE.getEmptyErrorEvent();
						mEvent.setType(EVENT_TYPE_PARSING);
						mEvent.setData0("PARSE_CRASH_ERROR");
						mEvent.setData1(type);
						mEvent.setData2(eventId);
						GeneralEventGenerator.INSTANCE.generateEvent(mEvent);
					}
				}
			}
		}
	}

	public void writeCrashFile(String reason) throws FileNotFoundException,IOException{
		FileOutputStream f = new FileOutputStream(crashFile);
		try {
			OutputStreamWriter record = new OutputStreamWriter(f);
			record.write("EVENT="+eventName+"\n");
			record.write("ID="+eventId+"\n");
			record.write("SN="+sn+"\n");
			record.write("IMEI="+imei+"\n");
			record.write("DATE="+date+"\n");
			record.write("UPTIME="+uptime+"\n");
			record.write("BUILD="+buildId+"\n");
			record.write("MODEM="+modem+"\n");
			record.write("BOARD="+board+"\n");
			record.write("TYPE="+type+"_"+reason+"\n");
			record.write("DATA_READY="+ dataReady+"\n");
			record.write("OPERATOR="+ operator+"\n");
			record.write("DATA0="+data0+"\n");
			record.write("DATA1="+data1+"\n");
			record.write("DATA2="+data2+"\n");
			record.write("DATA3="+data3+"\n");
			record.write("DATA4="+data4+"\n");
			record.write("DATA5="+data5+"\n");
			record.write("MODEMVERSIONUSED="+modemVersionUsed+"\n");
			record.write("_END\n");
			record.close();
		}
		finally {
			f.close();
		}
	}

	private File openCrashFile(String path) {
		return new File(path + "/crashfile");
	}

	private void fillCrashFile(File crashFile) throws FileNotFoundException {
		Scanner scan = null;
		try {
			scan = new Scanner(crashFile);
			String field;
			while(scan.hasNext()) {
				field = scan.nextLine();
				if (field != null){
					fillField(field);
				}
			}
		} catch (IllegalStateException e) {
			Log.w("IllegalStateException : considered as file not found exception");
			throw new FileNotFoundException("Illegal state");
		} finally {
			if (scan != null) {
				scan.close();
			}
		}
	}

	private void fillField(String field) {
		final int MAX_FIELDS = 2;
		String name;
		String value;

		if (!field.isEmpty() && !field.equals("_END")) {
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
				else if (name.equals("OPERATOR"))
					operator = value;
				else if (name.equals("DATA_READY")){
					try{
						dataReady = Integer.parseInt(value);
					}catch (NumberFormatException e){
						//default value is ready => 1
						dataReady=1;
					}
				}else if (name.equals("DATA0")){
					bMissingData0 = false;
					data0 = value;
				}
				else if (name.equals("DATA1")){
					bMissingData1 = false;
					data1 = value;
				}
				else if (name.equals("DATA2")){
					bMissingData2 = false;
					data2 = value;
				}
				else if (name.equals("DATA3"))
					data3 = value;
				else if (name.equals("DATA4"))
					data4 = value;
				else if (name.equals("DATA5"))
					data5 = value;
				else if (name.equals("MODEMVERSIONUSED"))
					modemVersionUsed = value;
				else if (name.equals("PARSER")){
					//ignoring this value but this field is expected
				} else
					Log.w("CrashFile: field name\"" + name + "\" not recognised");
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

	public int getDataReady() {
		return dataReady;
	}

	public void setDataReady(int dataReady) {
		this.dataReady = dataReady;
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public String getModemVersionUsed() {
		return modemVersionUsed;
	}

	public void setModemVersionUsed(String modemVersionUsed) {
		this.modemVersionUsed = modemVersionUsed;
	}
}
