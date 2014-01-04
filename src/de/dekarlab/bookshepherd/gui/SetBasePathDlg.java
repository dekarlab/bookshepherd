package de.dekarlab.bookshepherd.gui;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import de.dekarlab.bookshepherd.Property;
import de.dekarlab.bookshepherd.Resource;
import de.dekarlab.bookshepherd.controller.ModelController;

public class SetBasePathDlg {
	// private Button btUpdate1;
	// private Button btUpdate2;

	private Button btCancel;
	private Button btOk;

	private Button btBrowse;
	private Text path;
	private Property property;
	private Shell shell;

	// private ModelController controller;

	public SetBasePathDlg(Shell shell, Property property,
			ModelController controller) {
		this.property = property;
		this.shell = shell;
		// this.controller = controller;
	}

	public void init() {
		shell.setBounds(property.getEditDlgWindowX(), property
				.getEditDlgWindowY(), property.getEditDlgWindowW(), property
				.getEditDlgWindowH());
		shell.setText(Resource.getText("setbasepathdlg.title"));
		GridLayout gl = new GridLayout();
		gl.numColumns = 3;
		shell.setLayout(gl);
		shell.setLayoutData(new GridData(GridData.FILL_BOTH));

		// Label lbl1 = new Label(shell, SWT.NONE);
		// lbl1.setText(Resource.getText("setbasepathdlg.note1"));
		// btUpdate1 = new Button(shell, SWT.NONE);
		// btUpdate1.setText(Resource.getText("setbasepathdlg.bt.absolute"));
		// GridData gd = new GridData();
		// gd.horizontalSpan = 2;
		// lbl1.setLayoutData(gd);
		//
		// btUpdate1.addListener(SWT.Selection, new Listener() {
		//
		// public void handleEvent(Event arg0) {
		// // start bar
		// final Shell shellBar = new Shell(shell, SWT.APPLICATION_MODAL);
		// Rectangle b = shell.getBounds();
		// shellBar.setBounds((b.x + b.width - 250) / 2,
		// (b.y + b.height - 50) / 2, 250, 50);
		// shellBar.setLayout(new GridLayout());
		// new Label(shellBar, SWT.NONE).setText(Resource
		// .getText("setbasepathdlg.bar.update"));
		// final ProgressBar bar = new ProgressBar(shellBar,
		// SWT.INDETERMINATE);
		// shellBar.setText(Resource.getText("setbasepathdlg.bar.update"));
		//
		// bar.setLayoutData(new GridData(GridData.FILL_BOTH));
		// shellBar.open();
		// // create group to import
		// SetBasePath.updateBasePathAbsoulte(path.getText(), bar,
		// controller);
		// // import files to group
		// shellBar.getDisplay().syncExec(new Runnable() {
		// public void run() {
		// bar.setSelection(bar.getSelection() + 1);
		// }
		// });
		// shellBar.dispose();
		// }
		// });

		Label lbl3 = new Label(shell, SWT.NONE);
		lbl3.setText(Resource.getText("setbasepathdlg.path") + ":");
		GridData gd3 = new GridData();
		gd3.horizontalSpan = 3;
		lbl3.setLayoutData(gd3);
		path = new Text(shell, SWT.BORDER | SWT.READ_ONLY);
		String basePath = property.getBasePath();
		if (basePath == null) {
			basePath = "";
		}
		path.setText(basePath);
		GridData gd4 = new GridData(GridData.FILL_HORIZONTAL);
		gd4.horizontalSpan = 2;
		path.setLayoutData(gd4);

		btBrowse = new Button(shell, SWT.NONE);
		btBrowse.setText(Resource.getText("importdlg.bt.browse"));

		btBrowse.addListener(SWT.Selection, new Listener() {

			public void handleEvent(Event arg0) {
				DirectoryDialog fd = new DirectoryDialog(shell, SWT.OPEN);
				fd.setText(Resource.getText("setbasepathdlg.select"));
				fd.setFilterPath(property.getBasePath());
				final String selected = fd.open();
				if (selected == null) {
					return;
				}
				path.setText(selected);
			}

		});

		// Label lbl2 = new Label(shell, SWT.NONE);
		// lbl2.setText(Resource.getText("setbasepathdlg.note2"));
		// btUpdate2 = new Button(shell, SWT.NONE);
		// btUpdate2.setText(Resource.getText("setbasepathdlg.bt.relative"));
		// GridData gd2 = new GridData();
		// gd2.horizontalSpan = 2;
		// lbl2.setLayoutData(gd2);
		//
		// btUpdate2.addListener(SWT.Selection, new Listener() {
		//
		// public void handleEvent(Event arg0) {
		// // start bar
		// final Shell shellBar = new Shell(shell, SWT.APPLICATION_MODAL);
		// Rectangle b = shell.getBounds();
		// shellBar.setBounds((b.x + b.width - 250) / 2,
		// (b.y + b.height - 50) / 2, 250, 50);
		// shellBar.setLayout(new GridLayout());
		// new Label(shellBar, SWT.NONE).setText(Resource
		// .getText("setbasepathdlg.bar.update"));
		// final ProgressBar bar = new ProgressBar(shellBar,
		// SWT.INDETERMINATE);
		// shellBar.setText(Resource.getText("setbasepathdlg.bar.update"));
		//
		// bar.setLayoutData(new GridData(GridData.FILL_BOTH));
		// shellBar.open();
		// // create group to import
		// SetBasePath.updateBasePath(path.getText(), bar, controller);
		// // import files to group
		// shellBar.getDisplay().syncExec(new Runnable() {
		// public void run() {
		// bar.setSelection(bar.getSelection() + 1);
		// }
		// });
		// shellBar.dispose();
		// }
		// });

		btCancel = new Button(shell, SWT.NONE);
		btCancel.setText(Resource.getText("btn.cancel"));
		btCancel.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event arg0) {
				shell.close();
			}
		});

		btOk = new Button(shell, SWT.NONE);
		btOk.setText(Resource.getText("btn.ok"));
		btOk.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event arg0) {
				if (new File(path.getText()).exists()) {
					property.setBasePath(new File(path.getText())
							.getAbsolutePath());
					shell.close();
				}
			}
		});

	}

	public void updateProperty() {
		property.setEditDlgWindowH(shell.getBounds().height);
		property.setEditDlgWindowW(shell.getBounds().width);
		property.setEditDlgWindowX(shell.getBounds().x);
		property.setEditDlgWindowY(shell.getBounds().y);
	}

}
