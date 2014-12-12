package com.intel.crashtoolserver.bean;

import java.io.Serializable;

public class ModemVariant implements Serializable {

    private static final long serialVersionUID = -8734276160104789948L;

    private Long id;

    private String name;
    
    /**
     * Used by the server in order to retrieve the right modemVariant when
     * rowSwConfig is blank
     */
    private String regexpr;

    private boolean defaultModem;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRegexpr() {
        return regexpr;
    }

    public void setRegexpr(String regexpr) {
        this.regexpr = regexpr;
    }

    public boolean isDefaultModem() {
        return defaultModem;
    }

    public void setDefaultModem(boolean defaultModem) {
        this.defaultModem = defaultModem;
    }

    @Override
    public String toString() {
        return "ModemVariant [id=" + id + ", name=" + name + ", regexpr="
                + regexpr + ", defaultModem=" + defaultModem + "]";
    }
}
