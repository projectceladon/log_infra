package com.intel.crashreport.propconfig;

import java.io.IOException;
import java.io.InputStreamReader;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.intel.crashreport.propconfig.bean.BuildAllowedValues;
import com.intel.phonedoctor.utils.FileOps;

/**
 * A class to load build properties allowed value properly
 * from a JSON configuration file.
 */
public class PropertyConfigLoader {

	/**
	 * The singleton.
	 */
	private static PropertyConfigLoader INSTANCE = null;

	/**
	 * The JSON configuration file.
	 */
	private static final String PROP_CONFIG_FILE_LOCATION = "/system/etc/build_properties.json";

	/**
	 * Private constructor.
	 * To retrieve an initialized instance, use
	 * <code>getInstance</code> method.
	 */
	private PropertyConfigLoader() {
	}

	/**
	 * Returns the instance of this class to use.
	 * @param c the Context.
	 * @return an initialized <code>PropertyConfigLoader</code> instance.
	 */
	public static synchronized PropertyConfigLoader getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new PropertyConfigLoader();
		}
		return INSTANCE;
	}

	/**
	 * Returns the object containing the configuration of allowed
	 * values for all properties of this build.
	 * @return a <code>BuildAllowedValues</code> or <code>null</code>
	 *	 	if an error occurred.
	 */
	public BuildAllowedValues getPropertiesConfiguration() {
		BuildAllowedValues config = BuildAllowedValues.empty();
		InputStreamReader reader = null;
		try {
			reader = FileOps.getInputStreamReader(PROP_CONFIG_FILE_LOCATION);
			if(reader != null) {
				Gson gson = new GsonBuilder().create();
				config = gson.fromJson(
					reader,
					BuildAllowedValues.class);
				reader.close();
			} else {
				Log.w("PhoneDoctor", "Got null InputStreamReader when opening " + PROP_CONFIG_FILE_LOCATION + ".");
			}
		} catch (IOException e) {
			Log.e("PhoneDoctor", "An exception occurred while reading " + PROP_CONFIG_FILE_LOCATION + ".");
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					Log.w("PhoneDoctor", "IOException : " + e.getMessage());
				}
			}
		}
		if(config != null) {
				Log.d("PhoneDoctor", "Returning configuration: " + config.toString());
		} else {
				Log.d("PhoneDoctor", "Got <null> configuration.");
		}
		return config;
	}
}
