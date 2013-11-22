/* Phone Doctor (CLOTA)
 *
 * Copyright (C) Intel 2013
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
 * Author: Charles-Edouard Vidoine <charles.edouardx.vidoine@intel.com>
 */
package com.intel.crashreport;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import android.os.SystemProperties;

public class GeneralBuild {

	private static final int FIELD_NUMBER = 10;

	private static final String MODEM_VERSION_FILE = "/logs/modem_version.txt";

	/**
	 * The name of the property to search for when filling the
	 * <code>variant</code> property.
	 */
	public static final String VARIANT_PROPERTY_NAME = "ro.product.name";

	private static String VARIANT = null;

	protected final BuildProperty buildId = new BuildProperty("buildId");
	protected final BuildProperty fingerPrint = new BuildProperty("fingerPrint");
	protected final BuildProperty kernelVersion = new BuildProperty("kernelVersion");
	protected final BuildProperty buildUserHostname = new BuildProperty("buildUserHostname");
	protected final BuildProperty modemVersion = new BuildProperty("modemVersion");
	protected final BuildProperty ifwiVersion = new BuildProperty("ifwiVersion");
	protected final BuildProperty iafwVersion = new BuildProperty("iafwVersion");
	protected final BuildProperty scufwVersion = new BuildProperty("scufwVersion");
	protected final BuildProperty punitVersion = new BuildProperty("punitVersion", "");
	protected final BuildProperty valhooksVersion = new BuildProperty("valhooksVersion");

	private final List<BuildProperty> properties = new ArrayList<GeneralBuild.BuildProperty>();

	public GeneralBuild(String buildId, String fingerPrint, String kernelVersion, String buildUserHostname, String modemVersion,
			String ifwiVersion, String iafwVersion, String scufwVersion, String punitVersion, String valhooksVersion) {
		super();
		this.setBuildId(buildId);
		this.setFingerPrint(fingerPrint);
		this.setKernelVersion(kernelVersion);
		this.setBuildUserHostname(buildUserHostname);
		this.setModemVersion(modemVersion);
		this.setIfwiVersion(ifwiVersion);
		this.setIafwVersion(iafwVersion);
		this.setScufwVersion(scufwVersion);
		this.setPunitVersion(punitVersion);
		this.setValhooksVersion(valhooksVersion);
	}

	public GeneralBuild(String longBuildId) {
		if (longBuildId != null) {
			if (longBuildId.contains(",")) {
				String buildFields[] = longBuildId.split(",");
				if ((buildFields != null) && (buildFields.length == FIELD_NUMBER)) {
					this.setBuildId(buildFields[0]);
					this.setFingerPrint(buildFields[1]);
					this.setKernelVersion(buildFields[2]);
					this.setBuildUserHostname(buildFields[3]);
					this.setModemVersion(buildFields[4]);
					this.setIfwiVersion(buildFields[5]);
					this.setIafwVersion(buildFields[6]);
					this.setScufwVersion(buildFields[7]);
					this.setPunitVersion(buildFields[8]);
					this.setValhooksVersion(buildFields[9]);
				}
			}
		}
	}

	public GeneralBuild() {}

	public com.intel.crashtoolserver.bean.Build getBuildForServer() {
		return new com.intel.crashtoolserver.bean.Build(
				buildId.getValue(),
				fingerPrint.getValue(),
				kernelVersion.getValue(),
				buildUserHostname.getValue(),
				modemVersion.getValue(),
				ifwiVersion.getValue(),
				iafwVersion.getValue(),
				scufwVersion.getValue(),
				punitVersion.getValue(),
				valhooksVersion.getValue());
	}

	protected static String getProperty(String name) {
		try {
			String property = SystemProperties.get(name, "");
			return property;
		} catch (IllegalArgumentException e) {
			Log.w("Propery not available : "+name);
		}
		return "";
	}

