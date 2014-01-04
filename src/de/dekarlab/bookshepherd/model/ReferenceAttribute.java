package de.dekarlab.bookshepherd.model;

/**
 * Attribute class handles attributes for items. Attribute could be like author,
 * article, file name and so on.
 * 
 * 
 */
public class ReferenceAttribute extends Element {
	// private long id;
	/**
	 * Attribute name.
	 */
	private String name;
	/**
	 * Attribute value.
	 */
	private String value;
	/**
	 * Item, to which attribute belongs.
	 */
	private ReferenceItem item;

	protected ReferenceAttribute() {

	}

	protected ReferenceAttribute(String name, String value) {
		this.name = name;
		this.value = value;
	}

	public ReferenceItem getItem() {
		return item;
	}

	public void setItem(ReferenceItem item) {
		this.item = item;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	// public long getId() {
	// return id;
	// }
	//
	// public void setId(long id) {
	// this.id = id;
	// }

}
