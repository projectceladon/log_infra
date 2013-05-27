/* Phone Doctor (CLOTA)
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
 * Author: Jeremy Rocher <jeremyx.rocher@intel.com>
 */

package com.intel.crashreport.specific;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import com.intel.crashreport.GeneralBuild;
import com.intel.crashreport.Log;

public class Build extends GeneralBuild{

	private static final String PATH_MODEMID = "/logs/modemid.txt";
	public Build(String buildId, String fingerPrint, String kernelVersion, String buildUserHostname, String modemVersion,
			String ifwiVersion, String iafwVersion, String scufwVersion, String punitVersion, String valhooksVersion) {
		super(buildId, fingerPrint, kernelVersion, buildUserHostname, modemVersion,
				ifwiVersion, iafwVersion, scufwVersion, punitVersion, valhooksVersion);
		consolidateModemVersion();
	}

	public Build(String longBuildId) {
		super(longBuildId);
		if (longBuildId != null) {
			if (longBuildId.contains(",")) {
				consolidateModemVersion();
			}
		}
	}

	public Build() {
		super();
	}

	public void fillBuildWithSystem() {
		this.buildId = android.os.Build.VERSION.INCREMENTAL;
		this.fingerPrint = android.os.Build.FINGERPRINT;
		this.kernelVersion = getProperty("sys.kernel.version");
		this.buildUserHostname = getProperty("ro.build.user")+"@"+getProperty("ro.build.host");
		this.modemVersion = getProperty("gsm.version.baseband");
		this.ifwiVersion = getProperty("sys.ifwi.version");
		this.iafwVersion = getProperty("sys.ia32.version");
		this.scufwVersion = getProperty("sys.scu.version");
		this.punitVersion = getProperty("sys.punit.version");
		this.valhooksVersion = getProperty("sys.valhooks.version");
		consolidateModemVersion();
	}

	private void consolidateModemVersion(){
		//for crashtool identification, we need to be sure modem version is present
		if (modemVersion.equals("")){
			//fill it with modemid.txt
			BufferedReader modemid;
			try {
				modemid = new BufferedReader(new FileReader(PATH_MODEMID));
			} catch (FileNotFoundException e1) {
				// no file, just return
				return;
			}
			try {
				String sTmp = modemid.readLine();
				if (sTmp != null){
					modemVersion = sTmp;
				}
				modemid.close();
			} catch (IOException e) {
				Log.w(" consolidateModemVersion :" + e.getMessage());
			}
		}
	}

}
