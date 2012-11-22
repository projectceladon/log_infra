/* crashinfo - getbz gives content of bz database
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
 * Author: Jacques Imougar
 */

package com.intel.commands.crashinfo.subcommand;
import com.intel.commands.crashinfo.DBManager;
import com.intel.commands.crashinfo.ISubCommand;
import com.intel.commands.crashinfo.option.OptionData;
import com.intel.commands.crashinfo.option.Options;


public class GetBZ implements ISubCommand {
	Options myOptions;
	String[] myArgs;

	public GetBZ(){

	}

	@Override
	public int execute() {
		OptionData mainOp = myOptions.getMainOption();
		if (mainOp == null){
			return execGetBZ();
		}else if (mainOp.getKey().equals(Options.HELP_COMMAND)){
			myOptions.generateHelp();
			return 0;
		}else{
			System.out.println("error : unknown op - " + mainOp.getKey());
			return -1;
		}
	}

	private int execGetBZ(){
		try {
			DBManager aDB = new DBManager();
			aDB.getBz();
		}catch (Exception e) {
			System.out.println("Exception : "+e.toString());
			return -1;
		}
		return 0;
	}

	@Override
	public void setArgs(String[] subArgs) {
		myArgs = subArgs;
		myOptions = new Options(subArgs, "GetBZ gives content of BZ database");
	}

	@Override
	public boolean checkArgs() {
		return myOptions.check();
	}

}
