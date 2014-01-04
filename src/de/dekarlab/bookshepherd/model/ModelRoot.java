package de.dekarlab.bookshepherd.model;

import java.util.ArrayList;
import java.util.List;

import de.dekarlab.bookshepherd.Resource;

public class ModelRoot extends Element {

	private long startId;
	private List<ReferenceItem> refItems;
	private ReferenceGroup refGroup;
	private ExcerptGroup excerptGroup;
	private boolean assignId;

	public ModelRoot() {
		this.assignId = true;
		this.refItems = new ArrayList<ReferenceItem>();
		this.refGroup = createReferenceGroup();
		this.refGroup.setName(Resource.getText("tree.menu.groups"));
		this.excerptGroup = createExcerptGroup();
		this.excerptGroup.setName(Resource.getText("tree.excerpt"));
	}

	public boolean isAssignId() {
		return assignId;
	}

	public void setAssignId(boolean assignId) {
		this.assignId = assignId;
	}

	public ReferenceGroup getRefGroup() {
		return refGroup;
	}

	public void setRefGroup(ReferenceGroup refGroup) {
		this.refGroup = refGroup;
	}

	public ExcerptGroup getExcerptGroup() {
		return excerptGroup;
	}

	public void setExcerptGroup(ExcerptGroup excerptGroup) {
		this.excerptGroup = excerptGroup;
	}

	public void addRefItem(ReferenceItem ritem) {
		refItems.add(ritem);
	}

	public void removeRefItem(ReferenceItem ritem) {
		refItems.remove(ritem);
	}

	public int getRefItemCount() {
		return refItems.size();
	}

	public ReferenceItem getRefItem(int i) {
		return refItems.get(i);
	}

	protected long getNextId() {
		startId++;
		return startId;
	}

	public void setSatrtId(long startId) {
		if (this.startId < startId) {
			this.startId = startId;
		}
	}

	public ExcerptAttribute createExcerptAttribute() {
		ExcerptAttribute elem = new ExcerptAttribute();
		// if (assignId) {
		// elem.setId(getNextId());
		// }
		return elem;
	}

	public ExcerptAttribute createExcerptAttribute(String name, String value) {
		ExcerptAttribute elem = new ExcerptAttribute(name, value);
		// if (assignId) {
		// elem.setId(getNextId());
		// }
		return elem;
	}

	public ExcerptGroup createExcerptGroup() {
		ExcerptGroup elem = new ExcerptGroup();
		if (assignId) {
			elem.setId(getNextId());
		}
		return elem;
	}

	public ExcerptItem createExcerptItem() {
		ExcerptItem elem = new ExcerptItem();
		if (assignId) {
			elem.setId(getNextId());
		}
		return elem;
	}

	public ExcerptItemGroup createExcerptItemGroup() {
		ExcerptItemGroup elem = new ExcerptItemGroup();
		if (assignId) {
			elem.setId(getNextId());
		}
		return elem;
	}

	public ReferenceAttribute createReferenceAttribute() {
		ReferenceAttribute elem = new ReferenceAttribute();
		// if (assignId) {
		// elem.setId(getNextId());
		// }
		return elem;
	}

	public ReferenceAttribute createReferenceAttribute(String name, String value) {
		ReferenceAttribute elem = new ReferenceAttribute(name, value);
		// if (assignId) {
		// elem.setId(getNextId());
		// }
		return elem;
	}

	public ReferenceGroup createReferenceGroup() {
		ReferenceGroup elem = new ReferenceGroup();
		if (assignId) {
			elem.setId(getNextId());
		}
		return elem;
	}

	public ReferenceItem createReferenceItem() {
		ReferenceItem elem = new ReferenceItem();
		if (assignId) {
			elem.setId(getNextId());
		}
		return elem;
	}

	public ReferenceItemGroup createReferenceItemGroup() {
		ReferenceItemGroup elem = new ReferenceItemGroup();
		if (assignId) {
			elem.setId(getNextId());
		}
		return elem;
	}
}
