package com.intel.crashtoolserver.bean;

import java.io.Serializable;

/**
 * Bean Crashtype
 *
 * @author greg
 *
 */
public class Crashtype implements Serializable {

	private static final long serialVersionUID = -3933669314600924927L;
	
	public final static int MAX_SIZE_EVENT = 10;
	public final static int MAX_SIZE_TYPE = 20;
	public final static int MAX_SIZE_DATA0 = 255;
	public final static int MAX_SIZE_DATA1 = 255;
	public final static int MAX_SIZE_DATA2 = 512;
	public final static int MAX_SIZE_DATA3 = 255;
	public final static int MAX_SIZE_DATA4 = 512;
	public final static int MAX_SIZE_DATA5 = 512;

	public final static String EVENT_BZ = "BZ";

	private Long id;
	private String event;
	private String type;
	private String data0;
	private String data1;
	private String data2;
	private long bugzilla;
	private Variant variant;

	/**
	 * Default constructor
	 */
	public Crashtype() {
	}

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return the event
	 */
	public String getEvent() {
		return event;
	}

	/**
	 * @param event the event to set
	 */
	public void setEvent(String event) {
		this.event = event;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @return the data0
	 */
	public String getData0() {
		return data0;
	}

	/**
	 * @param data0 the data0 to set
	 */
	public void setData0(String data0) {
		this.data0 = data0;
	}

	/**
	 * @return the data1
	 */
	public String getData1() {
		return data1;
	}

	/**
	 * @param data1 the data1 to set
	 */
	public void setData1(String data1) {
		this.data1 = data1;
	}

	/**
	 * @return the data2
	 */
	public String getData2() {
		return data2;
	}

	/**
	 * @param data2 the data2 to set
	 */
	public void setData2(String data2) {
		this.data2 = data2;
	}

	/**
	 * @return the bugzilla
	 */
	public long getBugzilla() {
		return bugzilla;
	}

	/**
	 * @param bugzilla the bugzilla to set
	 */
	public void setBugzilla(long bugzilla) {
		this.bugzilla = bugzilla;
	}

	public Variant getVariant() {
		return variant;
	}

	public void setVariant(Variant variant) {
		this.variant = variant;
	}

	@Override
	public String toString() {
		return "Crashtype [id=" + id + ", event=" + event + ", type=" + type
				+ ", data0=" + data0 + ", data1=" + data1 + ", data2=" + data2
				+ ", bugzilla=" + bugzilla + ", variant=" + variant + "]";
	}
}
