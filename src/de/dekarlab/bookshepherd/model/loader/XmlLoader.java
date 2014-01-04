package de.dekarlab.bookshepherd.model.loader;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import de.dekarlab.bookshepherd.model.Element;
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

public class XmlLoader extends DefaultHandler {
	private Element rootElem = null;
	private ModelRoot modelRoot;
	private Stack<Object> stack;
	private String cdata;

	public XmlLoader() {
		stack = new Stack<Object>();
	}

	public static ModelRoot load(String file) throws Exception {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser parser = factory.newSAXParser();
		XmlLoader handler = new XmlLoader();
		parser.parse(new File(file), handler);
		return handler.getModelRoot();
	}

	public ModelRoot getModelRoot() {
		return modelRoot;
	}

	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		if (qName.equals(XmlSaver.ModelRoot)) {
			modelRoot = new ModelRoot();
			// don#t assign ids for new elements
			modelRoot.setAssignId(false);
			stack.push(modelRoot);
			if (rootElem == null) {
				rootElem = modelRoot;
			}
		} else if (qName.equals(XmlSaver.ExcerptAttribute)) {
			ExcerptAttribute elem = modelRoot.createExcerptAttribute();
			ExcerptItem item = (ExcerptItem) stack.peek();
			// elem.setId(Long.parseLong(attributes.getValue("id")));
			// modelRoot.setSatrtId(elem.getId());
			elem.setName(Util.xmlDecode(attributes.getValue("name")));
			elem.setItem(item);
			item.addAttribute(elem);
			cdata = "";
			stack.push(elem);
			if (rootElem == null) {
				rootElem = elem;
			}
		} else if (qName.equals(XmlSaver.ExcerptGroup)) {
			ExcerptGroup parent = null;
			ExcerptGroup elem = modelRoot.createExcerptGroup();
			elem.setId(Long.parseLong(attributes.getValue("id")));
			modelRoot.setSatrtId(elem.getId());
			elem.setName(Util.xmlDecode(attributes.getValue("name")));
			if (stack.peek() instanceof ExcerptGroup) {
				parent = (ExcerptGroup) stack.peek();
				parent.addChild(elem);
			} else {
				((ModelRoot) stack.peek()).setExcerptGroup(elem);
			}
			stack.push(elem);
			if (rootElem == null) {
				rootElem = elem;
			}
		} else if (qName.equals(XmlSaver.ExcerptItem)) {
			ExcerptItem elem = modelRoot.createExcerptItem();
			ReferenceItem refItem = (ReferenceItem) stack.peek();
			elem.setId(Long.parseLong(attributes.getValue("id")));
			modelRoot.setSatrtId(elem.getId());
			elem.setName(Util.xmlDecode(attributes.getValue("name")));
			refItem.getExcerpts().add(elem);
			elem.setReference(refItem);
			stack.push(elem);
			if (rootElem == null) {
				rootElem = elem;
			}
		} else if (qName.equals(XmlSaver.ReferenceAttribute)) {
			ReferenceAttribute elem = modelRoot.createReferenceAttribute();
			ReferenceItem item = (ReferenceItem) stack.peek();
			// elem.setId(Long.parseLong(attributes.getValue("id")));
			// modelRoot.setSatrtId(elem.getId());
			elem.setName(Util.xmlDecode(attributes.getValue("name")));
			elem.setItem(item);
			item.addAttribute(elem);
			cdata = "";
			stack.push(elem);
			if (rootElem == null) {
				rootElem = elem;
			}
		} else if (qName.equals(XmlSaver.ReferenceGroup)) {
			ReferenceGroup parent = null;
			ReferenceGroup elem = modelRoot.createReferenceGroup();
			elem.setId(Long.parseLong(attributes.getValue("id")));
			modelRoot.setSatrtId(elem.getId());
			elem.setName(Util.xmlDecode(attributes.getValue("name")));
			if (stack.size() != 0) {
				if (stack.peek() instanceof ReferenceGroup) {
					parent = (ReferenceGroup) stack.peek();
					parent.addChild(elem);
				} else {
					((ModelRoot) stack.peek()).setRefGroup(elem);
				}
			}
			stack.push(elem);
			if (rootElem == null) {
				rootElem = elem;
			}
		} else if (qName.equals(XmlSaver.ReferenceItem)) {
			ReferenceItem elem = modelRoot.createReferenceItem();
			elem.setId(Long.parseLong(attributes.getValue("id")));
			modelRoot.setSatrtId(elem.getId());
			elem.setName(Util.xmlDecode(attributes.getValue("name")));
			((ModelRoot) stack.peek()).addRefItem(elem);
			stack.push(elem);
			if (rootElem == null) {
				rootElem = elem;
			}
		} else if (qName.equals(XmlSaver.ExcerptItemGroup)) {
			ExcerptItemGroup elem = modelRoot.createExcerptItemGroup();
			elem.setId(Long.parseLong(attributes.getValue("id")));
			modelRoot.setSatrtId(elem.getId());
			ExcerptItem excItem = (ExcerptItem) stack.peek();
			ExcerptGroup excGroup = modelRoot.getExcerptGroup().findChildById(
					Long.parseLong(attributes.getValue("id")));
			if (excGroup == null) {
				Logger.getLogger("bookshepherd").log(
						Level.WARNING,
						"ExcerptItemGroup with id: "
								+ attributes.getValue("id") + " is not found.");
			}
			elem.setGroup(excGroup);
			elem.setItem(excItem);
			excItem.getGroups().add(elem);
			excGroup.getItems().add(elem);
			stack.push(elem);
			if (rootElem == null) {
				rootElem = elem;
			}
		} else if (qName.equals(XmlSaver.ReferenceItemGroup)) {
			ReferenceItemGroup elem = modelRoot.createReferenceItemGroup();
			elem.setId(Long.parseLong(attributes.getValue("id")));
			modelRoot.setSatrtId(elem.getId());
			ReferenceItem refItem = (ReferenceItem) stack.peek();
			ReferenceGroup refGroup = modelRoot.getRefGroup().findChildById(
					Long.parseLong(attributes.getValue("id")));
			if (refGroup == null) {
				Logger.getLogger("bookshepherd").log(
						Level.WARNING,
						"ReferenceItemGroup with id: "
								+ attributes.getValue("id") + " is not found.");
			}
			refItem.addGroup(elem);
			refGroup.addItem(elem);
			stack.push(elem);
			if (rootElem == null) {
				rootElem = elem;
			}
		}
	}

	public void characters(char[] ch, int start, int length)
			throws SAXException {
		cdata += new String(ch, start, length);
	}

	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if (qName.equals(XmlSaver.ExcerptAttribute)) {
			((ExcerptAttribute) stack.peek()).setValue(cdata);
		} else if (qName.equals(XmlSaver.ReferenceAttribute)) {
			((ReferenceAttribute) stack.peek()).setValue(cdata);
		} else if (qName.equals(XmlSaver.ModelRoot)) {
			// switch on
			modelRoot.setAssignId(true);
		}
		stack.pop();
	}

	public static Element parse(String content, ModelRoot modelRoot)
			throws Exception {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser parser = factory.newSAXParser();
		XmlLoader handler = new XmlLoader();
		handler.modelRoot = modelRoot;
		InputStream io = new ByteArrayInputStream(content.getBytes());
		parser.parse(io, handler);
		return handler.rootElem;
	}
}
