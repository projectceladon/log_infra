/* Phone Doctor (CLOTA)
 *
 * Copyright (C) Intel 2012
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Author: Jeremy Rocher <jeremyx.rocher@intel.com>
 */

package com.intel.crashreport.specific;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import android.content.Context;

import com.intel.crashreport.ApplicationPreferences;
import com.intel.crashreport.GeneralBuild;
import com.intel.crashreport.Log;
import com.intel.crashreport.specific.ingredients.FileIngredientBuilder;
import com.intel.crashreport.specific.ingredients.IngredientBuilder;
import com.intel.crashreport.specific.ingredients.IngredientManager;
import com.intel.crashreport.propconfig.PropertyConfigLoader;
import com.intel.crashreport.propconfig.bean.BuildAllowedValues;
import com.intel.phonedoctor.Constants;

public class Build extends GeneralBuild{

	private static final String PATH_MODEMID = Constants.LOGS_DIR + "/modemid.txt";
	public static final String INGREDIENTS_FILE_PATH = Constants.LOGS_DIR + "/ingredients.txt";
	private static BuildAllowedValues ALLOWED_VALUES = null;
	private Context ctx;

	public Build(String buildId, String fingerPrint, String kernelVersion, String buildUserHostname, String modemVersion,
			String ifwiVersion, String iafwVersion, String scufwVersion, String punitVersion, String valhooksVersion) {
		super(buildId, fingerPrint, kernelVersion, buildUserHostname, modemVersion,
				ifwiVersion, iafwVersion, scufwVersion, punitVersion, valhooksVersion);
		checkAllowedProperties();
		consolidateModemVersion();
	}

	public Build(String longBuildId) {
		super(longBuildId);
		if (longBuildId != null) {
			if (longBuildId.contains(",")) {
				checkAllowedProperties();
				consolidateModemVersion();
				checkBuild();
			}
		}
	}

	public Build(Context context) {
		super();
		ctx = context;
		checkAllowedProperties();
	}

	/**
	 * Returns this <code>GeneralBuild</code>'s <i>ingredients</i> value
	 * as JSON string.
	 * @return the ingredients as string.
	 */
	public static final String getIngredients() {
		// Create an ingredient builder
		IngredientBuilder builder = new FileIngredientBuilder(INGREDIENTS_FILE_PATH);
		// Retrieve the ingredients
		Map<String, String> ingredients = builder.getIngredients();
		IngredientManager.INSTANCE.storeLastIngredients(ingredients);

		// Create a StringBuilder for the final JSON string
		StringBuilder ingredientsBuffer = new StringBuilder("{");
		// Process the ingredients
		appendIngredients(ingredients, ingredientsBuffer);
		// Remove the last coma if any
		int lastComa = ingredientsBuffer.lastIndexOf(",");
		if(lastComa != -1) {
			ingredientsBuffer.deleteCharAt(lastComa);
		}
		// End the JSON string property
		ingredientsBuffer.append("}");
		// Return the JSON string
		return ingredientsBuffer.toString();
	}

	//for PDLite easy integration
	public static final String getUniqueKeyComponent() {
		return IngredientManager.INSTANCE.getUniqueKeyList().toString();
	}

	/**
	 * Appends the given ingredients to the given buffer.
	 * @param ingredients the ingredients to write to buffer
	 * @param buffer the buffer in which to write.
	 */
	private static void appendIngredients(Map<String, String> ingredients, StringBuilder buffer) {
		/* Do nothing if parameters are not valid. */
		if(ingredients == null || buffer == null) {
			return;
		}
		// Iterate on the ingredients
		for(String key : ingredients.keySet()) {
			// Add each ingredient value to the JSON string
			String value = ingredients.get(key);
			if(value == null) {
				value = "";
			}
			buffer.append("\"");
			buffer.append(key);
			buffer.append("\":\"");
			buffer.append(value);
			buffer.append("\",");
		}
	}


	public void fillBuildWithSystem() {
		this.setBuildId(android.os.Build.VERSION.INCREMENTAL);
		this.setFingerPrint(android.os.Build.FINGERPRINT);
		this.setKernelVersion(getProperty("sys.kernel.version"));
		this.setBuildUserHostname(getProperty("ro.build.user")+"@"+getProperty("ro.build.host"));
		this.setModemVersion(getProperty("gsm.version.baseband"));
		this.setIfwiVersion(getProperty("sys.ifwi.version"));
		this.setIafwVersion(getProperty("sys.ia32.version"));
		this.setScufwVersion(getProperty("sys.scu.version"));
		this.setPunitVersion(getProperty("sys.punit.version"));
		this.setValhooksVersion(getProperty("sys.valhooks.version"));
		checkAllowedProperties();
		consolidateModemVersion();
		consolidateBuildId();
		ApplicationPreferences prefs = new ApplicationPreferences(ctx);
		prefs.setBuild(super.toString());
	}

