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
import java.util.ArrayList;
import java.util.List;

import com.intel.phonedoctor.utils.FileOps;
import com.intel.crashreport.core.ParsableEvent;

public class PostProcessRule {

	private static final String TAG_INPUT_FILE = "file";
	private static final String TAG_INPUT_FOLDER = "folder";
	private static long MB = 1024 * 1024;

	private String mInputType;
	private String mInputValue;
	private String mOutputValue;
	private String mOutputSizeLimitMB;
	List<String> mPatternOptionList = new ArrayList<String>();

	public String getInputType() {
		return mInputType;
	}

	public void setInputType(String mInputType) {
		this.mInputType = mInputType;
	}

	// proxy function for JSONbuilder
	public void setinput_type(String mInputType) {
		this.setInputType(mInputType);
	}

	public String getInputValue() {
		return mInputValue;
	}

	public void setInputValue(String mInputValue) {
		this.mInputValue = mInputValue;
	}

	// proxy function for JSONbuilder
	public void setinput_value(String mInputValue) {
		this.setInputValue(mInputValue);
	}

	public String getOutputValue() {
		return mOutputValue;
	}

	public void setOutputValue(String mOutputValue) {
		this.mOutputValue = mOutputValue;
	}

	// proxy function for JSONbuilder
	public void setoutput_value(String mOutputValue) {
		this.setOutputValue(mOutputValue);
	}

	public String getmOutputSizeLimitMB() {
		return mOutputSizeLimitMB;
	}

	public void setmOutputSizeLimitMB(String mOutputSizeLimitMB) {
		this.mOutputSizeLimitMB = mOutputSizeLimitMB;
	}

	// proxy function for JSONbuilder
	public void setoutput_size_limit_MB(String mOutputSizeLimitMB) {
		setmOutputSizeLimitMB(mOutputSizeLimitMB);
	}

	public void addpattern_options(String aOption) {
		mPatternOptionList.add(aOption);
	}

	public void analyzeEvent(ParsableEvent aEvent) {
		int iLimitMB;
		String sDestPath;

		// data check
		if ((getInputType() == null) || (getInputValue() == null)
				|| (getOutputValue() == null)) {
			return;
		}
		sDestPath = aEvent.getCrashDir() + File.separator + getOutputValue();
		if (new File(sDestPath).exists()) {
			// don't override existing file
			return;
		}
		if (getInputType().equals(TAG_INPUT_FILE)) {
			// check file exists
			File source = new File(getInputValue());
			if (source.exists() && source.isFile()) {
				File dest = new File(sDestPath);
				try {
					FileOps.copy(source, dest);
				} catch (IOException e) {
					APLog.e("can't retrieve file to copy: " + getInputValue(),
							e);
				}
			}
		} else if (getInputType().equals(TAG_INPUT_FOLDER)) {
			// check folder exists
			File source = new File(getInputValue());
			if (source.exists() && source.isDirectory()) {
				try {
					FileOps.compressFolderAndMove(source.getAbsolutePath(),
							sDestPath);
				} catch (IOException e) {
					APLog.e("Error while compressing: " + e.getMessage());
				}
			}
		}// other INPUT TYPE could be added here
		else {
			APLog.e("Unmanaged InputType : " + getInputType());
			return;
		}
		// check for data output size
		if (getmOutputSizeLimitMB() != null) {
			try {
				iLimitMB = Integer.parseInt(getmOutputSizeLimitMB());
			} catch (NumberFormatException e) {
				iLimitMB = -1;
			}
			if (iLimitMB < 0) {
				return;
			}
			File destToCheck = new File(sDestPath);
			if (destToCheck.length() > (iLimitMB * MB)) {
				APLog.w("too big data - deleting: " + sDestPath);
				destToCheck.delete();
			}
		}
	}

}
