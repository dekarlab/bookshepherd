package de.dekarlab.bookshepherd.controller;

import de.dekarlab.bookshepherd.model.Element;

public interface ModelListener {
	void inserted(Element[] elems);
	void removed(Element[] elems);
	void changed(Element[] elems);

}
