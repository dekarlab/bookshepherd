package de.dekarlab.bookshepherd;

import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import de.dekarlab.bookshepherd.gui.MainFrame;

/**
 * Library starter.
 * 
 */
public class BookShepherdApp {

	/**
	 * Start GUI.
	 * 
	 * @param arg
	 *            arg
	 */
	public static void main(String[] arg) {
		try {
			Handler fh = new FileHandler("bookshepherd.log");
			Logger.getLogger("bookshepherd").addHandler(fh);
			Logger.getLogger("bookshepherd").setLevel(Level.ALL);
			Logger.getLogger("bookshepherd").log(Level.INFO,
					"Starting Bookshepherd");
		} catch (Exception ex) {
			Logger.getLogger("bookshepherd").log(Level.SEVERE, ex.getMessage(),
					ex);

		}

		Display display = new Display();
		Display.setAppName(Resource.getText("appname"));
		Shell shell = new Shell(display);
		shell.setText(Resource.getText("title"));
		Property property = new Property();
		MainFrame mainFrame = null;
		try {
			property.loadProperty();
			mainFrame = new MainFrame(shell, property);
			mainFrame.init();
			shell.open();
			while (!shell.isDisposed()) {
				if (!display.isDisposed()) {
					if (!display.readAndDispatch())
						display.sleep();
				}
			}
			property.saveProperty();
		} catch (Exception e) {
			Logger.getLogger("bookshepherd").log(Level.SEVERE,
					"Error in Main: ", e);
		} finally {
			display.dispose();
		}
		Logger.getLogger("bookshepherd")
				.log(Level.INFO, "Exiting Bookshepherd");
	}

}
