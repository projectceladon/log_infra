package com.intel.crashtoolserver.bean;

public interface IngredientsKey {

	public abstract String getIafw();

	public abstract void setIafw(String iafw);

	public abstract String getMia();

	public abstract void setMia(String mia);

	public abstract String getIfwi();

	public abstract void setIfwi(String ifwi);

	public abstract String getIa32();

	public abstract void setIa32(String ia32);

	public abstract String getScu();

	public abstract void setScu(String scu);

	public abstract String getScubs();

	public abstract void setScubs(String scubs);

	public abstract String getPunit();

	public abstract void setPunit(String punit);

	public abstract String getValhooks();

	public abstract void setValhooks(String valhooks);

	public abstract String getChaabi();

	public abstract void setChaabi(String chaabi);

	public abstract String getPmicNvm();

	public abstract void setPmicNvm(String pmicNvm);

	public abstract String getuCode();

	public abstract void setuCode(String uCode);

	public abstract String getPmic();

	public abstract void setPmic(String pmic);

	public abstract String getBios();

	public abstract void setBios(String bios);

	public abstract String getGop();

	public abstract void setGop(String gop);

	public abstract String getsEC();

	public abstract void setsEC(String sEC);

	public abstract String getmRC();

	public abstract void setmRC(String mRC);

	public abstract String getpMC();

	public abstract void setpMC(String pMC);

	public abstract String getModem();

	public abstract void setModem(String modem);

	public abstract String getModemExt();

	public abstract void setModemExt(String modemExt);
	
	public abstract String getRoSwconfInfo();
	
	public abstract void setRoSwconfInfo(String roSwconfInfo);

	public abstract RoSwconfInfoBulk getRoSwconfInfoBulk();
	
	public abstract void setRoSwconfInfoBulk(RoSwconfInfoBulk roSwconfInfoBulk);
	
	public abstract String getRoSwconfInfoTelephony();

	public abstract void setRoSwconfInfoTelephony(String roSwconfInfoTelephony);
	
	public abstract String getRoSwconfInfoBattery();

	public abstract void setRoSwconfInfoBattery(String roSwconfInfoBattery);
}
