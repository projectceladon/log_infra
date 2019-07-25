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

package com.intel.crashtoolserver.bean;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipFile;

/**
 * Log File class that allow to manipulate a crashtool logfile
 * @author glivonx
 *
 */
public class CtZipFile extends ZipFile {


	/** Path Name */
	private String pathname;

	/**
	 * Construct a logfile from it path
	 * @param pathname
	 */
	public CtZipFile(String pathname) throws IOException {
		super(pathname);
		this.pathname = pathname;
	}

	/**
	 * Gives the logfile's parent path
	 * @return
	 */
	public String getLogFileParentPath() {
		File file = new File(pathname);
		return file.getParent();
	}

	/**
	 * @return the pathname
	 */
	public String getPathname() {
		return pathname;
	}
}
