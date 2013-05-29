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

package com.intel.crashreport;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import android.os.SystemProperties;

public class Build {

	private static final int FIELD_NUMBER = 10;
	private static final String PATH_MODEMID = "/logs/modemid.txt";

	private String buildId = "";
	private String fingerPrint = "";
	private String kernelVersion = "";
	private String buildUserHostname = "";
	private String modemVersion = "";
	private String ifwiVersion = "";
	private String iafwVersion = "";
	private String scufwVersion = "";
	private String punitVersion = "";
	private String valhooksVersion = "";

	public Build(String buildId, String fingerPrint, String kernelVersion, String buildUserHostname, String modemVersion,
			String ifwiVersion, String iafwVersion, String scufwVersion, String punitVersion, String valhooksVersion) {
		super();
		this.buildId = buildId;
		this.fingerPrint = fingerPrint;
		this.kernelVersion = kernelVersion;
		this.buildUserHostname = buildUserHostname;
		this.modemVersion = modemVersion;
		this.ifwiVersion = ifwiVersion;
		this.iafwVersion = iafwVersion;
		this.scufwVersion = scufwVersion;
		this.punitVersion = punitVersion;
		this.valhooksVersion = valhooksVersion;
		consolidateModemVersion();
	}

	public Build(String longBuildId) {
		if (longBuildId != null) {
			if (longBuildId.contains(",")) {
				String buildFields[] = longBuildId.split(",");
				if ((buildFields != null) && (buildFields.length == FIELD_NUMBER)) {
					this.buildId = buildFields[0];
					this.fingerPrint = buildFields[1];
					this.kernelVersion = buildFields[2];
					this.buildUserHostname = buildFields[3];
					this.modemVersion = buildFields[4];
					this.ifwiVersion = buildFields[5];
					this.iafwVersion = buildFields[6];
					this.scufwVersion = buildFields[7];
					this.punitVersion = buildFields[8];
					this.valhooksVersion = buildFields[9];
					consolidateModemVersion();
				}
			}
		}
	}

	public Build() {}

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

	public com.intel.crashtoolserver.bean.Build getBuildForServer() {
		return new com.intel.crashtoolserver.bean.Build(buildId,
				fingerPrint,
				kernelVersion,
				buildUserHostname,
				modemVersion,
				ifwiVersion,
				iafwVersion,
				scufwVersion,
				punitVersion,
				valhooksVersion);
	}

	private String getProperty(String name) {
		try {
			String property = SystemProperties.get(name, "");
			return property;
		} catch (IllegalArgumentException e) {
			Log.w("Propery not available : "+name);
		}
		return "";
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

	@Override
	public String toString() {
		return this.buildId+","+this.fingerPrint+","+this.kernelVersion+","+this.buildUserHostname+","+
				this.modemVersion+","+this.ifwiVersion+","+this.iafwVersion+","+this.scufwVersion+","+
				this.punitVersion+","+this.valhooksVersion;
	}

	public String getBuildId() {
		return buildId;
	}

	public void setBuildId(String buildId) {
		this.buildId = buildId;
	}

	public String getFingerPrint() {
		return fingerPrint;
	}

	public void setFingerPrint(String fingerPrint) {
		this.fingerPrint = fingerPrint;
	}

	public String getKernelVersion() {
		return kernelVersion;
	}

	public void setKernelVersion(String kernelVersion) {
		this.kernelVersion = kernelVersion;
	}

	public String getBuildUserHostname() {
		return buildUserHostname;
	}

	public void setBuildUserHostname(String buildUserHostname) {
		this.buildUserHostname = buildUserHostname;
	}

	public String getModemVersion() {
		return modemVersion;
	}

	public void setModemVersion(String modemVersion) {
		this.modemVersion = modemVersion;
	}

	public String getIfwiVersion() {
		return ifwiVersion;
	}

	public void setIfwiVersion(String ifwiVersion) {
		this.ifwiVersion = ifwiVersion;
	}

	public String getIafwVersion() {
		return iafwVersion;
	}

	public void setIafwVersion(String iafwVersion) {
		this.iafwVersion = iafwVersion;
	}

	public String getScufwVersion() {
		return scufwVersion;
	}

	public void setScufwVersion(String scufwVersion) {
		this.scufwVersion = scufwVersion;
	}

	public String getPunitVersion() {
		return punitVersion;
	}

	public void setPunitVersion(String punitVersion) {
		this.punitVersion = punitVersion;
	}

	public String getValhooksVersion() {
		return valhooksVersion;
	}

	public void setValhooksVersion(String valhooksVersion) {
		this.valhooksVersion = valhooksVersion;
	}

}
