package de.dekarlab.bookshepherd;

import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CustomAttributes {
	private Map<String, String[]> attrs;
	private static CustomAttributes instance;
	private int length;

	public static final String BUNDLE = "custom";

	private ResourceBundle bundle;

	private CustomAttributes() {

	}

	public static CustomAttributes getInstance() {
		if (instance == null) {
			instance = new CustomAttributes();
			try {
				instance.attrs = new HashMap<String, String[]>();
				instance.bundle = ResourceBundle.getBundle(BUNDLE);
				instance.load();
			} catch (Exception ex) {
				Logger.getLogger("bookshepherd").log(Level.SEVERE,
						ex.getMessage(), ex);

			}

		}
		return instance;
	}

	protected void load() {

		int i = 1;
		String name;
		String doctype;
		String[] attrNames;
		String[] newAttrNames;

		length = 0;
		while (true) {
			try {
				name = bundle.getString("prop." + i + ".name");
				doctype = bundle.getString("prop." + i + ".doctype");
			} catch (Exception ex) {
				name = null;
				doctype = null;
			}
			if (name == null || doctype == null) {
				break;
			}
			name = adaptCustom(name);
			// add new attribute
			attrNames = attrs.get(doctype);
			if (attrNames == null) {
				newAttrNames = new String[1];
			} else {
				newAttrNames = new String[attrNames.length + 1];
				System.arraycopy(attrNames, 0, newAttrNames, 0,
						attrNames.length);
			}

			newAttrNames[newAttrNames.length - 1] = name;
			attrs.put(doctype, newAttrNames);

			length++;
			i++;
		}
	}

	public int getLength() {
		return length;
	}

	public static final String adaptCustom(String name) {
		// for compability with bibtex
		name = name.toLowerCase();
		name = name.replace(" ", "_");
		name = name.replace(":", "_");
		name = name.replace(",", "_");
		name = name.replace(".", "_");
		// -----------------------------
		return name;
	}

	public String[] getPropName(String docType) {
		String[] ret = attrs.get(docType);
		if (ret == null) {
			ret = new String[0];
		}
		return ret;
	}
}
