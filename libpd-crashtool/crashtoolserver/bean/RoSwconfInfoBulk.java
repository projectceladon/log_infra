package com.intel.crashtoolserver.bean;

/**
 * Bean that encapsulates RoSw information : telephony/Battery
 * @author glivon
 */
public class RoSwconfInfoBulk {
	private static final String DEFAULT_VALUE = "";
	
	private String roSwconfInfoTelephony = DEFAULT_VALUE;
	private String roSwconfInfoBattery = DEFAULT_VALUE;
	
	/**
	 * Default Constructor
	 */
	public RoSwconfInfoBulk() {
		// Default constructor
	}
	
	/**
	 * Constructor with all Attributes
	 * @param tel
	 * @param battery
	 */
	public RoSwconfInfoBulk(String telephony, String battery) {
		this.roSwconfInfoTelephony = telephony;
		this.roSwconfInfoBattery = battery;
	}

	/**
	 * @return the roSwconfInfotelephony
	 */
	public String getRoSwconfInfoTelephony() {
		return roSwconfInfoTelephony;
	}

	/**
	 * @param roSwconfInfotelephony the roSwconfInfotelephony to set
	 */
	public void setRoSwconfInfoTelephony(String roSwconfInfotelephony) {
		this.roSwconfInfoTelephony = roSwconfInfotelephony;
	}

	/**
	 * @return the roSwconfInfoBattery
	 */
	public String getRoSwconfInfoBattery() {
		return roSwconfInfoBattery;
	}

	/**
	 * @param roSwconfInfoBattery the roSwconfInfoBattery to set
	 */
	public void setRoSwconfInfoBattery(String roSwconfInfoBattery) {
		this.roSwconfInfoBattery = roSwconfInfoBattery;
	}

	
	/**
	 * Is no roSwconfInfoBulk field filled
	 * @return
	 */
	public boolean isEmpty() {
		return (roSwconfInfoBattery == null || roSwconfInfoBattery.isEmpty()) 
				&& (roSwconfInfoTelephony == null || roSwconfInfoTelephony.isEmpty());
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "RoSwconfInfoBulk [roSwconfInfoTelephony="
				+ roSwconfInfoTelephony + ", roSwconfInfoBattery="
				+ roSwconfInfoBattery + "]";
	}
}