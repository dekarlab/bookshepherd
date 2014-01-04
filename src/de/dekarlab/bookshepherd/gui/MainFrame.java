package de.dekarlab.bookshepherd.gui;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.TreeItem;

import de.dekarlab.bookshepherd.Property;
import de.dekarlab.bookshepherd.Resource;
import de.dekarlab.bookshepherd.action.CheckFileForUpdate;
import de.dekarlab.bookshepherd.action.ExportBibTex;
import de.dekarlab.bookshepherd.action.ExportEndNote;
import de.dekarlab.bookshepherd.action.ExportHtml;
import de.dekarlab.bookshepherd.action.FileNew;
import de.dekarlab.bookshepherd.action.FileOpen;
import de.dekarlab.bookshepherd.action.FileSave;
import de.dekarlab.bookshepherd.action.FileSaveAs;
import de.dekarlab.bookshepherd.action.ImportBibTex;
import de.dekarlab.bookshepherd.action.RenameFiles;
import de.dekarlab.bookshepherd.controller.ModelController;
import de.dekarlab.bookshepherd.controller.ModelListener;
import de.dekarlab.bookshepherd.model.Element;
import de.dekarlab.bookshepherd.model.ReferenceGroup;
import de.dekarlab.bookshepherd.model.ReferenceItem;
import de.dekarlab.bookshepherd.util.Util;

public class MainFrame {

	private Shell shell;
	private Menu menu;
	private Property property;
	private Group referenceGroup;
	private Group excerptGroup;
	private SashForm referenceSF;
	private SashForm excerptSF;

	private TabFolder folder;

	private ModelController controller;

	private ReferencePart referencePart;
	private ExcerptPart excerptPart;

	public MainFrame(Shell shell, Property property) {
		this.property = property;
		this.shell = shell;
	}

	public void updateTitle() {
		shell.setText(Util.getTitle(controller.getFile(),
				controller.isModified()));
	}

	public void init() {
		controller = new ModelController();
		// add check every minute
		final MainFrame mf = this;
		final int time = 1000 * 30;
		Runnable timer = new Runnable() {
			public void run() {
				CheckFileForUpdate.checkFile(mf);
				shell.getDisplay().timerExec(time, this);
			}
		};
		shell.getDisplay().timerExec(time, timer);

		controller.addListener(new ModelListener() {
			public void changed(Element[] elems) {
				controller.setModified(true);
				updateTitle();
			}

			public void inserted(Element[] elems) {
				controller.setModified(true);
				updateTitle();
			}

			public void removed(Element[] elems) {
				controller.setModified(true);
				updateTitle();
			}
		});

		shell.addListener(SWT.Close, new OnClose());
		updateTitle();
		GridLayout gl = new GridLayout();
		gl.numColumns = 2;
		shell.setLayout(gl);
		createMenuBar();

		folder = new TabFolder(shell, SWT.NONE);
		folder.setLayoutData(new GridData(GridData.FILL_BOTH));
		TabItem referenceItem = new TabItem(folder, SWT.NONE);
		TabItem excerptItem = new TabItem(folder, SWT.NONE);
		referenceItem.setText(Resource.getText("tab.reference"));
		excerptItem.setText(Resource.getText("tab.excerpt"));

		referencePart = new ReferencePart(this, property, controller, shell);
		referenceGroup = new Group(folder, SWT.SHADOW_IN);
		referenceGroup.setLayout(new GridLayout());
		referencePart.createSearch(referenceGroup);
		referenceSF = new SashForm(referenceGroup, SWT.HORIZONTAL);
		referenceSF.setLayout(new FillLayout());
		referenceSF.setLayoutData(new GridData(GridData.FILL_BOTH));
		referenceItem.setControl(referenceGroup);

		referencePart.createTree(referenceSF);
		referencePart.createTable(referenceSF);
		referenceSF.setWeights(new int[] { property.getRefSashWTree(),
				property.getRefSashWTable() });

		excerptPart = new ExcerptPart(property, controller, shell, this);
		excerptGroup = new Group(folder, SWT.SHADOW_IN);
		excerptGroup.setLayout(new GridLayout());
		excerptPart.createSearch(excerptGroup);
		excerptSF = new SashForm(excerptGroup, SWT.HORIZONTAL);
		excerptSF.setLayout(new FillLayout());
		excerptSF.setLayoutData(new GridData(GridData.FILL_BOTH));
		excerptItem.setControl(excerptGroup);
		excerptPart.createTree(excerptSF);
		excerptPart.createTable(excerptSF);
		excerptSF.setWeights(new int[] { property.getExcSashWTree(),
				property.getExcSashWTable() });

		shell.pack();
		shell.setBounds(property.getWindowX(), property.getWindowY(),
				property.getWindowW(), property.getWindowH());

		openFile(this, property.getLastOpenedFile());

	}

