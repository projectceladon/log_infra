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

import java.io.BufferedReader;
import java.io.Reader;
import java.io.IOException;



public class BufferedReaderClean extends BufferedReader {

	private Reader mReader = null;
	public BufferedReaderClean(Reader aReader){
		super(aReader);
		mReader = aReader;
	}

	@Override
	public void close(){
		try {
			super.close();
		} catch (IOException e) {
			//catch exception to simplify MainParser Code
			APLog.e("IOException : " + e.getMessage());
		}
		finally {
			if (mReader != null) {
				try {
					mReader.close();
				} catch (IOException e) {
					APLog.e("IOException : " + e.getMessage());
				}
			}
		}
	}
}
