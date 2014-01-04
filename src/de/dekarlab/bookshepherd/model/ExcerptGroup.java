package de.dekarlab.bookshepherd.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ExcerptGroup extends Element {
	private long id;
	private String name;

	private ExcerptGroup parent;
	private List<ExcerptGroup> children;

	private Set<ExcerptItemGroup> items;

	protected ExcerptGroup() {
		this.children = new ArrayList<ExcerptGroup>();
		this.items = new HashSet<ExcerptItemGroup>();
	}

	public void addChild(ExcerptGroup child) {
		addChild(child, -1);
	}

	public void addChild(ExcerptGroup child, int index) {
		child.setParent(this);
		if (index == -1) {
			getChildren().add(child);
		} else {
			getChildren().add(index, child);
		}
	}

	public void deleteChild(ExcerptGroup child) {
		for (ExcerptGroup gr : getChildren()) {
			if (gr.getId() == child.getId()) {
				getChildren().remove(gr);
				break;
			}
		}
	}

	public void addItem(ExcerptItemGroup group) {
		group.setGroup(this);
		getItems().add(group);
	}

	public void removeItem(ExcerptItem item) {
		for (ExcerptItemGroup lg : getItems()) {
			if (lg.getItem().getId() == item.getId()) {
				lg.setItem(null);
				getItems().remove(lg);
				break;
			}
		}
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

	public List<ExcerptGroup> getChildren() {
		if (children == null) {
			children = new ArrayList<ExcerptGroup>();
		}
		return children;
	}

	public void setChildren(List<ExcerptGroup> children) {
		this.children = children;
	}

	public ExcerptGroup getParent() {
		return parent;
	}

	public void setParent(ExcerptGroup parent) {
		this.parent = parent;
	}

	public Set<ExcerptItemGroup> getItems() {
		return items;
	}

	public void setItems(Set<ExcerptItemGroup> items) {
		this.items = items;
	}

	public ExcerptGroup findChildById(long id) {
		ExcerptGroup child;
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
