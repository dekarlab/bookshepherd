package de.dekarlab.bookshepherd.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.dekarlab.bookshepherd.BSConstant;

/**
 * Knowledge Item.
 */
public class ExcerptItem extends Element {
	private long id;
	private String name;
	private Set<ExcerptItemGroup> groups;
	private List<ExcerptAttribute> attributes;
	private ReferenceItem reference;

	protected ExcerptItem() {
		attributes = new ArrayList<ExcerptAttribute>();
		groups = new HashSet<ExcerptItemGroup>();
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void addGroup(ExcerptItemGroup group) {
		group.setItem(this);
		getGroups().add(group);
	}

	public void removeGroup(ExcerptGroup group) {
		for (ExcerptItemGroup lg : getGroups()) {
			if (lg.getGroup().getId() == group.getId()) {
				getGroups().remove(lg);
				break;
			}
		}
	}

	public Set<ExcerptItemGroup> getGroups() {
		return groups;
	}

	public void setGroups(Set<ExcerptItemGroup> groups) {
		this.groups = groups;
	}

	public List<ExcerptAttribute> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<ExcerptAttribute> attributes) {
		this.attributes = attributes;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void addAttribute(ExcerptAttribute attribute) {
		if (attributes == null) {
			attributes = new ArrayList<ExcerptAttribute>();
		}
		attribute.setItem(this);
		attributes.add(attribute);
	}

	/**
	 * Update first occurnce of attribute with name.
	 * 
	 * @param name
	 * @param value
	 */
	public void setAttribute(String name, String value) {
		if (attributes == null) {
			attributes = new ArrayList<ExcerptAttribute>();
		}
		for (ExcerptAttribute attr : attributes) {
			if (attr.getName().equals(name)) {
				attr.setValue(value);
				return;
			}
		}
		addAttribute(new ExcerptAttribute(name, value));
	}

	public String getAttribute(String key) {
		if (key.equals(BSConstant.ATTR_SHORT_NAME)) {
			return getName();
		}
		for (ExcerptAttribute attribute : attributes) {
			if (attribute.getName().equals(key)) {
				return attribute.getValue();
			}
		}
		return null;
	}

	public ReferenceItem getReference() {
		return reference;
	}

	public void setReference(ReferenceItem reference) {
		this.reference = reference;
	}

}
