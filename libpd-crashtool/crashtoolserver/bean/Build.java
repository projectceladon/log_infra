/* Copyright (C) 2019 Intel Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.intel.crashtoolserver.bean;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class Build implements Serializable, BuildKey {

	private static final long serialVersionUID = 5286879029277574763L;

	public static final String DEFAULT_VALUE = "";
	public static final int MAX_SIZE_BUILDID = 40;
	public static final int MAX_SIZE_COMPONENT = 92;
	public static final int MAX_SIZE_FINGERTPRINT = 120;
	public final static String DEFAULT_BUILD_TYPE = "dev";
	public final static String LATEST_BUILD_TYPE = "latest";
	public final static String DEFAULT_BUILD_VARIANT = "unknown";

	public final static String DEFAULT_ORGANIZATION = "";

	public final static List<String> LEGACY_UNIQUE_KEY_COMPONENTS = Arrays.asList("modem", "ifwi", "iafw", "scu", "punit", "valhooks");

	private Long id;
	private String buildId;
	private String name = DEFAULT_VALUE;
	private String fingerPrint = DEFAULT_VALUE;
	private String kernelVersion = DEFAULT_VALUE;
	private String buildUserHostname = DEFAULT_VALUE;
	private String modemVersion = DEFAULT_VALUE;
	@Deprecated
	private String ifwiVersion = DEFAULT_VALUE;
	@Deprecated
	private String iafwVersion  = DEFAULT_VALUE;
	@Deprecated
	private String scufwVersion  = DEFAULT_VALUE;
	@Deprecated
	private String punitVersion  = DEFAULT_VALUE;
	@Deprecated
	private String valhooksVersion  = DEFAULT_VALUE;
	@Deprecated
	private String variant;
	private String type;
	private String os;
	private Project project;	
	private String ingredientsJson;
	private Ingredients ingredients;
	private String uniqueKey;
	private String uniqueKeyFull;
	private String organization;
	
	private Modem modem;
	private Modem modemExt;
	
	@Deprecated
	private String uniqueKeyGenerationMethod;
	private List<String> uniqueKeyComponents;
	
	@Deprecated
	private Long mainlineId;
	@Deprecated
	private Mainline mainline;

	private Date date;
	
	private String mainlineStr;
	private String targetProduct;
	private String targetProductScalability;
	private String projectConfigName;
	
	
	/**
	 * full constructor used internally
	 * @param id
	 * @param buildId
	 * @param name
	 * @param fingerPrint
	 * @param kernelVersion
	 * @param buildUserHostname
	 * @param modemVersion
	 * @param ifwiVersion
	 * @param iafwVersion
	 * @param scufwVersion
	 * @param punitVersion
	 * @param valhooksVersion
	 * @param variant
	 * @param type
	 * @param os
	 * @param project
	 * @param ingredientsJson
	 * @param uniqueKeyComponents
	 * @param mainlineId
	 * @param mainline
	 * @param date
	 */
	private Build(Long id, String buildId, String name, String fingerPrint,
			String kernelVersion, String buildUserHostname,
			String modemVersion, String ifwiVersion, String iafwVersion,
			String scufwVersion, String punitVersion, String valhooksVersion,
			String variant, String type, String os, Project project,
			String ingredientsJson,
			List<String> uniqueKeyComponents, Long mainlineId,
			Mainline mainline, Date date, String uniqueKeyFull, String organization,
			String mainlineStr, String targetProduct, String targetProductScalability, String projectConfigName) {
		
		super();
		this.id = id;
		this.buildId = buildId;
		this.name = name;
		this.fingerPrint = fingerPrint;
		this.kernelVersion = kernelVersion;
		this.buildUserHostname = buildUserHostname;
		this.modemVersion = modemVersion;
		this.ifwiVersion = ifwiVersion;
		this.iafwVersion = iafwVersion;
		this.scufwVersion = scufwVersion;
		this.punitVersion = punitVersion;
		this.valhooksVersion = valhooksVersion;
		this.variant = variant;
		this.type = type;
		this.os = os;
		this.project = project;
		this.ingredientsJson = ingredientsJson;
		this.uniqueKeyComponents = uniqueKeyComponents;
		this.mainlineId = mainlineId;
		this.mainline = mainline;
		this.date = date;
		this.uniqueKeyFull = uniqueKeyFull;
		this.organization = organization;
		this.mainlineStr = mainlineStr;
		this.targetProduct = targetProduct;
		this.targetProductScalability = targetProductScalability;
		this.projectConfigName = projectConfigName;
	}


	/**
	 * Default constructor
	 */
	public Build() {
		
	}
	
	/**
	 * Used by PD under 1.0
	 * @param buildId
	 */
	@Deprecated
	public Build(String buildId) {
		super();
		this.buildId = buildId;
	}

	/**
	 * Used by MPM, EGG
	 * @param buildId
	 * @param fingerPrint
	 * @param kernelVersion
	 * @param buildUserHostname
	 * @param modemVersion
	 * @param ifwiVersion
	 * @param iafwVersion
	 * @param scufwVersion
	 * @param punitVersion
	 * @param valhooksVersion
	 * @param os
	 */
	public Build(String buildId, String fingerPrint, String kernelVersion,
			String buildUserHostname, String modemVersion, String ifwiVersion, String iafwVersion,
			String scufwVersion, String punitVersion, String valhooksVersion, String os) {
		
		this(0l, buildId, null, fingerPrint,
				kernelVersion, buildUserHostname,
				modemVersion, ifwiVersion, iafwVersion,
				scufwVersion, punitVersion, valhooksVersion,
				null, null, os, null,
				null,
				null, 0l,
				null, null, null, null,
				null, null, null, null);
	}
	
	/**
	 * Used by PD under 1.6
	 * @param buildId
	 * @param fingerPrint
	 * @param kernelVersion
	 * @param buildUserHostname
	 * @param modemVersion
	 * @param ifwiVersion
	 * @param iafwVersion
	 * @param scufwVersion
	 * @param punitVersion
	 * @param valhooksVersion
	 */
	@Deprecated
	public Build(String buildId, String fingerPrint, String kernelVersion,
			String buildUserHostname, String modemVersion, String ifwiVersion, String iafwVersion,
			String scufwVersion, String punitVersion, String valhooksVersion) {
		
		this(0l, buildId, null, fingerPrint,
				kernelVersion, buildUserHostname,
				modemVersion, ifwiVersion, iafwVersion,
				scufwVersion, punitVersion, valhooksVersion,
				null, null, null, null,
				null,
				null, 0l,
				null, null, null, null,
				null, null, null, null);
	}
	
	/**
	 * Used by PD above 2.0
	 * @param uniqueKey
	 * @param uniquekeyGenerationMethod
	 * @param buildId
	 * @param fingerPrint
	 * @param kernelVersion
	 * @param buildUserHostname
	 * @param ingredientsJson
	 * @param os
	 */
	@Deprecated
	public Build(
			List<String> uniquekeyComponents, String buildId, String fingerPrint,
			String kernelVersion, String buildUserHostname, String ingredientsJson,
			String os) {
		
		 this(0l, buildId, null, fingerPrint,
					kernelVersion, buildUserHostname,
					null, null, null,
					null, null, null,
					null, null, os, null,
					ingredientsJson, 
					uniquekeyComponents, 0l,
					null, null, null, null,
					null, null, null, null);
	}
	
	/**
     * Used by MPTA above 3.5
     * @param uniqueKey
     * @param uniquekeyGenerationMethod
     * @param buildId
     * @param fingerPrint
     * @param kernelVersion
     * @param buildUserHostname
     * @param ingredientsJson
     * @param os
     * @param organization
     */
    public Build(
            List<String> uniquekeyComponents, String buildId, String fingerPrint,
            String kernelVersion, String buildUserHostname, String ingredientsJson,
            String os, String organization) {
        
         this(0l, buildId, null, fingerPrint,
                    kernelVersion, buildUserHostname,
                    null, null, null,
                    null, null, null,
                    null, null, os, null,
                    ingredientsJson, 
                    uniquekeyComponents, 0l,
                    null, null, null, organization,
                    null, null, null, null);
    }
    
	/**
     * Used by PD latest
     * @param uniqueKey
     * @param uniquekeyGenerationMethod
     * @param buildId
     * @param fingerPrint
     * @param kernelVersion
     * @param buildUserHostname
     * @param ingredientsJson
     * @param os
     * @param organization
     */
    public Build(
            List<String> uniquekeyComponents, String buildId, String fingerPrint,
            String kernelVersion, String buildUserHostname, String ingredientsJson,
            String os, String organization, String mainlineStr, String targetProduct, String targetProductScalability, String projectConfigName) {
        
         this(0l, buildId, null, fingerPrint,
                    kernelVersion, buildUserHostname,
                    null, null, null,
                    null, null, null,
                    null, null, os, null,
                    ingredientsJson, 
                    uniquekeyComponents, 0l,
                    null, null, null, organization,
                    mainlineStr, targetProduct, targetProductScalability, projectConfigName);
    }
	
	/**
	 * Used by CLA
	 * @param buildId
	 * @param modemVersion
	 * @param os, operating system
	 */
	public Build(String buildId, String modemVersion, String os) {
		
		 this(0l, buildId, null, null,
					null, null,
					modemVersion, null, null,
					null, null, null,
					null, null, os, null,
					null, 
					null, 0l,
					null, null, null, null,
					null, null, null, null);
	}

	/* (non-Javadoc)
	 * @see com.intel.crashtoolserver.bean.BuildKey#getBuildUserHostname()
	 */
	@Override
	public String getBuildUserHostname() {
		return buildUserHostname;
	}

	/**
	 * @param buildUserHostname the buildUserHostname to set
	 */
	public void setBuildUserHostname(String buildUserHostname) {
		this.buildUserHostname = buildUserHostname;
	}

	/**
	 * @return the ifwiVersion
	 */
	@Deprecated
	public String getIfwiVersion() {
		return ifwiVersion;
	}

	/**
	 * @param ifwiVersion the ifwiVersion to set
	 */
	@Deprecated
	public void setIfwiVersion(String ifwiVersion) {
		this.ifwiVersion = ifwiVersion;
	}

	/* (non-Javadoc)
	 * @see com.intel.crashtoolserver.bean.BuildKey#getFingerPrint()
	 */
	@Override
	public String getFingerPrint() {
		return fingerPrint;
	}

	/**
	 * @param fingerPrint the fingerPrint to set
	 */
	public void setFingerPrint(String fingerPrint) {
		this.fingerPrint = fingerPrint;
	}

	/* (non-Javadoc)
	 * @see com.intel.crashtoolserver.bean.BuildKey#getBuildId()
	 */
	@Override
	public String getBuildId() {
		return buildId;
	}

	/**
	 * @param buildId the buildId to set
	 */
	public void setBuildId(String buildId) {
		this.buildId = buildId;
	}
	/* (non-Javadoc)
	 * @see com.intel.crashtoolserver.bean.BuildKey#getKernelVersion()
	 */
	@Override
	public String getKernelVersion() {
		return kernelVersion;
	}
	/**
	 * @param kernelVersion the kernelVersion to set
	 */
	public void setKernelVersion(String kernelVersion) {
		this.kernelVersion = kernelVersion;
	}
	/**
	 * @return the modemVersion
	 */
	public String getModemVersion() {
		return modemVersion;
	}
	/**
	 * @param modemVersion the modemVersion to set
	 */
	public void setModemVersion(String modemVersion) {
		this.modemVersion = modemVersion;
	}
	/**
	 * @return the iafwVersion
	 */
	@Deprecated
	public String getIafwVersion() {
		return iafwVersion;
	}
	/**
	 * @param iafwVersion the iafwVersion to set
	 */
	@Deprecated
	public void setIafwVersion(String iafwVersion) {
		this.iafwVersion = iafwVersion;
	}
	/**
	 * @return the scufwVersion
	 */
	@Deprecated
	public String getScufwVersion() {
		return scufwVersion;
	}
	/**
	 * @param scufwVersion the scufwVersion to set
	 */
	@Deprecated
	public void setScufwVersion(String scufwVersion) {
		this.scufwVersion = scufwVersion;
	}
	/**
	 * @return the punitVersion
	 */
	@Deprecated
	public String getPunitVersion() {
		return punitVersion;
	}
	/**
	 * @param punitVersion the punitVersion to set
	 */
	@Deprecated
	public void setPunitVersion(String punitVersion) {
		this.punitVersion = punitVersion;
	}
	/**
	 * @return the valhooksVersion
	 */
	@Deprecated
	public String getValhooksVersion() {
		return valhooksVersion;
	}
	/**
	 * @param valhooksVersion the valhooksVersion to set
	 */
	@Deprecated
	public void setValhooksVersion(String valhooksVersion) {
		this.valhooksVersion = valhooksVersion;
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
	 * @return the date
	 */
	public Date getDate() {
		return date;
	}

	/**
	 * @param date the date to set
	 */
	public void setDate(Date date) {
		this.date = date;
	}

	/**
	 * @return the mainlineId
	 */
	@Deprecated
	public Long getMainlineId() {
		return mainlineId;
	}


	/**
	 * @param mainlineId the mainlineId to set
	 */
	@Deprecated
	public void setMainlineId(Long mainlineId) {
		this.mainlineId = mainlineId;
	}

	/**
	 * @return the mainline
	 */
	@Deprecated
	public Mainline getMainline() {
		return mainline;
	}

	/**
	 * @param mainline the mainline to set
	 */
	@Deprecated
	public void setMainline(Mainline mainline) {
		this.mainline = mainline;
	}

	/**
	 * @return the variant
	 */
	@Deprecated
	public String getVariant() {
		return variant;
	}

	/**
	 * @param variant the variant to set
	 */
	public void setVariant(String variant) {
		this.variant = variant;
	}

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}
	
	public String getIngredientsJson() {
		return ingredientsJson;
	}

	public void setIngredientsJson(String ingredientsJson) {
		this.ingredientsJson = ingredientsJson;
	}

	public String getUniqueKey() {
		return uniqueKey;
	}

	public void setUniqueKey(String uniqueKey) {
		this.uniqueKey = uniqueKey;
	}
	
	@Deprecated
	public String getUniqueKeyGenerationMethod() {
		return uniqueKeyGenerationMethod;
	}
	@Deprecated
	public void setUniqueKeyGenerationMethod(String uniqueKeyGenerationMethod) {
		this.uniqueKeyGenerationMethod = uniqueKeyGenerationMethod;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	public String getOs() {
		return os;
	}

	public void setOs(String os) {
		this.os = os;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
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

	public Ingredients getIngredients() {
		return ingredients;
	}

	public void setIngredients(Ingredients ingredients) {
		this.ingredients = ingredients;
	}
	

	public List<String> getUniqueKeyComponents() {
		return uniqueKeyComponents;
	}

	public void setUniqueKeyComponents(List<String> uniqueKeyComponents) {
		this.uniqueKeyComponents = uniqueKeyComponents;
	}
	
	/* (non-Javadoc)
	 * @see com.intel.crashtoolserver.bean.BuildKey#getUniqueKeyComponents()
	 */
	@Override
	public List<String> getUniqueKeyComponentsList() {
		return getUniqueKeyComponents();
	}
	
	public String getUniqueKeyComponentsString() {
		if (getUniqueKeyComponents() != null) {
			return uniqueKeyComponents.toString();
		}
		else {
			return Build.DEFAULT_VALUE;
		}		
	}

	public String getUniqueKeyFull() {
        return uniqueKeyFull;
    }


    public void setUniqueKeyFull(String uniqueKeyFull) {
        this.uniqueKeyFull = uniqueKeyFull;
    }
    
    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }
    
    public Modem getModem() {
        return modem;
    }


    public void setModem(Modem modem) {
        this.modem = modem;
    }


    public Modem getModemExt() {
        return modemExt;
    }


    public void setModemExt(Modem modemExt) {
        this.modemExt = modemExt;
    }
    
    @Override
    public IngredientsKey getIngredientsKey() {
    	return ingredients;
    }

	public String getMainlineStr() {
		return mainlineStr;
	}

	public void setMainlineStr(String mainlineStr) {
		this.mainlineStr = mainlineStr;
	}

	public String getTargetProduct() {
		return targetProduct;
	}

	public void setTargetProduct(String targetProduct) {
		this.targetProduct = targetProduct;
	}

	public String getTargetProductScalability() {
		return targetProductScalability;
	}

	public void setTargetProductScalability(String targetProductScalability) {
		this.targetProductScalability = targetProductScalability;
	}

	public String getProjectConfigName() {
		return projectConfigName;
	}

	public void setProjectConfigName(String projectConfigName) {
		this.projectConfigName = projectConfigName;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Build [id=" + id + ", buildId=" + buildId + ", name=" + name
				+ ", fingerPrint=" + fingerPrint + ", kernelVersion="
				+ kernelVersion + ", buildUserHostname=" + buildUserHostname
				+ ", modemVersion=" + modemVersion + ", ifwiVersion="
				+ ifwiVersion + ", iafwVersion=" + iafwVersion
				+ ", scufwVersion=" + scufwVersion + ", punitVersion="
				+ punitVersion + ", valhooksVersion=" + valhooksVersion
				+ ", variant=" + variant + ", type=" + type + ", os=" + os
				+ ", project=" + project + ", ingredientsJson="
				+ ingredientsJson + ", ingredients=" + ingredients
				+ ", uniqueKey=" + uniqueKey + ", uniqueKeyFull="
				+ uniqueKeyFull + ", organization=" + organization + ", modem="
				+ modem + ", modemExt=" + modemExt
				+ ", uniqueKeyGenerationMethod=" + uniqueKeyGenerationMethod
				+ ", uniqueKeyComponents=" + uniqueKeyComponents
				+ ", mainlineId=" + mainlineId + ", mainline=" + mainline
				+ ", date=" + date + ", mainlineStr=" + mainlineStr
				+ ", targetProduct=" + targetProduct
				+ ", targetProductScalability=" + targetProductScalability
				+ ", projectConfigName=" + projectConfigName + "]";
	}
}
