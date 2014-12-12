package com.intel.crashtoolserver.bean;


public class Project {
	
	private Long id;
	private Mainline mainline;
	private Variant variant;
	private String tracker;
	
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
	
	public String getTracker() {
        return tracker;
    }
    public void setTracker(String tracker) {
        this.tracker = tracker;
    }

    @Override
    public String toString() {
        return "Project [id=" + id + ", mainline=" + mainline + ", variant=" + variant + ", tracker=" + tracker + "]";
    }
	
	
}
