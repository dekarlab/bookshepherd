package de.dekarlab.bookshepherd.gui;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeItem;

import de.dekarlab.bookshepherd.Property;
import de.dekarlab.bookshepherd.Resource;
import de.dekarlab.bookshepherd.action.ImportFiles;
import de.dekarlab.bookshepherd.controller.ModelController;
import de.dekarlab.bookshepherd.model.ReferenceGroup;
import de.dekarlab.bookshepherd.model.ReferenceItem;

/**
 * Import dialog.
 * 
 */
public class ImportFileDlg {

	private Combo format;
	private Button btImport;
	private Button btCancel;
	private Button btBrowse;
	private Button btSetBasePath;
	private Label basePath;

	private Text path;
	private Property property;
	private Shell shell;
	private List<String> formats;
	private ReferencePart referencePart;
	private ModelController controller;

	public ImportFileDlg(Shell shell, Property property,
			ReferencePart referencePart, ModelController controller) {
		this.property = property;
		this.shell = shell;
		this.referencePart = referencePart;
		this.controller = controller;
	}

	public void init() {
		shell.setBounds(property.getEditDlgWindowX(), property
				.getEditDlgWindowY(), property.getEditDlgWindowW(), property
				.getEditDlgWindowH());
		shell.setText(Resource.getText("importdlg.title"));
		GridLayout gl = new GridLayout();
		gl.numColumns = 3;
		shell.setLayout(gl);
		shell.setLayoutData(new GridData(GridData.FILL_BOTH));

		new Label(shell, SWT.NONE).setText(Resource.getText("importdlg.path")
				+ ":");
		path = new Text(shell, SWT.BORDER | SWT.READ_ONLY);
		path.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		path.setText(property.getFileDlgFilterPath());

		btBrowse = new Button(shell, SWT.NONE);
		btBrowse.setText(Resource.getText("importdlg.bt.browse"));
		btBrowse.addListener(SWT.Selection, new Listener() {

			public void handleEvent(Event arg0) {
				DirectoryDialog fd = new DirectoryDialog(shell, SWT.OPEN);
				fd.setText(Resource.getText("importdlg.select"));
				fd.setFilterPath(property.getFileDlgFilterPath());
				final String selected = fd.open();
				if (selected == null) {
					return;
				}
				property.setFileDlgFilterPath(new File(selected)
						.getAbsolutePath());
				path.setText(selected);
			}

		});

		new Label(shell, SWT.NONE).setText(Resource
				.getText("setbasepathdlg.path")
				+ ":");
		basePath = new Label(shell, SWT.NONE);
		basePath.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		basePath.setText(property.getBasePath());

		btSetBasePath = new Button(shell, SWT.NONE);
		btSetBasePath.setText(Resource.getText("setbasepathdlg.title"));
		btSetBasePath.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event arg0) {
				Shell shellDlg = new Shell(shell, SWT.APPLICATION_MODAL
						| SWT.SHELL_TRIM);
				final SetBasePathDlg dlg = new SetBasePathDlg(shellDlg,
						property, controller);
				shellDlg.addListener(SWT.Close, new Listener() {
					public void handleEvent(Event arg0) {
						dlg.updateProperty();
						basePath.setText(property.getBasePath());
					}
				});
				dlg.init();
				shellDlg.open();
			}

		});

		new Label(shell, SWT.NONE).setText(Resource.getText("importdlg.format")
				+ ":");
		format = new Combo(shell, SWT.BORDER | SWT.READ_ONLY);
		String tmp;
		int f = 0;
		formats = new ArrayList<String>();
		while (true) {
			try {
				tmp = Resource.getText("importdlg.format." + f);
				if (tmp != null) {
					formats.add(tmp);
				} else {
					break;
				}
				f++;
			} catch (Exception e) {
				// stop if there is no more formats
				break;
			}
		}
		format.setItems(formats.toArray(new String[0]));
		format.select(0);
		format.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		btImport = new Button(shell, SWT.NONE);
		btImport.setText(Resource.getText("importdlg.bt.import"));

		btImport.addListener(SWT.Selection, new Listener() {

			public void handleEvent(Event arg0) {
				// start bar
				final Shell shellBar = new Shell(shell, SWT.APPLICATION_MODAL);
				Rectangle b = shell.getBounds();
				shellBar.setBounds((b.x + b.width - 250) / 2,
						(b.y + b.height - 50) / 2, 250, 50);
				shellBar.setLayout(new GridLayout());
				new Label(shellBar, SWT.NONE).setText(Resource
						.getText("importdlg.bar.import"));
				final ProgressBar bar = new ProgressBar(shellBar,
						SWT.INDETERMINATE);
				shellBar.setText(Resource.getText("importdlg.bar.import"));

				bar.setLayoutData(new GridData(GridData.FILL_BOTH));
				shellBar.open();
				// create group to import
				ReferenceGroup group = controller.getModelRoot()
						.createReferenceGroup();
				SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd-hh_mm");
				group.setName(df.format(new Date()));
				TreeItem parentGui = referencePart.getTree().getItem(0);
				ReferenceGroup parent = (ReferenceGroup) parentGui
						.getData(ReferencePart.TREE_ATTR_ITEM);
				parent.addChild(group);
				// referencePart.addToTree(parentGui, group, -1);
				// import files to group
				shellBar.getDisplay().syncExec(new Runnable() {
					public void run() {
						bar.setSelection(bar.getSelection() + 1);
					}
				});

				List<ReferenceItem> list = ImportFiles.importFiles(group,
						new File(path.getText()), format.getSelectionIndex(),
						property.getBasePath(), controller.getItems(),
						controller, true);
				referencePart.updateTreeFromModel();
				bar.setSelection(bar.getSelection() + 1);
				controller.insert(list);
				referencePart.setItems(controller.getItems());
				referencePart.updateTableFromModel();
				shellBar.dispose();
			}
		});

		btCancel = new Button(shell, SWT.NONE);
		btCancel.setText(Resource.getText("importdlg.bt.close"));
		btCancel.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event arg0) {
				shell.close();
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
