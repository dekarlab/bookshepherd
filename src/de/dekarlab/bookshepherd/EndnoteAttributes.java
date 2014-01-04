package de.dekarlab.bookshepherd;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EndnoteAttributes {
	private Map<String, String> attrs;
	private static EndnoteAttributes instance;
	private int length;

	public static final String BUNDLE = "endnote";

	private ResourceBundle bundle;

	private EndnoteAttributes() {

	}

	public static EndnoteAttributes getInstance() {
		if (instance == null) {
			instance = new EndnoteAttributes();
			try {
				instance.bundle = ResourceBundle.getBundle(BUNDLE);				
				instance.attrs = new HashMap<String, String>();
				instance.load();
			} catch (Exception ex) {
				Logger.getLogger("bookshepherd").log(Level.SEVERE, ex.getMessage(),
						ex);

			}

		}
		return instance;
	}

	protected void load() {
		Enumeration<String> en = instance.bundle.getKeys();
		String name;
		while (en.hasMoreElements()) {
			name = en.nextElement();
			attrs.put(CustomAttributes.adaptCustom(name), instance.bundle
					.getString(name));
		}
	}

	public int getLength() {
		return length;
	}

	public String getPropName(String docType, String bibtex) {
		String ret = attrs.get(docType + "__" + bibtex);
		return ret;
	}
}
