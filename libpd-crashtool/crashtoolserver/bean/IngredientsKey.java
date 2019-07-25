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

public interface IngredientsKey {

	// bios
	public abstract String getBios();
	public abstract void setBios(String bios);
	
	// chaabi
	public abstract String getChaabi();
	public abstract void setChaabi(String chaabi);

	// cse
	public abstract String getCse();
	public abstract void setCse(String cse);
	
	// gop
	public abstract String getGop();
	public abstract void setGop(String gop);
	
	// ia32
	public abstract String getIa32();
	public abstract void setIa32(String ia32);
	
	// iafw
	public abstract String getIafw();
	public abstract void setIafw(String iafw);

	// ifwi
	public abstract String getIfwi();
	public abstract void setIfwi(String ifwi);
	
	// ish
	public abstract String getIsh();
	public abstract void setIsh(String ish);
	
	// mia
	public abstract String getMia();
	public abstract void setMia(String mia);
	
	// modem
	public abstract String getModem();
	public abstract void setModem(String modem);
	
	// modemExt
	public abstract String getModemExt();
	public abstract void setModemExt(String modemExt);

	// mrc
	public abstract String getmRC();
	public abstract void setmRC(String mRC);
	
	// pmic
	public abstract String getPmic();
	public abstract void setPmic(String pmic);
	
	// pmicnvm
	public abstract String getPmicNvm();
	public abstract void setPmicNvm(String pmicNvm);

	// pmc
	public abstract String getpMC();
	public abstract void setpMC(String pMC);
	
	// punit
	public abstract String getPunit();
	public abstract void setPunit(String punit);
	
	// roswconfinfo
	@Deprecated
	public abstract String getRoSwconfInfo();
	@Deprecated
	public abstract void setRoSwconfInfo(String roSwconfInfo);
	
	// roswconfinfobulk
	public abstract RoSwconfInfoBulkKey getRoSwconfInfoBulk();
	
	// roswconfinfotelephony
	public abstract String getRoSwconfInfoTelephony();
	public abstract void setRoSwconfInfoTelephony(String roSwconfInfoTelephony);
	
	// roswconfinfobattery
	public abstract String getRoSwconfInfoBattery();
	public abstract void setRoSwconfInfoBattery(String roSwconfInfoBattery);
	
	// sec
	public abstract String getsEC();
	public abstract void setsEC(String sEC);
	
	// scu
	public abstract String getScu();
	public abstract void setScu(String scu);

	// scubs
	public abstract String getScubs();
	public abstract void setScubs(String scubs);

	// ucode
	public abstract String getuCode();
	public abstract void setuCode(String uCode);
	
	// valhooks
	public abstract String getValhooks();
	public abstract void setValhooks(String valhooks);
}