	protected void createMenuBar() {
		menu = new Menu(shell, SWT.BAR);
		shell.setMenuBar(menu);
		createFileMenu();
		createImportMenu();
		createExportMenu();
		createToolsMenu();
	}

	protected void createExportMenu() {
		Menu exportMenu = new Menu(shell, SWT.DROP_DOWN);
		MenuItem exportMenuItem = new MenuItem(menu, SWT.CASCADE);
		exportMenuItem.setText(Resource.getText("menu.export"));
		exportMenuItem.setMenu(exportMenu);

		MenuItem endNoteMenuItem = new MenuItem(exportMenu, SWT.CASCADE);
		endNoteMenuItem.setText(Resource.getText("menu.export.endnote"));

		MenuItem bibTexMenuItem = new MenuItem(exportMenu, SWT.CASCADE);
		bibTexMenuItem.setText(Resource.getText("menu.export.bibtex"));

		MenuItem htmlMenuItem = new MenuItem(exportMenu, SWT.CASCADE);
		htmlMenuItem.setText(Resource.getText("menu.export.html"));

		htmlMenuItem.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event arg0) {
				//export sorted
				ExportHtml.export(
						controller.getItems(referencePart.getSortField()),
						shell);
				
				//ExportHtml.export(referencePart.getItems(), shell);
			}
		});

		endNoteMenuItem.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event arg0) {
				ExportEndNote.exportEndNote(controller.getItemsSortedById(),
						shell);
			}
		});

		bibTexMenuItem.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event arg0) {
				//export sorted
				ExportBibTex.export(
						controller.getItems(referencePart.getSortField()),
						shell);
				// ExportBibTex.export(referencePart.getItems(), shell);
			}
		});

	}

	protected void createToolsMenu() {
		Menu toolsMenu = new Menu(shell, SWT.DROP_DOWN);
		MenuItem toolsMenuItem = new MenuItem(menu, SWT.CASCADE);
		toolsMenuItem.setText(Resource.getText("menu.tools"));
		toolsMenuItem.setMenu(toolsMenu);

		MenuItem renameMenuItem = new MenuItem(toolsMenu, SWT.CASCADE);
		renameMenuItem.setText(Resource.getText("menu.tools.rename"));

		renameMenuItem.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event arg0) {
				RenameFiles.renameFiles(referencePart.getItems(),
						property.getBasePath());
			}
		});

	}

	protected void createImportMenu() {
		Menu importMenu = new Menu(shell, SWT.DROP_DOWN);
		MenuItem importMenuItem = new MenuItem(menu, SWT.CASCADE);
		importMenuItem.setText(Resource.getText("menu.import"));
		importMenuItem.setMenu(importMenu);

		MenuItem setBasePathMenuItem = new MenuItem(importMenu, SWT.CASCADE);
		setBasePathMenuItem.setText(Resource
				.getText("menu.library.setbasepath"));

		// MenuItem updateGsMenuItem = new MenuItem(importMenu, SWT.CASCADE);
		// updateGsMenuItem.setText(Resource.getText("menu.library.updategs"));

		new MenuItem(importMenu, SWT.SEPARATOR);

		MenuItem importLibraryFilesMenuItem = new MenuItem(importMenu,
				SWT.CASCADE);
		importLibraryFilesMenuItem.setText(Resource
				.getText("menu.library.import.files"));

		final Shell shelLoc = shell;
		importLibraryFilesMenuItem.addListener(SWT.Selection, new Listener() {

			public void handleEvent(Event arg0) {
				Shell shellDlg = new Shell(shelLoc, SWT.APPLICATION_MODAL
						| SWT.SHELL_TRIM);
				final ImportFileDlg dlg = new ImportFileDlg(shellDlg, property,
						referencePart, controller);
				shellDlg.addListener(SWT.Close, new Listener() {
					public void handleEvent(Event arg0) {
						dlg.updateProperty();
					}
				});
				dlg.init();
				shellDlg.open();

			}
		});

		setBasePathMenuItem.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event arg0) {
				Shell shellDlg = new Shell(shelLoc, SWT.APPLICATION_MODAL
						| SWT.SHELL_TRIM);
				final SetBasePathDlg dlg = new SetBasePathDlg(shellDlg,
						property, controller);
				shellDlg.addListener(SWT.Close, new Listener() {
					public void handleEvent(Event arg0) {
						dlg.updateProperty();
					}
				});
				dlg.init();
				shellDlg.open();

			}
		});

		// updateGsMenuItem.addListener(SWT.Selection, new Listener() {
		// public void handleEvent(Event arg0) {
		// GSSearch.update(referencePart.getItems());
		// referencePart.setPerformSearch(false);
		// referencePart.updateTableFromModel();
		// referencePart.setPerformSearch(true);
		// }
		// });

		MenuItem bibTexMenuItem = new MenuItem(importMenu, SWT.CASCADE);
		bibTexMenuItem.setText(Resource.getText("menu.import.bibtex"));

		bibTexMenuItem.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event arg0) {
				FileDialog dialog = new FileDialog(shell, SWT.OPEN);
				dialog.setFilterNames(new String[] { "TeX Bib Files",
						"All Files (*.*)" });
				dialog.setFilterExtensions(new String[] { "*.bib", "*.*" });
				String selected = dialog.open();
				if (selected == null) {
					return;
				}
				// create group to import
				ReferenceGroup group = controller.getModelRoot()
						.createReferenceGroup();
				SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd-hh_mm");
				group.setName(df.format(new Date()));
				TreeItem parentGui = referencePart.getTree().getItem(0);
				ReferenceGroup parent = (ReferenceGroup) parentGui
						.getData(ReferencePart.TREE_ATTR_ITEM);
				parent.addChild(group);
				referencePart.addToTree(parentGui, group, -1);
				referencePart.setPerformSearch(false);
				List<ReferenceItem> list = ImportBibTex.importBibTex(group,
						new File(selected), controller);
				controller.insert(list);
				referencePart.setItems(controller.getItems());
				referencePart.updateTableFromModel();
				referencePart.setPerformSearch(true);
			}
		});

	}

	public ModelController getController() {
		return controller;
	}

	public Shell getShell() {
		return shell;
	}

	public static void openFile(MainFrame mainFrame, String file) {
		if (FileOpen
				.open(mainFrame.getController(), mainFrame.getShell(), file)) {
			mainFrame.getReferencePart().setRoot(
					mainFrame.getController().getReferenceTree());
			mainFrame.getExcerptPart().setRoot(
					mainFrame.getController().getExcerptTree());

			mainFrame.getReferencePart().updateTreeFromModel();
			mainFrame.getExcerptPart().updateTreeFromModel();

			List<ReferenceItem> list = mainFrame.getController().getItems(
					mainFrame.getReferencePart().getSortField());
			mainFrame.getReferencePart().setItems(list);
			mainFrame.getReferencePart().updateTableFromModel();
			mainFrame.getExcerptPart().setItems(
					mainFrame.getController().getExcerptItems(list,
							mainFrame.getExcerptPart().getSortField()));
			mainFrame.getExcerptPart().updateTableFromModel();

			mainFrame.getExcerptPart().setPerformSearch(false);
			mainFrame.getExcerptPart().getSearchExc().setText("");
			mainFrame.getExcerptPart().setPerformSearch(true);

			mainFrame.getReferencePart().setPerformSearch(false);
			mainFrame.getReferencePart().getSearchRef().setText("");
			mainFrame.getReferencePart().setPerformSearch(true);
			mainFrame.updateTitle();
		}
	}

	protected void createFileMenu() {
		Menu fileMenu = new Menu(shell, SWT.DROP_DOWN);
		MenuItem fileMenuItem = new MenuItem(menu, SWT.CASCADE);
		fileMenuItem.setText(Resource.getText("menu.file"));
		fileMenuItem.setMenu(fileMenu);

		MenuItem newFileMenuItem = new MenuItem(fileMenu, SWT.CASCADE);
		newFileMenuItem.setText(Resource.getText("menu.file.new"));
		newFileMenuItem.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				new OnClose().handleEvent(event);
				if (!event.doit) {
					return;
				}

				FileNew.create(controller);

				List<ReferenceItem> refs = controller.getItems(referencePart
						.getSortField());
				referencePart.setItems(refs);
				referencePart.updateTableFromModel();
				excerptPart.setItems(controller.getExcerptItems(refs,
						excerptPart.getSortField()));
				excerptPart.updateTableFromModel();

				referencePart.setRoot(controller.getReferenceTree());
				referencePart.updateTreeFromModel();
				excerptPart.setRoot(controller.getExcerptTree());
				excerptPart.updateTreeFromModel();

				excerptPart.setPerformSearch(false);
				excerptPart.getSearchExc().setText("");
				excerptPart.setPerformSearch(true);

				referencePart.setPerformSearch(false);
				referencePart.getSearchRef().setText("");
				referencePart.setPerformSearch(true);

				updateTitle();
			}
		});

		MenuItem openFileMenuItem = new MenuItem(fileMenu, SWT.CASCADE);
		openFileMenuItem.setText(Resource.getText("menu.file.open"));
		final MainFrame mf = this;
		openFileMenuItem.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				try {
					new OnClose().handleEvent(event);
					if (!event.doit) {
						return;
					}
					openFile(mf, null);
				} catch (Exception ex) {
					Logger.getLogger("bookshepherd").log(Level.SEVERE,
							"Error in opne file: ", ex);
				}
			}
		});

		MenuItem saveFileMenuItem = new MenuItem(fileMenu, SWT.CASCADE);
		saveFileMenuItem.setText(Resource.getText("menu.file.save"));
		saveFileMenuItem.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event arg0) {
				if (controller.getFile() == null) {
					FileSaveAs.saveAs(shell, controller);
					updateProperty();
				} else {
					FileSave.save(controller, shell);
				}
				updateTitle();
			}
		});

		MenuItem saveasFileMenuItem = new MenuItem(fileMenu, SWT.CASCADE);
		saveasFileMenuItem.setText(Resource.getText("menu.file.saveas"));
		saveasFileMenuItem.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event arg0) {
				FileSaveAs.saveAs(shell, controller);
				updateTitle();
				updateProperty();
			}
		});

		new MenuItem(fileMenu, SWT.SEPARATOR);
		MenuItem exitFileMenuItem = new MenuItem(fileMenu, SWT.CASCADE);
		exitFileMenuItem.setText(Resource.getText("menu.file.exit"));
		exitFileMenuItem.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event arg0) {
				shell.close();
			}
		});

	}

	public Menu getMenu() {
		return menu;
	}

	/**
	 * Update GUI properties.
	 * 
	 * @param property
	 * @param display
	 * @param mf
	 */
	protected void updateProperty() {
		referencePart.updateProperty();
		property.setWindowH(shell.getBounds().height);
		property.setWindowW(shell.getBounds().width);
		property.setWindowX(shell.getBounds().x);
		property.setWindowY(shell.getBounds().y);

		int[] ws = excerptSF.getWeights();
		property.setExcSashWTree(ws[0]);
		property.setExcSashWTable(ws[1]);

		ws = referenceSF.getWeights();
		property.setRefSashWTree(ws[0]);
		property.setRefSashWTable(ws[1]);

		property.setLastOpenedFile(controller.getFile());

	}

	public ReferencePart getReferencePart() {
		return referencePart;
	}

	public ExcerptPart getExcerptPart() {
		return excerptPart;
	}

	public void updateCounter() {
		String text = "";
		text += Resource.getText("counter.ref") + ": ";

		if (getReferencePart() != null) {
			text += String.valueOf(getReferencePart().getItems().size());
		} else {
			text += "0";
		}
		text += "\n";

		text += Resource.getText("counter.note") + ": ";
		if (getExcerptPart() != null) {
			text += String.valueOf(getExcerptPart().getItems().size());
		} else {
			text += "0";
		}

		if (getReferencePart() != null) {
			getReferencePart().getCounter().setText(text);
		}

		if (getExcerptPart() != null) {
			getExcerptPart().getCounter().setText(text);
		}
	}

	public class OnClose implements Listener {

		public void handleEvent(Event event) {
			if (controller.isModified()) {
				MessageBox messageBox = new MessageBox(shell, SWT.YES | SWT.NO
						| SWT.CANCEL | SWT.ICON_WARNING);
				messageBox.setText(Resource.getText("message.save"));
				messageBox.setMessage(Resource.getText("message.save"));
				int res = messageBox.open();
				if (res == SWT.NO) {
					updateProperty();
				} else if (res == SWT.YES) {
					if (controller.getFile() == null) {
						FileSaveAs.saveAs(shell, controller);
					} else {
						FileSave.save(controller, shell);
					}
				} else {
					event.doit = false;
				}
			}
			updateProperty();
		}
	}

}
