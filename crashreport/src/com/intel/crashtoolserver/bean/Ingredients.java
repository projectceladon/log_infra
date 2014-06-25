package com.intel.crashtoolserver.bean;


public class Ingredients {

	private String iafw; // varchar(92)
	private String mia; // varchar(92)
	private String ifwi; // varchar(92)
	private String ia32; // varchar(92)
	private String scu; // varchar(92)
	private String scubs; // varchar(92)
	private String punit; // varchar(92)
	private String valhooks; // varchar(92)
	private String chaabi; // varchar(92)
	private String pmicNvm; // varchar(92)
	private String uCode; // varchar(92)
	private String pUnit; // varchar(92)
	private String pmic; // varchar(92)
	private String bios; // varchar(92)
	private String gop; // varchar(92)
	private String sEC; // varchar(92)
	private String mRC; // varchar(92)
	private String pMC; // varchar(92)
	private String modem; // varchar(92)
	private String modemExt; // varchar(92)
	
	
	
	/**
	 * Constructor for legacy purpose. PD under 2.0
	 * @param modem
	 * @param ifwi
	 * @param iafw
	 * @param scu
	 * @param punit
	 * @param valhooks
	 */
	public Ingredients(String modem, String ifwi, String iafw, String scu, String punit, String valhooks) {
		this (iafw, null, ifwi, null,
				scu, null, punit, valhooks,
				null, null, null, null,
				null, null, null, null, null,
				null, modem, null);
	}
	
	/**
	 * Constructor used by PhoneDoctor 2.0 and above
	 * @param iafw
	 * @param mia
	 * @param ifwi
	 * @param ia32
	 * @param scu
	 * @param scubs
	 * @param punit
	 * @param valhooks
	 * @param chaabi
	 * @param pmicNvm
	 * @param uCode
	 * @param pUnit2
	 * @param pmic
	 * @param bios
	 * @param gop
	 * @param sEC
	 * @param mRC
	 * @param pMC
	 * @param modem
	 * @param modemExt
	 */
	public Ingredients(String iafw, String mia, String ifwi, String ia32,
			String scu, String scubs, String punit, String valhooks,
			String chaabi, String pmicNvm, String uCode, String pUnit2,
			String pmic, String bios, String gop, String sEC, String mRC,
			String pMC, String modem, String modemExt) {
		super();
		this.iafw = iafw;
		this.mia = mia;
		this.ifwi = ifwi;
		this.ia32 = ia32;
		this.scu = scu;
		this.scubs = scubs;
		this.punit = punit;
		this.valhooks = valhooks;
		this.chaabi = chaabi;
		this.pmicNvm = pmicNvm;
		this.uCode = uCode;
		pUnit = pUnit2;
		this.pmic = pmic;
		this.bios = bios;
		this.gop = gop;
		this.sEC = sEC;
		this.mRC = mRC;
		this.pMC = pMC;
		this.modem = modem;
		this.modemExt = modemExt;
	}

	public String getIafw() {
		return iafw;
	}

	public void setIafw(String iafw) {
		this.iafw = iafw;
	}

	public String getMia() {
		return mia;
	}

	public void setMia(String mia) {
		this.mia = mia;
	}

	public String getIfwi() {
		return ifwi;
	}

	public void setIfwi(String ifwi) {
		this.ifwi = ifwi;
	}

	public String getIa32() {
		return ia32;
	}

	public void setIa32(String ia32) {
		this.ia32 = ia32;
	}

	public String getScu() {
		return scu;
	}

	public void setScu(String scu) {
		this.scu = scu;
	}

	public String getScubs() {
		return scubs;
	}

	public void setScubs(String scubs) {
		this.scubs = scubs;
	}

	public String getPunit() {
		return punit;
	}

	public void setPunit(String punit) {
		this.punit = punit;
	}

	public String getValhooks() {
		return valhooks;
	}

	public void setValhooks(String valhooks) {
		this.valhooks = valhooks;
	}

	public String getChaabi() {
		return chaabi;
	}

	public void setChaabi(String chaabi) {
		this.chaabi = chaabi;
	}

	public String getPmicNvm() {
		return pmicNvm;
	}

	public void setPmicNvm(String pmicNvm) {
		this.pmicNvm = pmicNvm;
	}

	public String getuCode() {
		return uCode;
	}

	public void setuCode(String uCode) {
		this.uCode = uCode;
	}

	public String getpUnit() {
		return pUnit;
	}

	public void setpUnit(String pUnit) {
		this.pUnit = pUnit;
	}

	public String getPmic() {
		return pmic;
	}

	public void setPmic(String pmic) {
		this.pmic = pmic;
	}

	public String getBios() {
		return bios;
	}

	public void setBios(String bios) {
		this.bios = bios;
	}

	public String getGop() {
		return gop;
	}

	public void setGop(String gop) {
		this.gop = gop;
	}

	public String getsEC() {
		return sEC;
	}

	public void setsEC(String sEC) {
		this.sEC = sEC;
	}

	public String getmRC() {
		return mRC;
	}

	public void setmRC(String mRC) {
		this.mRC = mRC;
	}

	public String getpMC() {
		return pMC;
	}

	public void setpMC(String pMC) {
		this.pMC = pMC;
	}

	public String getModem() {
		return modem;
	}

	public void setModem(String modem) {
		this.modem = modem;
	}

	public String getModemExt() {
		return modemExt;
	}

	public void setModemExt(String modemExt) {
		this.modemExt = modemExt;
	}

	@Override
	public String toString() {
		return "Ingredients [iafw=" + iafw + ", mia=" + mia + ", ifwi=" + ifwi
				+ ", ia32=" + ia32 + ", scu=" + scu + ", scubs=" + scubs
				+ ", punit=" + punit + ", valhooks=" + valhooks + ", chaabi="
				+ chaabi + ", pmicNvm=" + pmicNvm + ", uCode=" + uCode
				+ ", pUnit=" + pUnit + ", pmic=" + pmic + ", bios=" + bios
				+ ", gop=" + gop + ", sEC=" + sEC + ", mRC=" + mRC + ", pMC="
				+ pMC + ", modem=" + modem + ", modemExt=" + modemExt + "]";
	}
}
