/* INTEL CONFIDENTIAL
 * Copyright 2015 Intel Corporation
 *
 * The source code contained or described herein and all documents
 * related to the source code ("Material") are owned by Intel
 * Corporation or its suppliers or licensors. Title to the Material
 * remains with Intel Corporation or its suppliers and
 * licensors. The Material contains trade secrets and proprietary
 * and confidential information of Intel or its suppliers and
 * licensors. The Material is protected by worldwide copyright and
 * trade secret laws and treaty provisions. No part of the Material
 * may be used, copied, reproduced, modified, published, uploaded,
 * posted, transmitted, distributed, or disclosed in any way without
 * Intel's prior express written permission.
 *
 * No license under any patent, copyright, trade secret or other
 * intellectual property right is granted to or conferred upon you
 * by disclosure or delivery of the Materials, either expressly, by
 * implication, inducement, estoppel or otherwise. Any license under
 * such intellectual property rights must be express and approved by
 * Intel in writing.
 */

package com.intel.crashtoolserver.bean;

public class Ingredients implements IngredientsKey {
    
    private static final String DEFAULT_VALUE = "";

	private String iafw = DEFAULT_VALUE; // varchar(92)
	private String mia  = DEFAULT_VALUE; // varchar(92)
	private String ifwi = DEFAULT_VALUE; // varchar(92)
	private String ia32 = DEFAULT_VALUE; // varchar(92)
	private String scu = DEFAULT_VALUE; // varchar(92)
	private String scubs = DEFAULT_VALUE; // varchar(92)
	private String punit = DEFAULT_VALUE; // varchar(92)
	private String valhooks = DEFAULT_VALUE; // varchar(92)
	private String chaabi = DEFAULT_VALUE; // varchar(92)
	private String pmicNvm = DEFAULT_VALUE; // varchar(92)
	private String uCode = DEFAULT_VALUE; // varchar(92)
	private String pmic = DEFAULT_VALUE; // varchar(92)
	private String bios = DEFAULT_VALUE; // varchar(92)
	private String gop = DEFAULT_VALUE; // varchar(92)
	private String sEC = DEFAULT_VALUE; // varchar(92)
	private String mRC = DEFAULT_VALUE; // varchar(92)
	private String pMC = DEFAULT_VALUE; // varchar(92)
	private String modem = DEFAULT_VALUE; // varchar(92)
	private String modemExt = DEFAULT_VALUE; // varchar(92)
	@Deprecated
	private String roSwconfInfo = DEFAULT_VALUE; // varchar(92)
	private RoSwconfInfoBulk roSwconfInfoBulk;
	
	public Ingredients() {
	    
	}
	
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
	    this(iafw, DEFAULT_VALUE, ifwi, DEFAULT_VALUE,
            scu, DEFAULT_VALUE, punit, valhooks,
            DEFAULT_VALUE, DEFAULT_VALUE, DEFAULT_VALUE, DEFAULT_VALUE,
            DEFAULT_VALUE, DEFAULT_VALUE, DEFAULT_VALUE, DEFAULT_VALUE, DEFAULT_VALUE,
            modem, DEFAULT_VALUE, DEFAULT_VALUE);
	}

	/**
	 * 
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
	 * @param pmic
	 * @param bios
	 * @param gop
	 * @param sEC
	 * @param mRC
	 * @param pMC
	 * @param modem
	 * @param modemExt
	 * @param roSwconfInfo
	 */
    public Ingredients(String iafw, String mia, String ifwi, String ia32,
            String scu, String scubs, String punit, String valhooks,
            String chaabi, String pmicNvm, String uCode, String pmic,
            String bios, String gop, String sEC, String mRC, String pMC,
            String modem, String modemExt, String roSwconfInfo) {
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
        this.pmic = pmic;
        this.bios = bios;
        this.gop = gop;
        this.sEC = sEC;
        this.mRC = mRC;
        this.pMC = pMC;
        this.modem = modem;
        this.modemExt = modemExt;
        this.roSwconfInfo = roSwconfInfo;
    }
    

    /**
     * Constructor that deals with roSwConfInfoBulk
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
     * @param pmic
     * @param bios
     * @param gop
     * @param sEC
     * @param mRC
     * @param pMC
     * @param modem
     * @param modemExt
     * @param roSwConinfoBulk
     */
    public Ingredients(String iafw, String mia, String ifwi, String ia32,
            String scu, String scubs, String punit, String valhooks,
            String chaabi, String pmicNvm, String uCode, String pmic,
            String bios, String gop, String sEC, String mRC, String pMC,
            String modem, String modemExt, RoSwconfInfoBulk roSwConfinfoBulk) {
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
        this.pmic = pmic;
        this.bios = bios;
        this.gop = gop;
        this.sEC = sEC;
        this.mRC = mRC;
        this.pMC = pMC;
        this.modem = modem;
        this.modemExt = modemExt;
        this.roSwconfInfoBulk = roSwConfinfoBulk;
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

//	public String getpUnit() {
//		return punit;
//	}
//
//	public void setpUnit(String punit) {
//		this.punit = punit;
//	}

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
	
	@Deprecated
    public String getRoSwconfInfo() {
        return roSwconfInfo;
    }

	@Deprecated
    public void setRoSwconfInfo(String roSwconfInfo) {
        this.roSwconfInfo = roSwconfInfo;
    }

	public RoSwconfInfoBulk getRoSwconfInfoBulk() {
		return roSwconfInfoBulk;
	}

	public void setRoSwconfInfoBulk(RoSwconfInfoBulk roSwconfInfoBulk) {
		this.roSwconfInfoBulk = roSwconfInfoBulk;
	}

	@Override
	public String getRoSwconfInfoTelephony() {
    	if (roSwconfInfoBulk == null) {
    		return null;
    	}
		return roSwconfInfoBulk.getRoSwconfInfoTelephony();
	}

	@Override
	public void setRoSwconfInfoTelephony(String roSwconfInfoTelephony) {
		if (roSwconfInfoBulk == null) {
			roSwconfInfoBulk = new RoSwconfInfoBulk();
		}
		roSwconfInfoBulk.setRoSwconfInfoTelephony(roSwconfInfoTelephony);
	}

	@Override
	public String getRoSwconfInfoBattery() {
    	if (roSwconfInfoBulk == null) {
    		return null;
    	}
		return roSwconfInfoBulk.getRoSwconfInfoBattery();
	}

	@Override
	public void setRoSwconfInfoBattery(String roSwconfInfoBattery) {
		if (roSwconfInfoBulk == null) {
			roSwconfInfoBulk = new RoSwconfInfoBulk();
		}
		roSwconfInfoBulk.setRoSwconfInfoBattery(roSwconfInfoBattery);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Ingredients [iafw=" + iafw + ", mia=" + mia + ", ifwi=" + ifwi
				+ ", ia32=" + ia32 + ", scu=" + scu + ", scubs=" + scubs
				+ ", punit=" + punit + ", valhooks=" + valhooks + ", chaabi="
				+ chaabi + ", pmicNvm=" + pmicNvm + ", uCode=" + uCode
				+ ", pmic=" + pmic + ", bios=" + bios + ", gop=" + gop
				+ ", sEC=" + sEC + ", mRC=" + mRC + ", pMC=" + pMC + ", modem="
				+ modem + ", modemExt=" + modemExt 
				+ ", roSwconfInfo=" + roSwconfInfo 
				+ ((roSwconfInfoBulk != null && !roSwconfInfoBulk.isEmpty()) ? ", roSwconfInfoBulk=" + roSwconfInfoBulk : "") 
				+ "]";
	}
}
