package com.intel.crashtoolserver.bean;

import java.io.Serializable;
import java.util.Date;

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
    private Date releaseDate;
    private String releaseType;
    private String releaseVersion;
    
    
    public Modem() {    
    }
    
    public Modem(String name) {
        this(null, name, null, null, null, null, null, null);
    }   

    
    private Modem(Long id, String name, String cpuType, Long nbCores,
            Long modemVersionSubFamilyId, Date releaseDate, String releaseType,
            String releaseVersion) {
        super();
        this.id = id;
        this.name = name;
        this.cpuType = cpuType;
        this.nbCores = nbCores;
        this.modemVersionSubFamilyId = modemVersionSubFamilyId;
        this.releaseDate = releaseDate;
        this.releaseType = releaseType;
        this.releaseVersion = releaseVersion;
    }

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

    public String getCpuType() {
        return cpuType;
    }
    
    public void setCpuType(String cpuType) {
        this.cpuType = cpuType;
    }
    
    public Long getNbCores() {
        return nbCores;
    }
    
    public void setNbCores(Long nbCores) {
        this.nbCores = nbCores;
    }
    
    public Long getModemVersionSubFamilyId() {
        return modemVersionSubFamilyId;
    }
    
    public void setModemVersionSubFamilyId(Long modemVersionSubFamilyId) {
        this.modemVersionSubFamilyId = modemVersionSubFamilyId;
    }
    
    public Date getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getReleaseType() {
        return releaseType;
    }

    public void setReleaseType(String releaseType) {
        this.releaseType = releaseType;
    }

    public String getReleaseVersion() {
        return releaseVersion;
    }

    public void setReleaseVersion(String releaseVersion) {
        this.releaseVersion = releaseVersion;
    }

    @Override
    public String toString() {
        return "Modem [id=" + id + ", name=" + name + ", cpuType=" + cpuType
                + ", nbCores=" + nbCores + ", modemVersionSubFamilyId="
                + modemVersionSubFamilyId + ", releaseDate=" + releaseDate
                + ", releaseType=" + releaseType + ", releaseVersion="
                + releaseVersion + "]";
    }
}