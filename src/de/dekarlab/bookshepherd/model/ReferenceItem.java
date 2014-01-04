package de.dekarlab.bookshepherd.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.dekarlab.bookshepherd.BSConstant;

public class ReferenceItem extends Element {
	private long id;
	private String name;
	private Set<ReferenceItemGroup> groups;
	private List<ReferenceAttribute> attributes;
	private Set<ExcerptItem> excerpts;

	protected ReferenceItem() {
		attributes = new ArrayList<ReferenceAttribute>();
		groups = new HashSet<ReferenceItemGroup>();
		excerpts = new HashSet<ExcerptItem>();
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void addGroup(ReferenceItemGroup group) {
		group.setItem(this);
		getGroups().add(group);
	}

	public void removeGroup(ReferenceGroup group) {
		for (ReferenceItemGroup lg : getGroups()) {
			if (lg.getGroup().getId() == group.getId()) {
				getGroups().remove(lg);
				break;
			}
		}
	}

	public Set<ReferenceItemGroup> getGroups() {
		return groups;
	}

	public void setGroups(Set<ReferenceItemGroup> groups) {
		this.groups = groups;
	}

	public List<ReferenceAttribute> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<ReferenceAttribute> attributes) {
		this.attributes = attributes;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void addAttribute(ReferenceAttribute attribute) {
		if (attributes == null) {
			attributes = new ArrayList<ReferenceAttribute>();
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
			attributes = new ArrayList<ReferenceAttribute>();
		}
		for (ReferenceAttribute attr : attributes) {
			if (attr.getName().equals(name)) {
				attributes.remove(attr);
				break;
			}
		}
		addAttribute(new ReferenceAttribute(name, value));
	}

	public String getAttribute(String key) {
		if (key.equals(BSConstant.ATTR_SHORT_NAME)) {
			return getName();
		}
		for (ReferenceAttribute attribute : attributes) {
			if (attribute.getName().equals(key)) {
				return attribute.getValue();
			}
		}
		return null;
	}

	public Set<ExcerptItem> getExcerpts() {
		return excerpts;
	}

	public void setExcerpts(Set<ExcerptItem> excerpts) {
		this.excerpts = excerpts;
	}

	public void removeItem(ExcerptItem item) {
		for (ExcerptItem lg : getExcerpts()) {
			if (lg.getId() == item.getId()) {
				lg.setReference(null);
				getExcerpts().remove(lg);
				break;
			}
		}
	}

}