	/**
	 * Returns the modem version (variant).
	 * @return the variant as String
	 */
	public static final String getVariant() {
		if(VARIANT == null) {
			String suffix = parseModemVersionFile();
			StringBuffer sBuffer = new StringBuffer(
					GeneralBuild.getProperty(VARIANT_PROPERTY_NAME));
			if(!"".equals(suffix)) {
				sBuffer.append("-");
				sBuffer.append(suffix);
			}
			VARIANT = sBuffer.toString();
		}
		return VARIANT;
	}

	/**
	 * Reads the MODEM_VERSION_FILE and returns its content.
	 * @return the content of MODEM_VERSION_FILE or ""
	 */
	private static final String parseModemVersionFile() {
		String version = "";
		BufferedReader reader = null;
		File modemVersionFile = new File(MODEM_VERSION_FILE);
		boolean fileExists = false;
		try {
			fileExists = modemVersionFile.exists();
			if(fileExists) {
				reader = new BufferedReader(
					new FileReader(MODEM_VERSION_FILE));
				version = reader.readLine();
			} else {
				Log.i("No file " + MODEM_VERSION_FILE + " on device.");
			}
		} catch(SecurityException securityException) {
			Log.w("Operation not allowed on file: " + MODEM_VERSION_FILE);
		} catch(IOException generalException) {
			Log.w("Could not read file: " + MODEM_VERSION_FILE);
		} finally {
			if(reader != null) {
				try {
					reader.close();
				} catch(IOException exceptionOnClose) {
					Log.w("An error occurred while closing file: " + MODEM_VERSION_FILE);
				}
			}
		}
		return version;
	}

	@Override
	public String toString() {
		return this.getBuildId() + "," + this.getFingerPrint() + "," +
				this.getKernelVersion() + "," + this.getBuildUserHostname() + "," +
				this.getModemVersion() + "," + this.getIfwiVersion() + "," +
				this.getIafwVersion() + "," + this.getScufwVersion() + "," +
				this.getPunitVersion() + "," + this.getValhooksVersion();
	}

	public String getBuildId() {
		return buildId.getValue();
	}

	public void setBuildId(String buildId) {
		this.buildId.setValue(buildId);
	}

	public String getFingerPrint() {
		return fingerPrint.getValue();
	}

	public void setFingerPrint(String fingerPrint) {
		this.fingerPrint.setValue(fingerPrint);
	}

	public String getKernelVersion() {
		return kernelVersion.getValue();
	}

	public void setKernelVersion(String kernelVersion) {
		this.kernelVersion.setValue(kernelVersion);
	}

	public String getBuildUserHostname() {
		return buildUserHostname.getValue();
	}

	public void setBuildUserHostname(String buildUserHostname) {
		this.buildUserHostname.setValue(buildUserHostname);
	}

	public String getModemVersion() {
		return modemVersion.getValue();
	}

	public void setModemVersion(String modemVersion) {
		this.modemVersion.setValue(modemVersion);
	}

	public String getIfwiVersion() {
		return ifwiVersion.getValue();
	}

	public void setIfwiVersion(String ifwiVersion) {
		this.ifwiVersion.setValue(ifwiVersion);
	}

	public String getIafwVersion() {
		return iafwVersion.getValue();
	}

	public void setIafwVersion(String iafwVersion) {
		this.iafwVersion.setValue(iafwVersion);
	}

	public String getScufwVersion() {
		return scufwVersion.getValue();
	}

	public void setScufwVersion(String scufwVersion) {
		this.scufwVersion.setValue(scufwVersion);
	}

	public String getPunitVersion() {
		return punitVersion.getValue();
	}

	public void setPunitVersion(String punitVersion) {
		this.punitVersion.setValue(punitVersion);
	}

	public String getValhooksVersion() {
		return valhooksVersion.getValue();
	}

	public void setValhooksVersion(String valhooksVersion) {
		this.valhooksVersion.setValue(valhooksVersion);
	}

