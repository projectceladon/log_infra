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
