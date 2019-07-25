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

package com.intel.crashreport.specific;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import android.content.Context;
import java.io.File;

import com.intel.crashreport.ApplicationPreferences;
import com.intel.crashreport.core.GeneralBuild;
import com.intel.crashreport.Log;
import com.intel.crashreport.specific.ingredients.FileIngredientBuilder;
import com.intel.crashreport.specific.ingredients.IngredientBuilder;
import com.intel.crashreport.specific.ingredients.IngredientManager;
import com.intel.crashreport.propconfig.PropertyConfigLoader;
import com.intel.crashreport.propconfig.bean.BuildAllowedValues;
import com.intel.phonedoctor.Constants;

import org.json.JSONObject;
import android.os.SystemProperties;

public class Build extends GeneralBuild{

	public static final String INGREDIENTS_FILE_PATH = Constants.LOGS_DIR + "/ingredients.txt";
	private static BuildAllowedValues ALLOWED_VALUES = null;
	private Context ctx;

	public Build(String buildId, String fingerPrint, String kernelVersion, String buildUserHostname) {
		super(buildId, fingerPrint, kernelVersion, buildUserHostname);
		checkAllowedProperties();
	}

	public Build(String longBuildId) {
		super(longBuildId);
		if (longBuildId != null) {
			if (longBuildId.contains(",")) {
				checkAllowedProperties();
			}
		}
	}

	public Build(Context context) {
		super();
		ctx = context;
		checkAllowedProperties();
	}

	public static String getProperty(String name) {
		try {
			return SystemProperties.get(name, "");
		} catch (IllegalArgumentException e) {
			Log.d("Propery not available : "+name);
		}
		return "";
	}

	/**
	 * Returns the modem version (variant).
	 * @return the variant as String
	 */
	public static final String getVariant() {
		if(VARIANT == null) {
			String suffix = getProperty(SWCONF_PROPERTY_NAME);
			StringBuffer sBuffer = new StringBuffer(
					getProperty(VARIANT_PROPERTY_NAME));
			if(!suffix.isEmpty()) {
				sBuffer.append("-");
				sBuffer.append(suffix);
			}
			VARIANT = sBuffer.toString();
		}
		return VARIANT;
	}

	/**
	 * Returns this <code>GeneralBuild</code>'s <i>ingredients</i> value
	 * as JSON string.
	 * @return the ingredients as string.
	 */
	public static final String getIngredients() {
		JSONObject ingredients;
		//First check existence of file
		File ingFile = new File(INGREDIENTS_FILE_PATH);
		if (!ingFile.exists()) {
			// should return a default value in case
			// ingredients are not activated
			ingredients = IngredientManager.INSTANCE.getDefaultIngredients();
		} else {
			// Create an ingredient builder
			IngredientBuilder builder = new FileIngredientBuilder(INGREDIENTS_FILE_PATH);
			// Retrieve the ingredients
			ingredients = builder.getIngredients();
		}
		IngredientManager.INSTANCE.storeLastIngredients(ingredients);

		// Return the JSON string
		return ingredients.toString();
	}

	//for PDLite easy integration
	public static final String getUniqueKeyComponent() {
		return IngredientManager.INSTANCE.getUniqueKeyList().toString();
	}

	public void fillBuildWithSystem() {
		this.setBuildId(android.os.Build.VERSION.INCREMENTAL);
		this.setFingerPrint(android.os.Build.FINGERPRINT);
		this.setKernelVersion(getProperty("sys.kernel.version"));
		this.setBuildUserHostname(getProperty("ro.build.user")+"@"+getProperty("ro.build.host"));
		this.setOs(OS_VALUE);
		checkAllowedProperties();
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
}