	/**
	 * Returns a list of this object's properties.
	 * The return list contains all <code>BuildProperty</code> instance
	 * attached to this object.
	 *
	 * @return a <code>BuildProperty</code> list.
	 */
	public List<BuildProperty> listProperties() {
		// Update the list if it has not been initialized yet.
		// When initialized we expect at least one element in it.
		if(this.properties.size() == 0) {
			// Retrieve all fields declared by this class and super classes
			Field[] attributes = this.getAllFields(this.getClass());
			// Iterate on the fields to find those with the wanted type
			for(Field currentAttribute : attributes) {
				currentAttribute.setAccessible(true);
				if(currentAttribute.getType() == BuildProperty.class) {
					// If the current attribute has the correct class
					try {
						// Add the attribute to the result list
						this.properties.add((BuildProperty)currentAttribute.get(this));
					} catch (Exception reflectException) {
						continue;
					}
				}
			}
		}
		// Return the property list
		return this.properties;
	}

	/**
	 * Returns all <code>Fields</code> declared by the given <code>Class</code>
	 * and its super classes.
	 *
	 * @return a <code>Field[]</code> of all fields.
	 */
	private Field[] getAllFields(Class<?> kls) {
		// Initialize an empty array
		Field[] fields = new Field[0];
		// Do something only on relevant classes
		if(kls != null && kls != Object.class){
			// Recursively get the declared attributes of the super class
			Field[] myParentAttributes = this.getAllFields(kls.getSuperclass());
			// Get the declared attributes of this class
			Field[] myAttributes = kls.getDeclaredFields();
			// Merge the two Fields arrays
			fields = new Field[myAttributes.length + myParentAttributes.length];
			System.arraycopy(myAttributes, 0, fields, 0, myAttributes.length);
			System.arraycopy(myParentAttributes, 0, fields, myAttributes.length, myParentAttributes.length);
		}
		// Return the computed array
		return fields;
	}

	/**
	 * A class to handle build properties in a
	 * proper way.
	 *
	 * This class should make further development on properties
	 * easier (e.g: configuration files, etc).
	 *
	 */
	public static class BuildProperty {
		/**
		 * A global array of usually forbidden values.
		 */
		private static final String WRONG_VALUES[] = {"","00.00","0000.0000"};

		/**
		 * The property name.
		 */
		private String name;

		/**
		 * The property value.
		 */
		private String value;

		/**
		 * A list of values that are allowed for this property.
		 * Values contained in this list may override value
		 * stored in <code>WRONG_VALUES</code>.
		 */
		private List<String> allowedValues;

		public BuildProperty() {
			this.name = "default";
			this.value = "";
			this.allowedValues = new ArrayList<String>();

		}

		public BuildProperty(String name) {
			this.name = name;
			this.value = "";
			this.allowedValues = new ArrayList<String>();
		}

		public BuildProperty(String name, String value) {
			this.name = name;
			this.value = value;
			this.allowedValues = new ArrayList<String>();
		}

		public BuildProperty(String name, String value, List<String> allowedValues) {
			this.name = name;
			this.value = value;
			this.allowedValues = allowedValues;
		}

		public void allowValue(String value) {
			this.allowedValues.add(value);
		}

		public List<String> getAllowedValues() {
			return this.allowedValues;
		}

		public void setAllowedValues(List<String> values) {
			this.allowedValues = values;
		}

		public String getName() {
			return this.name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getValue() {
			return this.value;
		}

		public void setValue(String value) {
			this.value = value;
		}

		/**
		 * Checks whether this property value is valid or not.
		 * @return
		 * <ul>
		 * <li><code>true</code> if this property value shall be considered invalid.</li>
		 * <li><code>false</code> otherwise.</li>
		 * </ul>
		 */
		public boolean isWrongValue() {
			boolean isWrong = false;
			for(String value : this.allowedValues) {
				System.out.println("Allowed value: '" + value + "'");
			}
			if(this.allowedValues.contains(this.getValue())) {
				isWrong = false;
			} else {
				for(String currentValue : WRONG_VALUES) {
					if(currentValue.equals(this.getValue())) {
						isWrong = true;
						break;
					}
				}
			}
			return isWrong;
		}

		@Override
		public String toString() {
			StringBuffer sb = new StringBuffer();
			sb.append(this.getClass().getSimpleName());
			sb.append("[name:");
			sb.append(this.getName());
			sb.append(",value:");
			sb.append(this.getValue());
			sb.append("]");
			return sb.toString();
		}
	}
}
