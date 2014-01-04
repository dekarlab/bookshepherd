package de.dekarlab.bookshepherd.model;

public class ExcerptAttribute extends Element {
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
	private ExcerptItem item;

	protected ExcerptAttribute() {

	}

	protected ExcerptAttribute(String name, String value) {
		this.name = name;
		this.value = value;
	}

	public ExcerptItem getItem() {
		return item;
	}

	public void setItem(ExcerptItem item) {
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
