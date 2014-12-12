package com.intel.crashtoolserver.bean;

import java.io.Serializable;
import java.util.Date;

public class Campaign implements Serializable {
    
    private static final long serialVersionUID = 7568638016360537657L;
    
    public static final long UNKNOWN_ROWID = -1;

    private Long id;
    private String campaignId;
    private Date date;
    private String type;
    
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getCampaignId() {
        return campaignId;
    }
    public void setCampaignId(String campaignId) {
        this.campaignId = campaignId;
    }
    public Date getDate() {
        return date;
    }
    public void setDate(Date date) {
        this.date = date;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    
    /**
     * @param campaignId 
     * @param type
     */
    public Campaign(String campaignId, String type) {
        this(null, campaignId, null, type);
    }
    
    /**
     * 
     * @param id
     * @param campaignId
     * @param date
     * @param type
     */
    private Campaign(Long id, String campaignId, Date date, String type) {
        super();
        this.id = id;
        this.campaignId = campaignId;
        this.date = date;
        this.type = type;
    }
    @Override
    public String toString() {
        return "Campaign [id=" + id + ", campaignId=" + campaignId + ", date="
                + date + ", type=" + type + "]";
    }
}
