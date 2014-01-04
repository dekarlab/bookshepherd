package de.dekarlab.bookshepherd.model.loader;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.dekarlab.bookshepherd.model.ExcerptAttribute;
import de.dekarlab.bookshepherd.model.ExcerptGroup;
import de.dekarlab.bookshepherd.model.ExcerptItem;
import de.dekarlab.bookshepherd.model.ExcerptItemGroup;
import de.dekarlab.bookshepherd.model.ModelRoot;
import de.dekarlab.bookshepherd.model.ReferenceAttribute;
import de.dekarlab.bookshepherd.model.ReferenceGroup;
import de.dekarlab.bookshepherd.model.ReferenceItem;
import de.dekarlab.bookshepherd.model.ReferenceItemGroup;
import de.dekarlab.bookshepherd.util.Util;

public class XmlSaver {

	public static final String ExcerptAttribute = "ExcerptAttribute";
	public static final String ExcerptGroup = "ExcerptGroup";
	public static final String ExcerptItem = "ExcerptItem";
	public static final String ExcerptItemGroup = "ExcerptItemGroup";

	public static final String ReferenceAttribute = "ReferenceAttribute";
	public static final String ReferenceGroup = "ReferenceGroup";
	public static final String ReferenceItem = "ReferenceItem";
	public static final String ReferenceItemGroup = "ReferenceItemGroup";

	public static final String ModelRoot = "ModelRoot";

	public static StringBuffer createStartTag(String name, int indent,
			List<String> attrNames, List<String> attrValues) {
		StringBuffer out = new StringBuffer();
		for (int i = 0; i < indent; i++) {
			out.append(" ");
		}
		out.append("<");
		out.append(name);
		out.append(" ");
		if (attrNames != null) {
			for (int i = 0; i < attrNames.size(); i++) {
				out.append(attrNames.get(i));
				out.append("=\"");
				out.append(attrValues.get(i));
				out.append("\" ");
			}
		}
		out.append(">");
		if (!name.equals(ReferenceAttribute) && !name.equals(ExcerptAttribute)) {
			out.append("\n");
		}
		return out;
	}

	public static StringBuffer createCData(String value, int indent) {
		StringBuffer out = new StringBuffer();
		out.append("<![CDATA[");
		out.append(value);
		out.append("]]>");
		return out;
	}

	public static StringBuffer createEndTag(String name, int indent) {
		StringBuffer out = new StringBuffer();
		if (!name.equals(ReferenceAttribute) && !name.equals(ExcerptAttribute)) {
			for (int i = 0; i < indent; i++) {
				out.append(" ");
			}
		}
		out.append("</");
		out.append(name);
		out.append(">\n");
		return out;
	}

	public static StringBuffer save(ModelRoot mr) {
		StringBuffer out = new StringBuffer();
		int indent = 0;
		out.append(createStartTag(ModelRoot, indent, null, null));
		indent++;
		// save groups
		saveReferenceGroup(mr.getRefGroup(), out, indent);
		saveExcerptGroup(mr.getExcerptGroup(), out, indent);
		// save items
		for (int i = 0; i < mr.getRefItemCount(); i++) {
			saveReferenceItem(mr.getRefItem(i), out, indent);
		}
		indent--;
		out.append(createEndTag(ModelRoot, indent));
		return out;
	}

	public static void saveExcerptGroup(ExcerptGroup obj, StringBuffer out,
			int indent) {
		List<String> attrNames = new ArrayList<String>();
		List<String> attrValues = new ArrayList<String>();

		attrNames.add("id");
		attrValues.add(String.valueOf(obj.getId()));

		attrNames.add("name");
		attrValues.add(Util.xmlEncode(String.valueOf(obj.getName())));

		out.append(createStartTag(ExcerptGroup, indent, attrNames, attrValues));
		indent++;
		List<ExcerptGroup> children = obj.getChildren();
		for (int i = 0; i < children.size(); i++) {
			saveExcerptGroup(children.get(i), out, indent);
		}
		indent--;
		out.append(createEndTag(ExcerptGroup, indent));
	}

	public static void saveReferenceGroup(ReferenceGroup obj, StringBuffer out,
			int indent) {
		List<String> attrNames = new ArrayList<String>();
		List<String> attrValues = new ArrayList<String>();

		attrNames.add("id");
		attrValues.add(String.valueOf(obj.getId()));

		attrNames.add("name");
		attrValues.add(Util.xmlEncode(String.valueOf(obj.getName())));

		out
				.append(createStartTag(ReferenceGroup, indent, attrNames,
						attrValues));
		indent++;
		List<ReferenceGroup> children = obj.getChildren();
		for (int i = 0; i < children.size(); i++) {
			saveReferenceGroup(children.get(i), out, indent);
		}
		indent--;
		out.append(createEndTag(ReferenceGroup, indent));
	}

