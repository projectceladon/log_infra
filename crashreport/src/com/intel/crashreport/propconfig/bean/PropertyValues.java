package com.intel.crashreport.propconfig.bean;

public class PropertyValues {

	private String property_name;
	private String[] values;

	public PropertyValues() {
		this.property_name = "default";
		this.values = new String[]{};
	}

	public PropertyValues(String name) {
		this.property_name = name;
		this.values = new String[]{};
	}

	public PropertyValues(String name, String[] values) {
		this.property_name = name;
		this.values = values;
	}


	public String getPropertyName() {
		return this.property_name;
	}

	public void setPropertyName(String name) {
		this.property_name = name;
	}

	public String[] getValues() {
		return this.values;
	}

	public void setValues(String[] values) {
		this.values = values;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("PropertyValues[name:");
		sb.append(this.property_name);
		sb.append(", values:{");
		for(String value : this.values) {
			sb.append("\"");
			sb.append(value);
			sb.append("\"");
			sb.append(",");
		}
		sb.append("}]");
		return sb.toString();
	}
}
