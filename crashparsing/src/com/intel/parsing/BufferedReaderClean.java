/* Phone Doctor - parsing
 *
 * Copyright (C) Intel 2014
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
 * Author: Nicolas BENOIT <nicolasx.benoit@intel.com>
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
			System.err.println("IOException : " + e.getMessage());
		}
		finally {
			if (mReader != null) {
				try {
					mReader.close();
				} catch (IOException e) {
					System.err.println("IOException : " + e.getMessage());
				}
			}
		}
	}
}
