package de.dekarlab.bookshepherd.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Group helps organize items.
 * 
 * 
 */
public class ReferenceGroup extends Element {
	private long id;
	private String name;

	private ReferenceGroup parent;
	private List<ReferenceGroup> children;

	private Set<ReferenceItemGroup> items;

	protected ReferenceGroup() {
		this.children = new ArrayList<ReferenceGroup>();
		this.items = new HashSet<ReferenceItemGroup>();

	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<ReferenceGroup> getChildren() {
		if (children == null) {
			children = new ArrayList<ReferenceGroup>();
		}
		return children;
	}

	public void setChildren(List<ReferenceGroup> children) {
		this.children = children;
	}

	public ReferenceGroup getParent() {
		return parent;
	}

	public void setParent(ReferenceGroup parent) {
		this.parent = parent;
	}

	public void addItem(ReferenceItemGroup group) {
		group.setGroup(this);
		getItems().add(group);
	}

	public void removeItem(ReferenceItem item) {
		for (ReferenceItemGroup lg : getItems()) {
			if (lg.getItem().getId() == item.getId()) {
				getItems().remove(lg);
				break;
			}
		}
	}

	public Set<ReferenceItemGroup> getItems() {
		return items;
	}

	public void setItems(Set<ReferenceItemGroup> items) {
		this.items = items;
	}

	public void addChild(ReferenceGroup child) {
		addChild(child, -1);
	}

	public void addChild(ReferenceGroup child, int index) {
		child.setParent(this);
		if (index == -1) {
			getChildren().add(child);
		} else {
			getChildren().add(index, child);
		}
	}

	public void deleteChild(ReferenceGroup child) {
		for (ReferenceGroup gr : getChildren()) {
			if (gr.getId() == child.getId()) {
				getChildren().remove(gr);
				break;
			}
		}
	}

	public ReferenceGroup findChildById(long id) {
		ReferenceGroup child;
		for (int i = 0; i < getChildren().size(); i++) {
			child = getChildren().get(i);
			if (child.getId() == id) {
				return child;
			}
			child = child.findChildById(id);
			if (child != null) {
				return child;
			}
		}
		return null;
	}

}
