package com.intel.crashtoolserver.bean;


public class Project {
	
	private Long id;
	private Mainline mainline;
	private Variant variant;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Mainline getMainline() {
		return mainline;
	}
	public void setMainline(Mainline mainline) {
		this.mainline = mainline;
	}
	public Variant getVariant() {
		return variant;
	}
	public void setVariant(Variant variant) {
		this.variant = variant;
	}
	@Override
	public String toString() {
		return "Project [id=" + id + ", mainline=" + mainline + ", variant="
				+ variant + "]";
	}
	
	
}
