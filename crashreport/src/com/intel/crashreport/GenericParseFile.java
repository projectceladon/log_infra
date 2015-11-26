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

package com.intel.crashreport;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class GenericParseFile {

	private Map<String,String> parsedValue = new HashMap<String,String>();

	private File genFile;


	public GenericParseFile(String sPath, String sExt) throws FileNotFoundException {
		genFile = openGenFile(sPath,sExt);
		fillGenFile(genFile);
	}

	private File openGenFile(String sPath, String sFileExt) throws FileNotFoundException {
		//TO DO open a file depending on a schema
		File dir = new File(sPath);
		if(dir.exists() && dir.isDirectory()) {
			File[] dirFiles = dir.listFiles();
			if(dirFiles != null) {
				for(File file:dirFiles){
					if(file.getName().endsWith(sFileExt))
						return file;
				}
			}
		}
		throw new FileNotFoundException();
		//return new File(path + "/TO DO");
	}

	private void fillGenFile(File genFile) throws FileNotFoundException {
		Scanner scan = null;
		try {
			scan = new Scanner(genFile);
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

		if (field.length() != 0)  {
			try {
				String splitField[] = field.split("\\=", MAX_FIELDS);
				if (splitField.length == MAX_FIELDS) {
					name = splitField[0];
					value = splitField[1];
					parsedValue.put(name, value);
				}
			} catch (NullPointerException e) {
				Log.w("GenFile: field format not recognised : " + field);
			}
		}
	}

	public String getValueByName(String aName){
		if (parsedValue.containsKey(aName)){
			return parsedValue.get(aName);
		}else{
			return "";
		}
	}
}
