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

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import android.content.res.AssetManager;
import android.util.JsonReader;
import android.util.JsonToken;

public class JsonBuilder implements ParserBuilder {

	private static final String M_PARSER_DIR = "parserconfig";

	AssetManager mAssetManager;
	List<String> mParserFileList = new ArrayList<String>();
	int iParserIndex = 0;

	public JsonBuilder(AssetManager aAssetManager) {
		mAssetManager = aAssetManager;
		try {
			String mList[] = mAssetManager.list(M_PARSER_DIR);
			if (mList != null)
				for (String config : mList)
					mParserFileList.add(config);
		} catch (IOException e) {
			APLog.w("IOException " + e.getMessage());
		}
	}

	public EventParser getNextParser() {
		EventParser result = null;
		InputStreamReader is = null;
		try{
			is = new InputStreamReader(mAssetManager.open(
					M_PARSER_DIR + File.separator + mParserFileList.get(iParserIndex)));
			APLog.i("reading : " +  mParserFileList.get(iParserIndex));
			result = readOneStream(is);
		} catch (IOException e) {
			APLog.w("IOException " + e.getMessage());
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					APLog.e("IOException : " + e.getMessage());
				}
			}
		}
		iParserIndex++;
		return result;
	}

	public boolean hasNextParser() {
		return (iParserIndex < mParserFileList.size());
	}

	private StandardParser readOneStream(InputStreamReader is){
		JsonReader reader = new JsonReader(is);
		try {
			StandardParser parserToFill = new StandardParser();
			readJsonObject(reader, parserToFill);
			reader.close();
			return parserToFill;
		} catch (IOException e) {
			return null;
		}
	}

	private void readJsonObject(JsonReader curReader, StandardParser parserToFill){
		APLog.d("readJsonObject- starting");
		try {
			if (curReader.peek() != JsonToken.BEGIN_OBJECT){
				APLog.e("Error : expecting BEGIN_OBJECT token");
				return;
			}
			curReader.beginObject();
			while (curReader.hasNext()) {
				if (curReader.peek() == JsonToken.NAME){
					fillProperty(curReader,parserToFill);
				}else if (curReader.peek() == JsonToken.STRING) {
					//unexpected token => skip
					APLog.v("STRING SKIPPED");
					curReader.skipValue();
				}else if (curReader.peek() == JsonToken.BEGIN_ARRAY) {
					readRulesArray(curReader,parserToFill);
				} else if (curReader.peek() == JsonToken.BEGIN_OBJECT) {
					readJsonObject(curReader, parserToFill);
				}
				else {
					curReader.skipValue();
				}
				//needed to manage end of array properly
				if (curReader.peek() == JsonToken.END_ARRAY) {
					curReader.endArray();
				}
			}
			curReader.endObject();
		} catch (IOException e) {
			APLog.e("IOException " + e.getMessage());
		}
	}

	private void readRulesArray(JsonReader curReader, StandardParser parserToFill){
		APLog.d("readRulesArray- starting");
		try {
			if (curReader.peek() != JsonToken.BEGIN_ARRAY){
				curReader.skipValue();
				APLog.e("Error : expecting BEGIN_ARRAY token");
				return;
			}
			curReader.beginArray();
			while (curReader.hasNext()) {
				if (curReader.peek() == JsonToken.NAME){
					fillProperty(curReader,parserToFill);
				}else if (curReader.peek() == JsonToken.STRING) {
					//unexpected token => skip
					APLog.v("STRING SKIPPED");
					curReader.skipValue();
				}else if (curReader.peek() == JsonToken.BEGIN_ARRAY) {
					curReader.beginArray();
				} else if (curReader.peek() == JsonToken.BEGIN_OBJECT) {
					StandardRule aRule = new StandardRule();
					readRuleObject(curReader, aRule);
					parserToFill.addRulle(aRule);
				}
				else {
					curReader.skipValue();
				}
			}
			curReader.endArray();
		} catch (IOException e) {
			APLog.e("IOException " + e.getMessage());
		}
	}

	private void readRuleObject(JsonReader curReader, StandardRule ruleToFill) throws IOException{
		APLog.d("readRuleObject- starting");

		if (curReader.peek() != JsonToken.BEGIN_OBJECT){
			curReader.skipValue();
			APLog.e("Error : expecting BEGIN_OBJECT token");
			return;
		}
		curReader.beginObject();
		while (curReader.hasNext()) {
			if (curReader.peek() == JsonToken.NAME){
				fillPropertyForRule(curReader,ruleToFill);
			}else if (curReader.peek() == JsonToken.STRING) {
				//unexpected token => skip
				APLog.v("STRING SKIPPED");
				curReader.skipValue();
			}else if (curReader.peek() == JsonToken.BEGIN_ARRAY) {
				//unexpected token => skip
				curReader.skipValue();
			} else if (curReader.peek() == JsonToken.BEGIN_OBJECT) {
				APLog.w(" no new object expected");
				curReader.skipValue();
			}
			else {
				curReader.skipValue();
			}
		}
		curReader.endObject();
	}

	private void fillPropertyForRule(JsonReader curReader, StandardRule ruleToFill) throws IOException {
		if (curReader.peek() == JsonToken.NAME){
			String sName =curReader.nextName();
			if (curReader.peek() == JsonToken.STRING) {
				try {
					fillRuleByMethod(curReader, ruleToFill,
							StandardRule.class.getMethod("set" + sName, String.class));
				} catch (NoSuchMethodException e) {
					APLog.e("NoSuchMethodException for : " + sName);
				}
			} else if (curReader.peek() == JsonToken.BEGIN_ARRAY) {
				try {
					fillArrayRuleByMethod(curReader, ruleToFill,
							StandardRule.class.getMethod("add" + sName, String.class));
				} catch (NoSuchMethodException e) {
					APLog.e("NoSuchMethodException for : " + sName);
				}
			}
		}
	}

	private void fillProperty(JsonReader curReader, StandardParser parserToFill) throws IOException {
		if (curReader.peek() == JsonToken.NAME){
			String sName =curReader.nextName();
			if (sName.equals("event_name")){
				fillEventName(curReader, parserToFill);
			} else if (sName.equals("event_type")){
				fillEventType(curReader, parserToFill);
			} else if (sName.equals("description")){
				//Ok but not stored inside parser object
				curReader.skipValue();
				APLog.v(sName + " skipped");
			} else if (sName.equals("rules")){
				//Ok but not a property
				APLog.v(sName + " found");
			}
			else {
				APLog.w("unmanaged property : " + sName);
			}
		} else {
			APLog.e("BAD TOKEN");
		}
	}

	private void fillEventName(JsonReader curReader, StandardParser parserToFill) throws IOException {
		if (curReader.peek() == JsonToken.STRING) {
			String sValue = curReader.nextString();
			parserToFill.setEventName(sValue);
		} else {
			APLog.v("empty");
		}
	}

	private void fillEventType(JsonReader curReader, StandardParser parserToFill) throws IOException {
		if (curReader.peek() == JsonToken.STRING) {
			String sValue = curReader.nextString();
			parserToFill.setEventType(sValue);
		} else {
			APLog.v("empty");
		}
	}

	private void fillRuleByMethod(JsonReader curReader, StandardRule ruleToFill,Method aSetterMethod) throws IOException {
		if (curReader.peek() == JsonToken.STRING) {
			String sValue = curReader.nextString();
			try {
				aSetterMethod.invoke(ruleToFill, sValue);
			} catch (IllegalAccessException e) {
				APLog.e("IllegalAccessException");
			} catch (IllegalArgumentException e) {
				APLog.e("IllegalArgumentException");
			} catch (InvocationTargetException e) {
				APLog.e("InvocationTargetException");
			}
		}
	}

	private void fillArrayRuleByMethod(JsonReader curReader, StandardRule ruleToFill,Method aArrayMethod) throws IOException {
		try {
			if (curReader.peek() != JsonToken.BEGIN_ARRAY){
				curReader.skipValue();
				APLog.e("Error : expecting BEGIN_ARRAY token");
			}
			curReader.beginArray();
			while (curReader.hasNext()) {
				if (curReader.peek() == JsonToken.NAME){
					//unexpected token => skip
					APLog.e("Name SKIPPED");
					curReader.skipValue();

				}else if (curReader.peek() == JsonToken.STRING) {
					String sValue = curReader.nextString();
					try {
						aArrayMethod.invoke(ruleToFill, sValue);
					} catch (IllegalAccessException e) {
						APLog.e("IllegalAccessException");
					} catch (IllegalArgumentException e) {
						APLog.e("IllegalArgumentException");
					} catch (InvocationTargetException e) {
						APLog.e("InvocationTargetException");
					}
				}
				else {
					curReader.skipValue();
				}
			}
			curReader.endArray();
		} catch (IOException e) {
			APLog.e("IOException " + e.getMessage());
		}
	}
}
