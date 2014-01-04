package de.dekarlab.bookshepherd.action;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

import de.dekarlab.bookshepherd.controller.ModelController;
import de.dekarlab.bookshepherd.model.ModelRoot;
import de.dekarlab.bookshepherd.model.loader.XmlLoader;

public class FileOpen {

	public static boolean open(ModelController controller, Shell shell,
			String fileIn) {
		String selected;
		if (fileIn == null) {
			FileDialog dialog = new FileDialog(shell, SWT.OPEN);
			dialog
					.setFilterNames(new String[] { "XML Files",
							"All Files (*.*)" });
			dialog.setFilterExtensions(new String[] { "*.xml", "*.*" });
			selected = dialog.open();
			if (selected == null) {
				return false;
			}
		} else {
			selected = fileIn;
		}
		File file = new File(selected);
		if (!file.exists()) {
			return false;
		}
		try {
			ModelRoot modelRoot = XmlLoader.load(selected);
			controller.setModelRoot(modelRoot);
			controller.setFile(selected);
			controller.setModified(false);
		} catch (Exception e) {
			Logger.getLogger("bookshepherd").log(Level.SEVERE,
					"Error in open file.", e);
			return false;
		}
		return true;
	}
}
