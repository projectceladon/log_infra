package com.intel.crashtoolserver.bean;

import java.io.Serializable;

/**
 * Entity that represents a modemVersion with :
 * a name
 * a nbCores
 * @author glivon
 */
public class Modem implements Serializable {
    
    private static final long serialVersionUID = 681624506803727123L;
    private Long id;
    private String name;
    private String cpuType;
    private Long nbCores;
    private Long modemVersionSubFamilyId;
    
    public Modem(String name) {
        this(null, name, name, null, null);
    }
    
    private Modem(Long id, String name, String cpuType, Long nbCores,
            Long modemVersionSubFamilyId) {
        super();
        this.id = id;
        this.name = name;
        this.cpuType = cpuType;
        this.nbCores = nbCores;
        this.modemVersionSubFamilyId = modemVersionSubFamilyId;
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
     * @return the name
     */
    public String getName() {
        return name;
    }
    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }
    /**
     * @return the cpuType
     */
    public String getCpuType() {
        return cpuType;
    }
    /**
     * @param cpuType the cpuType to set
     */
    public void setCpuType(String cpuType) {
        this.cpuType = cpuType;
    }
    /**
     * @return the nbCores
     */
    public Long getNbCores() {
        return nbCores;
    }
    /**
     * @param nbCores the nbCores to set
     */
    public void setNbCores(Long nbCores) {
        this.nbCores = nbCores;
    }
    /**
     * @return the modemVersionSubFamilyId
     */
    public Long getModemVersionSubFamilyId() {
        return modemVersionSubFamilyId;
    }
    /**
     * @param modemVersionSubFamilyId the modemVersionSubFamilyId to set
     */
    public void setModemVersionSubFamilyId(Long modemVersionSubFamilyId) {
        this.modemVersionSubFamilyId = modemVersionSubFamilyId;
    }
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Modem [id=" + id + ", name=" + name + ", nbCores=" + nbCores + ", modemVersionSubFamilyId="
                + modemVersionSubFamilyId + "]";
    }
}