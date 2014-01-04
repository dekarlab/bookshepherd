package de.dekarlab.bookshepherd.action;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import de.dekarlab.bookshepherd.Resource;
import de.dekarlab.bookshepherd.controller.ModelController;
import de.dekarlab.bookshepherd.model.loader.XmlSaver;

public class FileSaveAs {
	public static void saveAs(Shell shell, ModelController controller) {
		FileDialog dialog = new FileDialog(shell, SWT.SAVE);
		dialog.setFilterNames(new String[] { "XML Files", "All Files (*.*)" });
		dialog.setFilterExtensions(new String[] { "*.xml", "*.*" });
		String selected = dialog.open();
		if (selected == null) {
			return;
		}

		if (!selected.endsWith(".xml")) {
			selected += ".xml";
		}

		if (new File(selected).exists()) {
			MessageBox messageBox = new MessageBox(shell, SWT.OK | SWT.CANCEL
					| SWT.ICON_QUESTION);
			messageBox.setText(Resource.getText("message.overwrite"));
			messageBox.setMessage(Resource.getText("message.overwrite"));
			if (messageBox.open() == SWT.CANCEL) {
				saveAs(shell, controller);
				return;
			}
		}

		StringBuffer out = XmlSaver.save(controller.getModelRoot());
		try {
			setContents(new File(selected), out.toString());
			controller.setFile(selected);
		} catch (FileNotFoundException e) {
			Logger.getLogger("bookshepherd").log(Level.SEVERE,
					e.getMessage(), e);			

		} catch (IOException e) {
			Logger.getLogger("bookshepherd").log(Level.SEVERE,
					e.getMessage(), e);			

		}
	}

	public static void setContents(File aFile, String aContents)
			throws FileNotFoundException, IOException {
		if (aFile == null) {
			throw new IllegalArgumentException("File should not be null.");
		}

		// use buffering
		Writer output = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(aFile), "UTF-8"));
		try {
			// FileWriter always assumes default encoding is OK!
			output.write(aContents);
		} finally {
			output.close();
		}
	}

}
