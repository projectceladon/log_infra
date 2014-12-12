package com.intel.crashtoolserver.bean;

/**
 * Bean that encapsulates RoSw information : telephony/Battery
 * @author glivon
 */
public class RoSwConfinfoBulk {
	private static final String DEFAULT_VALUE = "";
	
	private String roSwconfInfotelephony = DEFAULT_VALUE;
	private String roSwconfInfoBattery = DEFAULT_VALUE;
	
	/**
	 * Default Constructor
	 */
	public RoSwConfinfoBulk() {
		// Default constructor
	}
	
	/**
	 * Constructor with all Attributes
	 * @param tel
	 * @param battery
	 */
	public RoSwConfinfoBulk(String telephony, String battery) {
		this.roSwconfInfotelephony = telephony;
		this.roSwconfInfoBattery = battery;
	}

	/**
	 * @return the roSwconfInfotelephony
	 */
	public String getRoSwconfInfotelephony() {
		return roSwconfInfotelephony;
	}

	/**
	 * @param roSwconfInfotelephony the roSwconfInfotelephony to set
	 */
	public void setRoSwconfInfotelephony(String roSwconfInfotelephony) {
		this.roSwconfInfotelephony = roSwconfInfotelephony;
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
				&& (roSwconfInfotelephony == null || roSwconfInfotelephony.isEmpty());
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "RoSwConfinfoBulk [roSwconfInfotelephony="
				+ roSwconfInfotelephony + ", roSwconfInfoBattery="
				+ roSwconfInfoBattery + "]";
	}
}