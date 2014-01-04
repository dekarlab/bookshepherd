package de.dekarlab.bookshepherd.action;

import de.dekarlab.bookshepherd.controller.ModelController;
import de.dekarlab.bookshepherd.model.ModelRoot;

public class FileNew {

	public static void create(ModelController controller) {
		controller.setModelRoot(new ModelRoot());
		controller.setFile(null);
		controller.setModified(false);
	}
}
