/* crashinfo
 *
 * Copyright (C) Intel 2012
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
 * Author: Nicolas Benoit <nicolasx.benoit@intel.com>
 */

package com.intel.commands.crashinfo.subcommand;

import java.io.File;

import com.intel.commands.crashinfo.ISubCommand;

public class Clean implements ISubCommand {
	String[] myArgs;
	@Override
	public int execute() {
		System.out.println("aplog cleaning");
		File logFolder = new File("/data/logs");
		if (logFolder.isDirectory()){
			String[] sLogFiles = logFolder.list();
			if (sLogFiles!= null){
				for (int i = 0; i < sLogFiles.length; i++) {
					if (sLogFiles[i].contains("aplog.")){
						File f = new File(logFolder.getAbsolutePath() + File.separator +sLogFiles[i] );
						if (f.isFile()){
							f.delete();
							System.out.println("XXX APLOG " + i + " : cleaned XXX");
						}else{
							System.out.println("Path does not exist");
						}
					}else{
						System.out.println( "File " + i + " : skipped");
					}
				}
			}
		}else{
			System.out.println("Can't find log folder");
		}
		return 0;
	}

	@Override
	public void setArgs(String[] subArgs) {
		myArgs = subArgs;
	}

	@Override
	public boolean checkArgs() {
		boolean result = true;
		if (myArgs == null){
			//correct, nothing to do
		}else if (myArgs.length == 0){
			//correct, nothing to do
		}else{
			//Incorrect, no arguments allowed
			result = false;
		}
		return result;
	}

}