	public static void saveExcerptItem(ExcerptItem obj, StringBuffer out,
			int indent) {
		List<String> attrNames = new ArrayList<String>();
		List<String> attrValues = new ArrayList<String>();

		attrNames.add("id");
		attrValues.add(String.valueOf(obj.getId()));

		attrNames.add("name");
		attrValues.add(Util.xmlEncode(String.valueOf(obj.getName())));

		out.append(createStartTag(ExcerptItem, indent, attrNames, attrValues));
		indent++;

		List<ExcerptAttribute> attrs = obj.getAttributes();
		for (int i = 0; i < attrs.size(); i++) {
			saveExcerptAttribute(attrs.get(i), out, indent);
		}
		Iterator<ExcerptItemGroup> groups = obj.getGroups().iterator();
		while (groups.hasNext()) {
			saveExcerptItemGroupRef(groups.next(), out, indent);
		}

		indent--;
		out.append(createEndTag(ExcerptItem, indent));
	}

	public static void saveExcerptAttribute(ExcerptAttribute obj,
			StringBuffer out, int indent) {
		List<String> attrNames = new ArrayList<String>();
		List<String> attrValues = new ArrayList<String>();

		// attrNames.add("id");
		// attrValues.add(String.valueOf(obj.getId()));

		attrNames.add("name");
		attrValues.add(Util.xmlEncode(String.valueOf(obj.getName())));
		if (obj.getValue() != null && !obj.getValue().equals("")) {
			out.append(createStartTag(ExcerptAttribute, indent, attrNames,
					attrValues));
			indent++;
			out.append(createCData(obj.getValue(), indent));
			indent--;
			out.append(createEndTag(ExcerptAttribute, indent));
		}

	}

	public static void saveReferenceAttribute(ReferenceAttribute obj,
			StringBuffer out, int indent) {
		List<String> attrNames = new ArrayList<String>();
		List<String> attrValues = new ArrayList<String>();

		// attrNames.add("id");
		// attrValues.add(String.valueOf(obj.getId()));

		attrNames.add("name");
		attrValues.add(Util.xmlEncode(String.valueOf(obj.getName())));

		if (obj.getValue() != null && !obj.getValue().equals("")) {
			out.append(createStartTag(ReferenceAttribute, indent, attrNames,
					attrValues));
			indent++;
			out.append(createCData(obj.getValue(), indent));
			indent--;
			out.append(createEndTag(ReferenceAttribute, indent));
		}
	}

	public static void saveExcerptItemGroupRef(ExcerptItemGroup obj,
			StringBuffer out, int indent) {
		List<String> attrNames = new ArrayList<String>();
		List<String> attrValues = new ArrayList<String>();

		attrNames.add("id");
		attrValues.add(String.valueOf(obj.getGroup().getId()));

		out.append(createStartTag(ExcerptItemGroup, indent, attrNames,
				attrValues));
		out.append(createEndTag(ExcerptItemGroup, indent));
	}

	public static void saveReferenceItem(ReferenceItem obj, StringBuffer out,
			int indent) {
		List<String> attrNames = new ArrayList<String>();
		List<String> attrValues = new ArrayList<String>();

		attrNames.add("id");
		attrValues.add(String.valueOf(obj.getId()));

		attrNames.add("name");
		attrValues.add(Util.xmlEncode(String.valueOf(obj.getName())));

		out
				.append(createStartTag(ReferenceItem, indent, attrNames,
						attrValues));
		indent++;
		List<ReferenceAttribute> attributes = obj.getAttributes();
		for (int i = 0; i < attributes.size(); i++) {
			saveReferenceAttribute(attributes.get(i), out, indent);
		}

		Iterator<ReferenceItemGroup> groups = obj.getGroups().iterator();
		while (groups.hasNext()) {
			saveReferenceItemGroupRef(groups.next(), out, indent);
		}
		Iterator<ExcerptItem> excerpts = obj.getExcerpts().iterator();
		while (excerpts.hasNext()) {
			saveExcerptItem(excerpts.next(), out, indent);
		}
		indent--;
		out.append(createEndTag(ReferenceItem, indent));
	}

	public static void saveReferenceItemGroupRef(ReferenceItemGroup obj,
			StringBuffer out, int indent) {
		List<String> attrNames = new ArrayList<String>();
		List<String> attrValues = new ArrayList<String>();

		attrNames.add("id");
		attrValues.add(String.valueOf(obj.getGroup().getId()));

		out.append(createStartTag(ReferenceItemGroup, indent, attrNames,
				attrValues));
		out.append(createEndTag(ReferenceItemGroup, indent));
	}

}
