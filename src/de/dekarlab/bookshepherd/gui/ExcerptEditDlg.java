package de.dekarlab.bookshepherd.gui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import de.dekarlab.bookshepherd.BSConstant;
import de.dekarlab.bookshepherd.Property;
import de.dekarlab.bookshepherd.Resource;
import de.dekarlab.bookshepherd.action.SetBasePath;
import de.dekarlab.bookshepherd.controller.ModelController;
import de.dekarlab.bookshepherd.model.ExcerptGroup;
import de.dekarlab.bookshepherd.model.ExcerptItem;
import de.dekarlab.bookshepherd.model.ExcerptItemGroup;
import de.dekarlab.bookshepherd.model.ReferenceItem;

public class ExcerptEditDlg {

	private static String TREE_ATTR_ITEM = "tree_item";
	private List<ReferenceItem> rilist;
	private ExcerptItem excerptItem;
	private Button ok;
	private Button cancel;

	private Combo reference;
	private Text name;
	private Text text;
	private Text imagePath;
	private Button imageBrowseBt;
	private Button imageShowBt;

	private Tree groups;
	private TabFolder folder;

	private Property property;
	private Shell shell;
	private boolean closeOk;
	private ModelController controller;

	public ExcerptEditDlg(Shell shell, Property property,
			ExcerptItem excerptItem, ModelController controller,
			List<ReferenceItem> rilist) {
		this.property = property;
		this.excerptItem = excerptItem;
		this.shell = shell;
		this.closeOk = false;
		this.controller = controller;
		this.rilist = rilist;
	}

