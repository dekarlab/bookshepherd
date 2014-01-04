package de.dekarlab.bookshepherd.model;

public class ExcerptItemGroup extends Element {
	private long id;
	private ExcerptItem item;
	private ExcerptGroup group;

	/**
	 * Constructor.
	 */
	protected ExcerptItemGroup() {

	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public ExcerptItem getItem() {
		return item;
	}

	public void setItem(ExcerptItem item) {
		this.item = item;
	}

	public ExcerptGroup getGroup() {
		return group;
	}

	public void setGroup(ExcerptGroup group) {
		this.group = group;
	}

}
