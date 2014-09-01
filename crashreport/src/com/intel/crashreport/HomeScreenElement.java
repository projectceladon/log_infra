/* Phone Doctor (CLOTA)
 *
 * hold element id, name and in the future: image resource id
 *
 * Author:  Nicolae Natea <nicolaex.natea@intel.com>
 *
 */
package com.intel.crashreport;

public class HomeScreenElement {

	private int elementID;
	private String elementName;
	private int elementImageResource;
	private int elementPosition;

	public HomeScreenElement(int id, String name) {
		this.elementID = id;
		this.elementName = name;
		this.elementImageResource = -1;
	}

	public HomeScreenElement(int id, String name, int imageResource, int position) {
		this(id, name);
		this.elementImageResource = imageResource;
		this.elementPosition = position;
	}

	public int getElementID() {
		return this.elementID;
	}

	public String getElementName() {
		return this.elementName;
	}

	public int getElementImageResource() {
		return this.elementImageResource;
	}

	public int getElementPosition() {
		return this.elementPosition;
	}
}
