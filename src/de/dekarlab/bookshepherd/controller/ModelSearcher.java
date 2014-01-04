package de.dekarlab.bookshepherd.controller;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import de.dekarlab.bookshepherd.model.ExcerptAttribute;
import de.dekarlab.bookshepherd.model.ExcerptGroup;
import de.dekarlab.bookshepherd.model.ExcerptItem;
import de.dekarlab.bookshepherd.model.ExcerptItemGroup;
import de.dekarlab.bookshepherd.model.ReferenceAttribute;
import de.dekarlab.bookshepherd.model.ReferenceGroup;
import de.dekarlab.bookshepherd.model.ReferenceItem;
import de.dekarlab.bookshepherd.model.ReferenceItemGroup;

public class ModelSearcher {

	public static List<ReferenceItem> searchReferenceItem(
			List<ReferenceItem> list, String searchStr) {
		searchStr = searchStr.toLowerCase();
		Iterator<ReferenceItem> it = list.iterator();
		List<ReferenceItem> listIn = new ArrayList<ReferenceItem>();
		ReferenceItem item;
		while (it.hasNext()) {
			item = it.next();
			if (hasFoundWords(searchStr, item)) {
				listIn.add(item);
			}
		}
		return listIn;
	}

	public static List<ReferenceItem> searchReferenceItem(ReferenceGroup group,
			String searchStr) {
		searchStr = searchStr.toLowerCase();
		List<ReferenceItem> list = new ArrayList<ReferenceItem>();
		Iterator<ReferenceItemGroup> it = group.getItems().iterator();
		ReferenceItem item;
		while (it.hasNext()) {
			item = it.next().getItem();
			if (hasFoundWords(searchStr, item)) {
				list.add(item);
			}
		}
		return list;
	}

	protected static boolean hasFoundWords(String searchStr, ReferenceItem item) {
		StringTokenizer strt = new StringTokenizer(searchStr, " ", false);
		while (strt.hasMoreTokens()) {
			if (!hasFound(strt.nextToken(), item)) {
				return false;
			}
		}
		return true;
	}

	protected static boolean hasFound(String searchStr, ReferenceItem item) {
		if (item.getName() != null
				&& item.getName().toLowerCase().indexOf(searchStr) != -1) {
			return true;
		}
		Iterator<ReferenceAttribute> it = item.getAttributes().iterator();
		while (it.hasNext()) {
			if (it.next().getValue().toLowerCase().indexOf(searchStr) != -1) {
				return true;
			}
		}
		return false;
	}

	public static List<ExcerptItem> searchExcerptItem(List<ExcerptItem> list,
			String searchStr) {
		searchStr = searchStr.toLowerCase();
		Iterator<ExcerptItem> it = list.iterator();
		List<ExcerptItem> listIn = new ArrayList<ExcerptItem>();
		ExcerptItem item;
		while (it.hasNext()) {
			item = it.next();
			if (hasFoundWords(searchStr, item)) {
				listIn.add(item);
			}
		}
		return listIn;
	}

	protected static boolean hasFoundWords(String searchStr, ExcerptItem item) {
		StringTokenizer strt = new StringTokenizer(searchStr, " ", false);
		while (strt.hasMoreTokens()) {
			if (!hasFound(strt.nextToken(), item)) {
				return false;
			}
		}
		return true;
	}

	protected static boolean hasFound(String searchStr, ExcerptItem item) {

		if (item.getName() != null
				&& item.getName().toLowerCase().indexOf(searchStr) != -1) {
			return true;
		}
		Iterator<ExcerptAttribute> it = item.getAttributes().iterator();
		while (it.hasNext()) {
			if (it.next().getValue().toLowerCase().indexOf(searchStr) != -1) {
				return true;
			}
		}
		return false;
	}

	public static List<ExcerptItem> searchExcerptItem(ExcerptGroup group,
			List<ExcerptItem> list, String searchStr) {
		searchStr = searchStr.toLowerCase();
		List<ExcerptItem> listN = new ArrayList<ExcerptItem>();
		Iterator<ExcerptItemGroup> it = group.getItems().iterator();
		ExcerptItem item;
		while (it.hasNext()) {
			item = it.next().getItem();
			if (hasFoundWords(searchStr, item) && list.contains(item)) {
				listN.add(item);
			}
		}
		return listN;
	}

}
