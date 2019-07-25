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

public class PostProcessBuilder implements ParserBuilder {

	private static final String M_POST_PARSER_DIR = "postparserconfig";

	AssetManager mAssetManager;
	List<String> mParserFileList = new ArrayList<String>();

	public PostProcessBuilder(AssetManager aAssetManager) {
		mAssetManager = aAssetManager;
		try {
			String mList[] = mAssetManager.list(M_POST_PARSER_DIR);
			if (mList != null)
				for (String config : mList)
					mParserFileList.add(config);
		} catch (IOException e) {
			APLog.w("IOException " + e.getMessage());
		}
	}

	public List<EventParser> getParsers() {
		EventParser result = null;
		InputStreamReader is = null;
		List<EventParser> resultList = new ArrayList<EventParser>();

		for (int i = 0; i < mParserFileList.size(); i++) {
			try {
				is = new InputStreamReader(mAssetManager.open(M_POST_PARSER_DIR
						+ File.separator + mParserFileList.get(i)));
				APLog.i("reading post : " + mParserFileList.get(i));
				readOneStream(is, resultList);
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
		}
		return resultList;
	}

	private void readOneStream(InputStreamReader is,
			List<EventParser> listToFill) {
		JsonReader reader = new JsonReader(is);
		try {
			readJsonObjectList(reader, listToFill);
			reader.close();
		} catch (IOException e) {
			return;
		}
	}

	private void readJsonObjectList(JsonReader curReader,
			List<EventParser> listToFill) {

		APLog.d("readJsonObject- starting");
		try {
			if (curReader.peek() == JsonToken.BEGIN_ARRAY)
				curReader.beginArray();
			if (curReader.peek() == JsonToken.BEGIN_OBJECT) {
				PostProcessParser parserToFill = new PostProcessParser();
				readJsonObject(curReader, parserToFill);
				listToFill.add(parserToFill);
				// recursion
				readJsonObjectList(curReader, listToFill);
			}
			if (curReader.peek() == JsonToken.END_ARRAY) {
				curReader.endArray();
				return;
			}
			if (curReader.peek() == JsonToken.END_DOCUMENT) {
				return;
			}
			// should not reach that point
			APLog.e("Error : unexpected token : " + curReader.peek().toString());

		} catch (IOException e) {
			APLog.e("IOException " + e.getMessage());
		}
	}

	private void readJsonObject(JsonReader curReader,
			PostProcessParser parserToFill) {
		APLog.d("readJsonObject- starting");
		try {
			if (curReader.peek() != JsonToken.BEGIN_OBJECT) {
				APLog.e("Error : expecting BEGIN_OBJECT token");
				return;
			}
			curReader.beginObject();
			while (curReader.hasNext()) {
				if (curReader.peek() == JsonToken.NAME) {
					fillProperty(curReader, parserToFill);
				} else if (curReader.peek() == JsonToken.STRING) {
					// unexpected token => skip
					APLog.v("STRING SKIPPED");
					curReader.skipValue();
				} else if (curReader.peek() == JsonToken.BEGIN_ARRAY) {
					readRulesArray(curReader, parserToFill);
				} else if (curReader.peek() == JsonToken.BEGIN_OBJECT) {
					readJsonObject(curReader, parserToFill);
				} else {
					curReader.skipValue();
				}
				// needed to manage end of array properly
				if (curReader.peek() == JsonToken.END_ARRAY) {
					curReader.endArray();
				}
			}
			curReader.endObject();
		} catch (IOException e) {
			APLog.e("IOException " + e.getMessage());
		}
	}

	private void readRulesArray(JsonReader curReader,
			PostProcessParser parserToFill) {
		APLog.d("readRulesArray- starting");
		try {
			if (curReader.peek() != JsonToken.BEGIN_ARRAY) {
				curReader.skipValue();
				APLog.e("Error : expecting BEGIN_ARRAY token");
				return;
			}
			curReader.beginArray();
			while (curReader.hasNext()) {
				if (curReader.peek() == JsonToken.NAME) {
					fillProperty(curReader, parserToFill);
				} else if (curReader.peek() == JsonToken.STRING) {
					// unexpected token => skip
					APLog.v("STRING SKIPPED");
					curReader.skipValue();
				} else if (curReader.peek() == JsonToken.BEGIN_ARRAY) {
					curReader.beginArray();
				} else if (curReader.peek() == JsonToken.BEGIN_OBJECT) {
					PostProcessRule aRule = new PostProcessRule();
					readRuleObject(curReader, aRule);
					parserToFill.addRule(aRule);
				} else {
					curReader.skipValue();
				}
			}
			curReader.endArray();
		} catch (IOException e) {
			APLog.e("IOException " + e.getMessage());
		}
	}

	private void readRuleObject(JsonReader curReader, PostProcessRule ruleToFill)
			throws IOException {
		APLog.d("readRuleObject- starting");

		if (curReader.peek() != JsonToken.BEGIN_OBJECT) {
			curReader.skipValue();
			APLog.e("Error : expecting BEGIN_OBJECT token");
			return;
		}
		curReader.beginObject();
		while (curReader.hasNext()) {
			if (curReader.peek() == JsonToken.NAME) {
				fillPropertyForRule(curReader, ruleToFill);
			} else if (curReader.peek() == JsonToken.STRING) {
				// unexpected token => skip
				APLog.v("STRING SKIPPED");
				curReader.skipValue();
			} else if (curReader.peek() == JsonToken.BEGIN_ARRAY) {
				// unexpected token => skip
				curReader.skipValue();
			} else if (curReader.peek() == JsonToken.BEGIN_OBJECT) {
				APLog.w(" no new object expected");
				curReader.skipValue();
			} else {
				curReader.skipValue();
			}
		}
		curReader.endObject();
	}

	private void fillPropertyForRule(JsonReader curReader,
			PostProcessRule ruleToFill) throws IOException {
		if (curReader.peek() == JsonToken.NAME) {
			String sName = curReader.nextName();
			if (curReader.peek() == JsonToken.STRING) {
				try {
					fillRuleByMethod(curReader, ruleToFill,
							PostProcessRule.class.getMethod("set" + sName,
									String.class));
				} catch (NoSuchMethodException e) {
					APLog.e("NoSuchMethodException for : " + sName);
				}
			} else if (curReader.peek() == JsonToken.BEGIN_ARRAY) {
				try {
					fillArrayRuleByMethod(curReader, ruleToFill,
							StandardRule.class.getMethod("add" + sName,
									String.class));
				} catch (NoSuchMethodException e) {
					APLog.e("NoSuchMethodException for : " + sName);
				}
			}
		}
	}

	private void fillProperty(JsonReader curReader,
			PostProcessParser parserToFill) throws IOException {
		if (curReader.peek() == JsonToken.NAME) {
			String sName = curReader.nextName();
			if (curReader.peek() == JsonToken.STRING) {
				try {
					fillParserByMethod(curReader, parserToFill,
							PostProcessParser.class.getMethod("set" + sName,
									String.class));
				} catch (NoSuchMethodException e) {
					APLog.e("NoSuchMethodException for : " + sName);
				}
			}
		} else {
			APLog.e("BAD TOKEN");
		}
	}

	private void fillRuleByMethod(JsonReader curReader,
			PostProcessRule ruleToFill, Method aSetterMethod)
			throws IOException {
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

	private void fillParserByMethod(JsonReader curReader,
			PostProcessParser parserToFill, Method aSetterMethod)
			throws IOException {
		if (curReader.peek() == JsonToken.STRING) {
			String sValue = curReader.nextString();
			try {
				aSetterMethod.invoke(parserToFill, sValue);
			} catch (IllegalAccessException e) {
				APLog.e("IllegalAccessException");
			} catch (IllegalArgumentException e) {
				APLog.e("IllegalArgumentException");
			} catch (InvocationTargetException e) {
				APLog.e("InvocationTargetException");
			}
		}
	}

	private void fillArrayRuleByMethod(JsonReader curReader,
			PostProcessRule ruleToFill, Method aArrayMethod) throws IOException {
		try {
			if (curReader.peek() != JsonToken.BEGIN_ARRAY) {
				curReader.skipValue();
				APLog.e("Error : expecting BEGIN_ARRAY token");
			}
			curReader.beginArray();
			while (curReader.hasNext()) {
				if (curReader.peek() == JsonToken.NAME) {
					// unexpected token => skip
					APLog.e("Name SKIPPED");
					curReader.skipValue();

				} else if (curReader.peek() == JsonToken.STRING) {
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
				} else {
					curReader.skipValue();
				}
			}
			curReader.endArray();
		} catch (IOException e) {
			APLog.e("IOException " + e.getMessage());
		}
	}
}
