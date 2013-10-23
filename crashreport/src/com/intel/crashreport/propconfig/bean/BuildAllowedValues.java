package com.intel.crashreport.propconfig.bean;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * A class that allows to manage allowed values
 * for properties of a given build.
 *
 */
public class BuildAllowedValues {
	private String build;

	@SerializedName("allowed_values")
	private PropertyValues[] allowedValues;

	public String getBuild() {
		return this.build;
	}

	public void setBuild(String build) {
		this.build = build;
	}

	public PropertyValues[] getAllowedValues() {
		return this.allowedValues;
	}

	public void setAllowedValues(PropertyValues[] allowedValues) {
		this.allowedValues = allowedValues;
	}

	public String[] getConfiguredProperties() {
		List<String> names = new ArrayList<String>();
		for(PropertyValues values : this.allowedValues) {
			names.add(values.getPropertyName());
		}
		return names.toArray(new String[names.size()]);
	}

	public String[] getAllowedValuesForProperty(String name) {
		String [] allowedValues = null;
		for(PropertyValues allowed : this.allowedValues) {
			if(allowed.getPropertyName().equals(name)) {
				allowedValues = allowed.getValues();
			}
		}
		return allowedValues;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("BuildAllowedValues[build:");
		sb.append(this.build);
		sb.append(", allowed_values:{");
		for(PropertyValues values : this.allowedValues) {
			sb.append(values);
			sb.append(",");
		}
		sb.append("}");
		sb.append("]");
		return sb.toString();
	}
}