	/**
	 * This method applies the properties configuration to all
	 * properties.
	 *
	 * The static attribute <code>ALLOWED_VALUES</code> is updated
	 * only once if needed.
	 */
	private void checkAllowedProperties() {
		// Retrieve the build properties configuration if needed
		if(ALLOWED_VALUES == null) {
			PropertyConfigLoader loader = PropertyConfigLoader.getInstance();
			ALLOWED_VALUES = loader.getPropertiesConfiguration();
		}
		if(ALLOWED_VALUES != null) {
			// Apply the configuration
			List<BuildProperty> properties = this.listProperties();
			String[] configuredProperties = ALLOWED_VALUES.getConfiguredProperties();
			for(BuildProperty property : properties) {
				for(String configuredProperty : configuredProperties) {
					if(configuredProperty.equals(property.getName())) {
						// Update the property
						String[] allowedValues =
								ALLOWED_VALUES.getAllowedValuesForProperty(configuredProperty);
						property.setAllowedValues(Arrays.asList(allowedValues));
						// End this loop
						break;
					}
				}
			}
		}
	}

	private void consolidateModemVersion(){
		//for crashtool identification, we need to be sure modem version is present
		if (modemVersion.equals("")){
			//fill it with modemid.txt
			BufferedReader modemid = null;
			FileReader f = null;
			try {
				f = new FileReader(PATH_MODEMID);
				modemid = new BufferedReader(new FileReader(PATH_MODEMID));
				String sTmp = modemid.readLine();
				if (sTmp != null){
					modemVersion.setValue(sTmp);
				}
			} catch (FileNotFoundException e) {
				Log.w(" consolidateModemVersion :" + e.getMessage());
			} catch (IOException e) {
				Log.w(" consolidateModemVersion :" + e.getMessage());
			} finally {
				if (f != null) {
					try {
						f.close();
					} catch (IOException e) {
						Log.w(" consolidateModemVersion :" + e.getMessage());
					}
				}
				if (modemid != null) {
					try {
						modemid.close();
					} catch (IOException e) {
						Log.w(" consolidateModemVersion :" + e.getMessage());
					}
				}
			}
		}
	}

	private void consolidateBuildId() {
		if(isWrongValue(ifwiVersion) ||
				isWrongValue(iafwVersion) ||
				isWrongValue(scufwVersion) ||
				isWrongValue(punitVersion) ||
				isWrongValue(valhooksVersion) ) {
			ApplicationPreferences prefs = new ApplicationPreferences(ctx);
			String build = prefs.getBuild();
			if(!build.isEmpty()) {
				GeneralBuild oldBuild = new GeneralBuild(build);
				if(isWrongValue(ifwiVersion))
					ifwiVersion.setValue(oldBuild.getIfwiVersion());
				if(isWrongValue(iafwVersion))
					iafwVersion.setValue(oldBuild.getIafwVersion());
				if(isWrongValue(scufwVersion))
					scufwVersion.setValue(oldBuild.getScufwVersion());
				if(isWrongValue(punitVersion))
					punitVersion.setValue(oldBuild.getPunitVersion());
				if(isWrongValue(valhooksVersion))
					valhooksVersion.setValue(oldBuild.getValhooksVersion());
			}
		}
	}

	private void checkBuild() {
		if(isWrongValue(ifwiVersion))
			ifwiVersion.setValue(getProperty("sys.ifwi.version"));
		if(isWrongValue(iafwVersion))
			iafwVersion.setValue(getProperty("sys.ia32.version"));
		if(isWrongValue(scufwVersion))
			scufwVersion.setValue(getProperty("sys.scu.version"));
		if(isWrongValue(punitVersion))
			punitVersion.setValue(getProperty("sys.punit.version"));
		if(isWrongValue(valhooksVersion))
			valhooksVersion.setValue(getProperty("sys.valhooks.version"));
	}

	private boolean isWrongValue(BuildProperty property) {
		return property.isWrongValue();
	}

	public boolean testVersion() {
		checkAllowedProperties();
		return !(isWrongValue(ifwiVersion) ||
				isWrongValue(iafwVersion) ||
				isWrongValue(scufwVersion) ||
				isWrongValue(punitVersion) ||
				isWrongValue(valhooksVersion));
	}

}
