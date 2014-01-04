package de.dekarlab.bookshepherd;

import java.util.ResourceBundle;

public class Resource {

	public static final String GUI_BUNDLE = "de.dekarlab.bookshepherd.gui";

	private static ResourceBundle bundle = ResourceBundle.getBundle(GUI_BUNDLE);

	public static String getText(String text) {
		return bundle.getString(text);
	}
}
