package com.intel.crashtoolserver.bean;

import java.io.Serializable;
import java.util.Date;

public class AxfLocation implements Serializable {
    
    private static final long serialVersionUID = 681624506803727123L;
    private Long id;
    private String modemName;
    private String location;
    private Date date;
    
	public AxfLocation() {    
    }
    
    public AxfLocation(String modemName, String location) {
        this(null, modemName, location);
    }   

    private AxfLocation(Long id, String modemName, String location) {
        super();
        this.id = id;
        this.modemName = modemName;
        this.location = location;
    }

    public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getModemName() {
		return modemName;
	}

	public void setModemName(String modemName) {
		this.modemName = modemName;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	
	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	@Override
    public String toString() {
        return "Modem [id=" + id + ", modemName=" + modemName 
        		+ ", location=" + location + ", date=" + date + "]";
    }
}