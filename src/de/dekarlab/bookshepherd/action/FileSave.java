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

import org.eclipse.swt.widgets.Shell;

import de.dekarlab.bookshepherd.controller.ModelController;
import de.dekarlab.bookshepherd.model.loader.XmlSaver;

public class FileSave {
	public static void save(ModelController controller, Shell shell) {
		StringBuffer out = XmlSaver.save(controller.getModelRoot());
		try {
			setContents(new File(controller.getFile()), out.toString());
			// time of file update
			controller.setFile(controller.getFile());
			controller.setModified(false);
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
