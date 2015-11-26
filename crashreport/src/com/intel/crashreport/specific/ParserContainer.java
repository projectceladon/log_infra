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

import android.content.Context;
import android.content.res.AssetManager;

import com.intel.crashreport.Log;
import com.intel.parsing.ParserDirector;

public enum ParserContainer {
	INSTANCE;

	private ParserDirector mDirector = null;

	public void initDirector(Context aContext){
		Log.i("CrashReport: init of parser container");
		mDirector = new ParserDirector();
		mDirector.initParserWithManager(aContext.getAssets());
		int iParserCount = mDirector.getParserCount();
		Log.i("CrashReport: " + iParserCount + " parser(s) found" );
	}

	public boolean parseEvent(Event aEvent){
		if (mDirector != null){
			return mDirector.parseEvent(aEvent.getParsableEvent());
		} else {
			Log.e("CrashReport: mDirector is null");
			return false;
		}
	}
}
