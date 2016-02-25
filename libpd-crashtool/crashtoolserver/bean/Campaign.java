/* INTEL CONFIDENTIAL
 * Copyright 2015 Intel Corporation
 *
 * The source code contained or described herein and all documents
 * related to the source code ("Material") are owned by Intel
 * Corporation or its suppliers or licensors. Title to the Material
 * remains with Intel Corporation or its suppliers and
 * licensors. The Material contains trade secrets and proprietary
 * and confidential information of Intel or its suppliers and
 * licensors. The Material is protected by worldwide copyright and
 * trade secret laws and treaty provisions. No part of the Material
 * may be used, copied, reproduced, modified, published, uploaded,
 * posted, transmitted, distributed, or disclosed in any way without
 * Intel's prior express written permission.
 *
 * No license under any patent, copyright, trade secret or other
 * intellectual property right is granted to or conferred upon you
 * by disclosure or delivery of the Materials, either expressly, by
 * implication, inducement, estoppel or otherwise. Any license under
 * such intellectual property rights must be express and approved by
 * Intel in writing.
 */

package com.intel.crashtoolserver.bean;

import java.io.Serializable;
import java.util.Date;
@Deprecated
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