	public boolean init() {
		if (rilist.size() == 0) {
			return false;
		}
		shell.setBounds(property.getEditDlgWindowX(), property
				.getEditDlgWindowY(), property.getEditDlgWindowW(), property
				.getEditDlgWindowH());
		shell.setText(Resource.getText("excerptdlg.title"));
		shell.setLayout(new GridLayout());
		shell.setLayoutData(new GridData(GridData.FILL_BOTH));

		folder = new TabFolder(shell, SWT.NONE);
		folder.setLayoutData(new GridData(GridData.FILL_BOTH));
		folder.setLayout(new GridLayout());

		TabItem commonF = new TabItem(folder, SWT.NONE);
		commonF.setText(Resource.getText("excerptdlg.tab.common"));
		Composite commonGroup = new Composite(folder, SWT.NONE);
		commonF.setControl(commonGroup);
		GridLayout glC = new GridLayout();
		glC.numColumns = 2;
		commonGroup.setLayout(glC);
		commonGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
		new Label(commonGroup, SWT.NONE).setText(Resource
				.getText("excerptdlg.reference")
				+ ":");
		reference = new Combo(commonGroup, SWT.BORDER | SWT.READ_ONLY);
		reference.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		new Label(commonGroup, SWT.NONE).setText(Resource
				.getText("excerptdlg.name")
				+ ":");
		name = new Text(commonGroup, SWT.BORDER);
		name.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		TabItem textF = new TabItem(folder, SWT.NONE);
		textF.setText(Resource.getText("excerptdlg.tab.text"));
		Composite textGroup = new Composite(folder, SWT.NONE);
		textF.setControl(textGroup);
		textGroup.setLayout(new GridLayout());
		text = new Text(textGroup, SWT.MULTI | SWT.BORDER);
		text.setLayoutData(new GridData(GridData.FILL_BOTH));

		TabItem imageF = new TabItem(folder, SWT.NONE);
		imageF.setText(Resource.getText("excerptdlg.tab.image"));
		Composite imageGroup = new Composite(folder, SWT.NONE);
		imageF.setControl(imageGroup);
		GridLayout glI = new GridLayout();
		glI.numColumns = 2;
		imageGroup.setLayout(glI);
		new Label(imageGroup, SWT.NONE).setText(Resource
				.getText("excerptdlg.imagepath")
				+ ":");
		imagePath = new Text(imageGroup, SWT.BORDER);
		GridData gd5 = new GridData(GridData.FILL_HORIZONTAL);
		gd5.horizontalSpan = 2;
		imagePath.setLayoutData(gd5);

		imageBrowseBt = new Button(imageGroup, SWT.PUSH);
		imageBrowseBt.setText(Resource.getText("excerptdlg.btn.browse"));

		imageBrowseBt.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event arg0) {
				FileDialog fd = new FileDialog(shell, SWT.OPEN);
				fd.setFilterPath(property.getFileDlgImagePath());
				fd.setText(Resource.getText("editdlg.select"));
				String[] filterExt = { "*.gif;*.jpg;*.tiff", "*.*" };
				fd.setFilterExtensions(filterExt);
				String selected = fd.open();
				if (selected == null) {
					return;
				}
				property.setFileDlgImagePath(new File(selected).getParentFile()
						.getAbsolutePath());
				imagePath.setText(SetBasePath.getBasedPath(selected, property
						.getBasePath()));
			}

		});

		imageShowBt = new Button(imageGroup, SWT.PUSH);
		imageShowBt.setText(Resource.getText("excerptdlg.btn.show"));
		imageShowBt.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event arg0) {
				String file = imagePath.getText();
				if (file != null && !file.equals("")) {
					Program.launch(SetBasePath.getPath(file, property
							.getBasePath()));
				}
			}
		});

		TabItem groupsF = new TabItem(folder, SWT.NONE);
		groupsF.setText(Resource.getText("excerptdlg.tab.group"));
		Composite groupsGroup = new Composite(folder, SWT.NONE);
		groupsF.setControl(groupsGroup);
		groupsGroup.setLayout(new GridLayout());
		groups = new Tree(groupsGroup, SWT.BORDER | SWT.CHECK);
		groups.setLayoutData(new GridData(GridData.FILL_BOTH));

		Composite buttons = new Composite(shell, SWT.NONE);
		buttons.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayout gdb = new GridLayout();
		gdb.numColumns = 2;
		buttons.setLayout(gdb);
		cancel = new Button(buttons, SWT.PUSH);
		cancel.setText(Resource.getText("btn.cancel"));
		cancel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END
				| GridData.GRAB_HORIZONTAL));
		cancel.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event arg0) {
				shell.close();
			}
		});

		ok = new Button(buttons, SWT.PUSH);
		ok.setText(Resource.getText("btn.ok"));
		ok.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event arg0) {
				closeOk = true;
				updateModelFromView();
				shell.close();
			}
		});
		updateView();
		updateGroupTab();
		return true;
	}

	protected void updateView() {
		int index = 0;
		long refId = -1;
		if (excerptItem.getReference() != null) {
			refId = excerptItem.getReference().getId();
		}
		boolean useIndex = false;
		for (ReferenceItem ri : rilist) {
			reference.add(getReferenceName(ri));
			if (refId != ri.getId()) {
				index++;
			} else {
				useIndex = true;
				break;
			}
		}
		if (useIndex) {
			reference.select(index);
		} else {
			reference.select(0);
		}
		String comp = excerptItem.getName();
		if (comp != null) {
			name.setText(comp);
		}
		comp = excerptItem.getAttribute(BSConstant.ATTR_EXC_IMAGE);
		if (comp != null) {
			imagePath.setText(comp);
		}
		comp = excerptItem.getAttribute(BSConstant.ATTR_EXC_TEXT);
		if (comp != null) {
			text.setText(comp);
		}

	}

	protected void updateModelFromView() {
		excerptItem.setName(name.getText());
		// reference
		int index = reference.getSelectionIndex();
		excerptItem.setReference(rilist.get(index));
		if (excerptItem.getId() == 0) {
			rilist.get(index).getExcerpts().add(excerptItem);
		}
		updateItem(BSConstant.ATTR_EXC_IMAGE, imagePath.getText());
		updateItem(BSConstant.ATTR_EXC_TEXT, text.getText());
		saveGroupViewToModel();
	}

	protected void updateItem(String attrName, String attrValue) {
		if (attrName == null) {
			return;
		}
		excerptItem.setAttribute(attrName, attrValue);
	}

	protected void updateGroupTab() {
		ExcerptGroup root = controller.getExcerptTree();
		TreeItem rootGui = new TreeItem(groups, SWT.NONE);
		rootGui.setText(Resource.getText("tree.menu.groups"));
		rootGui.setData(TREE_ATTR_ITEM, root);
		addToTree(rootGui, root);
		rootGui.setExpanded(true);
	}

	protected void addToTree(TreeItem parentGui, ExcerptGroup parent) {
		TreeItem childGui;
		for (ExcerptGroup child : parent.getChildren()) {
			childGui = new TreeItem(parentGui, SWT.NONE);
			childGui.setText(child.getName());
			childGui.setData(TREE_ATTR_ITEM, child);
			for (ExcerptItemGroup groupItem : child.getItems()) {
				if (groupItem.getItem().getId() == excerptItem.getId()) {
					childGui.setChecked(true);
				}
			}
			addToTree(childGui, child);
		}
		parentGui.setExpanded(true);
	}

	public void updateProperty() {
		property.setEditDlgWindowH(shell.getBounds().height);
		property.setEditDlgWindowW(shell.getBounds().width);
		property.setEditDlgWindowX(shell.getBounds().x);
		property.setEditDlgWindowY(shell.getBounds().y);
	}

	protected List<ExcerptGroup> getSelectedGroupsFromTree(TreeItem locTree,
			List<ExcerptGroup> res) {
		for (TreeItem tItem : locTree.getItems()) {
			if (tItem.getChecked()) {
				ExcerptGroup eg = (ExcerptGroup) tItem.getData(TREE_ATTR_ITEM);
				res.add(eg);
			}
			getSelectedGroupsFromTree(tItem, res);
		}
		return res;
	}

	protected void saveGroupViewToModel() {
		ExcerptItem item = excerptItem;
		Set<ExcerptItemGroup> oldGroups = item.getGroups();
		List<ExcerptGroup> newGroups = new ArrayList<ExcerptGroup>();
		getSelectedGroupsFromTree(groups.getItem(0), newGroups);
		// should we add new
		boolean isOldInNewList;
		List<ExcerptItemGroup> listToRemove = new ArrayList<ExcerptItemGroup>();
		for (ExcerptItemGroup oldGroup : oldGroups) {
			isOldInNewList = false;
			for (ExcerptGroup newGroup : newGroups) {
				if (newGroup.getId() == oldGroup.getGroup().getId()) {
					isOldInNewList = true;
				}
			}
			// list to remove
			if (!isOldInNewList) {
				listToRemove.add(oldGroup);
			}
		}
		for (ExcerptItemGroup oldGroup : listToRemove) {
			if (oldGroup.getGroup() != null) {
				oldGroup.getItem().removeGroup(oldGroup.getGroup());
				oldGroup.getGroup().removeItem(item);
			}
		}

		boolean isNewInOldList;
		List<ExcerptGroup> listToAdd = new ArrayList<ExcerptGroup>();
		for (ExcerptGroup newGroup : newGroups) {
			isNewInOldList = false;
			for (ExcerptItemGroup oldGroup : oldGroups) {
				if (newGroup.getId() == oldGroup.getGroup().getId()) {
					isNewInOldList = true;
				}
			}
			// add
			if (!isNewInOldList) {
				listToAdd.add(newGroup);
			}
		}
		for (ExcerptGroup newGroup : listToAdd) {
			ExcerptItemGroup newItemGroup = controller.getModelRoot()
					.createExcerptItemGroup();
			item.addGroup(newItemGroup);
			newGroup.addItem(newItemGroup);
		}
	}

	/**
	 * Start GUI.
	 * 
	 * @param arg
	 *            arg
	 */
	public static void main(String[] arg) {
		Display display = new Display();
		Display.setAppName(Resource.getText("title"));
		Shell shell = new Shell(display);
		Property property = new Property();
		try {
			property.loadProperty();
			ExcerptEditDlg mainFrame = new ExcerptEditDlg(shell, property,
					null, null, null);
			mainFrame.init();
			shell.open();
			while (!shell.isDisposed()) {
				if (!display.readAndDispatch())
					display.sleep();
			}
			property.saveProperty();
		} catch (Exception e) {
			Logger.getLogger("bookshepherd").log(Level.SEVERE, e.getMessage(),
					e);

		} finally {
			display.dispose();
		}
	}

	public boolean isCloseOk() {
		return closeOk;
	}

	public static String getReferenceName(ReferenceItem ri) {
		String author = ri.getAttribute(BSConstant.BIB_TEX_AUTHOR);

		String year = ri.getAttribute(BSConstant.BIB_TEX_YEAR);
		if (author == null || author.equals("")) {
			author = ri.getAttribute(BSConstant.BIB_TEX_EDITOR);
		}
		if (author == null) {
			author = "";
		}

		if (year == null) {
			year = "";
		}
		return author + ", " + year + ri.getName();
	}
}
