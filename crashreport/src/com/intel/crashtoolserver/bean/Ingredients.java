package com.intel.crashtoolserver.bean;

public class Ingredients {

	private String ifwi; // varchar(92)
	private String bios; // varchar(92)
	private String gop; // varchar(92)
	private String sec; // varchar(92)
	private String mrc; // varchar(92)
	private String uCode; // varchar(92)
	private String pmc; // varchar(92)
	private String chaabi; // varchar(92)
	private String iafw; // varchar(92)
	private String scu; // varchar(92)
	private String punit; // varchar(92)
	private String modem; // varchar(92)
	private String valhooks; // varchar(92)
	
	/**
	 * 
	 * @param ifwi
	 * @param bios
	 * @param gop
	 * @param sec
	 * @param mrc
	 * @param uCode
	 * @param pmc
	 * @param chaabi
	 * @param iafw
	 * @param scu
	 * @param punit
	 * @param modem
	 * @param valhooks
	 */
	public Ingredients(String ifwi, String bios, String gop, String sec,
			String mrc, String uCode, String pmc, String chaabi, String iafw,
			String scu, String punit, String modem, String valhooks) {
		super();
		this.ifwi = ifwi;
		this.bios = bios;
		this.gop = gop;
		this.sec = sec;
		this.mrc = mrc;
		this.uCode = uCode;
		this.pmc = pmc;
		this.chaabi = chaabi;
		this.iafw = iafw;
		this.scu = scu;
		this.punit = punit;
		this.modem = modem;
		this.valhooks = valhooks;
	}
	
	public String getIfwi() {
		return ifwi;
	}

	public void setIfwi(String ifwi) {
		this.ifwi = ifwi;
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

	public String getSec() {
		return sec;
	}

	public void setSec(String sec) {
		this.sec = sec;
	}

	public String getMrc() {
		return mrc;
	}

	public void setMrc(String mrc) {
		this.mrc = mrc;
	}

	public String getuCode() {
		return uCode;
	}

	public void setuCode(String uCode) {
		this.uCode = uCode;
	}

	public String getPmc() {
		return pmc;
	}

	public void setPmc(String pmc) {
		this.pmc = pmc;
	}

	public String getChaabi() {
		return chaabi;
	}

	public void setChaabi(String chaabi) {
		this.chaabi = chaabi;
	}

	public String getIafw() {
		return iafw;
	}

	public void setIafw(String iafw) {
		this.iafw = iafw;
	}

	public String getScu() {
		return scu;
	}

	public void setScu(String scu) {
		this.scu = scu;
	}

	public String getPunit() {
		return punit;
	}

	public void setPunit(String punit) {
		this.punit = punit;
	}

	public String getModem() {
		return modem;
	}

	public void setModem(String modem) {
		this.modem = modem;
	}

	public String getValhooks() {
		return valhooks;
	}

	public void setValhooks(String valhooks) {
		this.valhooks = valhooks;
	}

	@Override
	public String toString() {
		return "Ingredients [ifwi=" + ifwi + ", bios=" + bios + ", gop=" + gop
				+ ", sec=" + sec + ", mrc=" + mrc + ", uCode=" + uCode
				+ ", pmc=" + pmc + ", chaabi=" + chaabi + ", iafw=" + iafw
				+ ", scu=" + scu + ", punit=" + punit + ", modem=" + modem
				+ ", valhooks=" + valhooks + "]";
	}

}
