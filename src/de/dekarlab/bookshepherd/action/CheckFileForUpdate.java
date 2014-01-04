package de.dekarlab.bookshepherd.action;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;

import de.dekarlab.bookshepherd.Resource;
import de.dekarlab.bookshepherd.gui.MainFrame;

public class CheckFileForUpdate {

	public static void checkFile(MainFrame mf) {
		if (mf.getController().getFile() == null) {
			return;
		}
		File file = new File(mf.getController().getFile());
		long lastModified = file.lastModified();
		if (mf.getController().getFileLastModified() != lastModified) {
			MessageBox messageBox = new MessageBox(mf.getShell(), SWT.OK
					| SWT.CANCEL);
			messageBox.setText(Resource.getText("message.fileupdated"));
			messageBox.setMessage(Resource.getText("message.fileupdated"));
			if (messageBox.open() == SWT.CANCEL) {
				mf.getController().setFileLastModified(lastModified);
			} else {
				MainFrame.openFile(mf, mf.getController().getFile());
			}
		}
	}

}
