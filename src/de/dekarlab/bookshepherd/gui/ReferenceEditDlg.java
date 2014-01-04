package de.dekarlab.bookshepherd.gui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import de.dekarlab.bookshepherd.BSConstant;
import de.dekarlab.bookshepherd.CustomAttributes;
import de.dekarlab.bookshepherd.Property;
import de.dekarlab.bookshepherd.Resource;
import de.dekarlab.bookshepherd.action.SetBasePath;
import de.dekarlab.bookshepherd.controller.ModelController;
import de.dekarlab.bookshepherd.model.ReferenceGroup;
import de.dekarlab.bookshepherd.model.ReferenceItem;
import de.dekarlab.bookshepherd.model.ReferenceItemGroup;
import de.dekarlab.bookshepherd.util.Util;

public class ReferenceEditDlg {
	private static final String TREE_ATTR_ITEM = "tree_item";

	private TabFolder tabFolder;
	private Shell shell;
	private Combo docTypes;
	private Composite reqPanel;
	private Composite optPanel;
	private Composite customPanel;
	private Composite groupPanel;

	private Button[] reqCheck;
	private Button[] optCheck;
	private Button[] customCheck;

	private Text[] reqTexts;
	private Text[] optTexts;
	private Text[] customTexts;
	private Label[] reqLabel;
	private Label[] optLabel;
	private Label[] customLabel;

	private Text docPath;
	private Text name;
	private Button docBrowseBt;
	private Button docShowBt;

	private Tree groupTree;
	private Tree groupDeleteTree;
	private Tree groupAddTree;

	private boolean inited;
	private Button ok;
	private Button cancel;
	private List<ReferenceItem> items;
	private static final String ITEM_PROP_NAME = "itemName";
	private Property property;
	private boolean closeOk;
	private ModelController controller;

	public ReferenceEditDlg(Shell shell, List<ReferenceItem> items,
			Property property, ModelController controller) {
		this.items = items;
		this.shell = shell;
		this.property = property;
		shell.setText(Resource.getText("editdlg.title"));
		this.closeOk = false;
		this.controller = controller;
	}

	public boolean init() {
		shell.setBounds(property.getEditDlgWindowX(), property
				.getEditDlgWindowY(), property.getEditDlgWindowW(), property
				.getEditDlgWindowH());
		shell.setLayout(new GridLayout());
		shell.setLayoutData(new GridData(GridData.FILL_BOTH));
		tabFolder = new TabFolder(shell, SWT.NONE);
		tabFolder.setLayoutData(new GridData(GridData.FILL_BOTH));

		TabItem doctype = new TabItem(tabFolder, SWT.NONE);
		doctype.setText(Resource.getText("editdlg.bibtex.doctype"));
		Composite group = new Composite(tabFolder, SWT.NONE);
		doctype.setControl(group);

		GridLayout gl = new GridLayout();
		gl.numColumns = 2;
		group.setLayout(gl);

		new Label(group, SWT.NONE).setText(Resource.getText("editdlg.name"));
		name = new Text(group, SWT.BORDER);
		GridData gd3 = new GridData(GridData.FILL_HORIZONTAL);
		gd3.horizontalSpan = 1;
		name.setLayoutData(gd3);

		Label label = new Label(group, SWT.NONE);
		label.setText(Resource.getText("editdlg.doctype") + ":");
		docTypes = new Combo(group, SWT.READ_ONLY);
		docTypes.setItems(BSConstant.BIB_TEX_DOC_TYPES);
		GridData gd2 = new GridData(GridData.FILL_HORIZONTAL);
		gd2.horizontalSpan = 1;
		docTypes.setLayoutData(gd2);
		final Label docTypeDesc = new Label(group, SWT.NONE);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 4;
		docTypeDesc.setLayoutData(gd);

		docTypes.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event arg0) {
				docTypeDesc.setText(Resource.getText("desc.doctype."
						+ docTypes.getItem(docTypes.getSelectionIndex())));
				updateReqOptPropertiesTabs(BSConstant.BIB_TEX_DOC_TYPES[docTypes
						.getSelectionIndex()]);
			}
		});

		Label l1 = new Label(group, SWT.NONE);
		l1.setText(Resource.getText("editdlg.path") + ":");
		GridData gd4 = new GridData(GridData.FILL_HORIZONTAL);
		gd4.horizontalSpan = 2;
		l1.setLayoutData(gd4);

		docPath = new Text(group, SWT.BORDER | SWT.WRAP);
		GridData gd5 = new GridData(GridData.FILL_BOTH);
		gd5.horizontalSpan = 2;
		docPath.setLayoutData(gd5);

		docBrowseBt = new Button(group, SWT.PUSH);
		docBrowseBt.setText(Resource.getText("editdlg.browse"));

		docBrowseBt.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event arg0) {
				FileDialog fd = new FileDialog(shell, SWT.OPEN);
				fd.setText(Resource.getText("editdlg.select"));
				String[] filterExt = {
						"*.pdf;*.djvu;*.djv;*.doc;*.ppt;*.xls;*.chm", "*.*" };
				fd.setFilterExtensions(filterExt);
				String selected = fd.open();
				if (selected == null) {
					return;
				}
				property.setFileDlgFilterPath(new File(selected)
						.getParentFile().getAbsolutePath());
				docPath.setText(SetBasePath.getBasedPath(selected, property
						.getBasePath()));
			}

		});

		docShowBt = new Button(group, SWT.PUSH);
		docShowBt.setText(Resource.getText("editdlg.show"));
		docShowBt.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event arg0) {
				String file = docPath.getText();
				if (file != null && !file.equals("")) {
					Program.launch(SetBasePath.getPath(file, property
							.getBasePath()));
				}
			}
		});

		TabItem req = new TabItem(tabFolder, SWT.NONE);
		reqPanel = new Composite(tabFolder, SWT.NONE);
		req.setText(Resource.getText("editdlg.bibtex.required"));
		req.setControl(reqPanel);
		GridLayout glReq = new GridLayout();
		glReq.numColumns = 3;
		reqPanel.setLayout(glReq);

		TabItem opt = new TabItem(tabFolder, SWT.NONE);
		optPanel = new Composite(tabFolder, SWT.NONE);
		opt.setText(Resource.getText("editdlg.bibtex.optional"));
		opt.setControl(optPanel);
		GridLayout glOpt = new GridLayout();
		glOpt.numColumns = 3;
		optPanel.setLayout(glOpt);

		TabItem custom = new TabItem(tabFolder, SWT.NONE);
		customPanel = new Composite(tabFolder, SWT.NONE);
		custom.setText(Resource.getText("editdlg.bibtex.custom"));
		custom.setControl(customPanel);
		GridLayout glcustom = new GridLayout();
		glcustom.numColumns = 3;
		customPanel.setLayout(glcustom);

		initDynProps();

		TabItem groups = new TabItem(tabFolder, SWT.NONE);
		groupPanel = new Composite(tabFolder, SWT.NONE);
		groups.setText(Resource.getText("editdlg.tab.group"));
		groups.setControl(groupPanel);
		if (items.size() == 1) {
			groupPanel.setLayout(new GridLayout());
			groupTree = new Tree(groupPanel, SWT.BORDER | SWT.CHECK);
			groupTree.setLayoutData(new GridData(GridData.FILL_BOTH));
		} else {
			GridLayout gd6 = new GridLayout();
			gd6.numColumns = 2;
			groupPanel.setLayout(gd6);

			groupAddTree = new Tree(groupPanel, SWT.BORDER | SWT.CHECK);
			groupAddTree.setLayoutData(new GridData(GridData.FILL_BOTH));
			groupDeleteTree = new Tree(groupPanel, SWT.BORDER | SWT.CHECK);
			groupDeleteTree.setLayoutData(new GridData(GridData.FILL_BOTH));
		}
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

		if (!updateDocType()) {
			return false;
		} else {
			docTypeDesc.setText(Resource.getText("desc.doctype."
					+ docTypes.getItem(docTypes.getSelectionIndex())));
			updateReqOptPropertiesTabs(BSConstant.BIB_TEX_DOC_TYPES[docTypes
					.getSelectionIndex()]);
			updateDocTab();
		}

		updateGroupTab();
		optPanel.pack();
		reqPanel.pack();
		customPanel.pack();

		return true;
	}

	protected void updateDocTab() {
		if (items.size() == 1) {
			String nameStr = items.get(0).getName();
			if (nameStr != null) {
				name.setText(nameStr);
			} else {
				name.setText(Util.generateName(items.get(0)));
			}
			docPath.setText(getPropText(BSConstant.ATTR_FILE));
		} else {
			docPath.setEnabled(false);
			name.setEnabled(false);
		}
	}

	protected void updateGroupTab() {
		ReferenceGroup root = controller.getReferenceTree();
		if (items.size() == 1) {
			TreeItem rootGui = new TreeItem(groupTree, SWT.NONE);
			rootGui.setText(Resource.getText("tree.menu.groups"));
			rootGui.setData(TREE_ATTR_ITEM, root);
			addToTree(rootGui, root);
			rootGui.setExpanded(true);
		} else {
			TreeItem rootGui1 = new TreeItem(groupAddTree, SWT.NONE);
			rootGui1.setText(Resource.getText("editdlg.tab.addtogroup"));
			rootGui1.setData(TREE_ATTR_ITEM, root);
			addToTree(rootGui1, root);
			rootGui1.setExpanded(true);
			TreeItem rootGui2 = new TreeItem(groupDeleteTree, SWT.NONE);
			rootGui2.setText(Resource.getText("editdlg.tab.removetogroup"));
			rootGui2.setData(TREE_ATTR_ITEM, root);
			addToTree(rootGui2, root);
			rootGui2.setExpanded(true);
		}

	}

	protected void addToTree(TreeItem parentGui, ReferenceGroup parent) {
		TreeItem childGui;
		for (ReferenceGroup child : parent.getChildren()) {
			childGui = new TreeItem(parentGui, SWT.NONE);
			childGui.setText(child.getName());
			childGui.setData(TREE_ATTR_ITEM, child);
			// check
			if (items.size() == 1) {
				for (ReferenceItemGroup groupItem : child.getItems()) {
					if (groupItem.getItem().getId() == items.get(0).getId()) {
						childGui.setChecked(true);
					}
				}
			}
			addToTree(childGui, child);
		}
		parentGui.setExpanded(true);
	}

	protected boolean updateDocType() {
		String docType = getPropText(BSConstant.ATTR_BIB_TEX_DOC_TYPE);

		String docTypeTemp;
		for (int i = 0; i < BSConstant.BIB_TEX_DOC_TYPES.length; i++) {
			docTypeTemp = BSConstant.BIB_TEX_DOC_TYPES[i];
			if (docType.equals(docTypeTemp)) {
				docTypes.select(i);
				return true;
			}
		}
		MessageBox mb = new MessageBox(shell, SWT.ICON_INFORMATION);
		mb.setText(Resource.getText("info.multipleselection.doctype"));
		mb.open();
		return false;
	}

	protected void initDynProps() {
		int customLen = CustomAttributes.getInstance().getLength();
		if (!inited) {
			reqCheck = new Button[BSConstant.BIB_TEX_PROPS.length];
			optCheck = new Button[BSConstant.BIB_TEX_PROPS.length];
			customCheck = new Button[customLen];

			reqTexts = new Text[BSConstant.BIB_TEX_PROPS.length];
			optTexts = new Text[BSConstant.BIB_TEX_PROPS.length];
			customTexts = new Text[customLen];

			reqLabel = new Label[BSConstant.BIB_TEX_PROPS.length];
			optLabel = new Label[BSConstant.BIB_TEX_PROPS.length];
			customLabel = new Label[customLen];

			for (int i = 0; i < BSConstant.BIB_TEX_PROPS.length; i++) {
				reqCheck[i] = new Button(reqPanel, SWT.CHECK);
				reqLabel[i] = new Label(reqPanel, SWT.NONE);
				reqTexts[i] = new Text(reqPanel, SWT.BORDER);
				final Button reqCheckFin = reqCheck[i];
				reqTexts[i].addListener(SWT.KeyDown, new Listener() {
					public void handleEvent(Event arg0) {
						if (reqCheckFin.getVisible()) {
							reqCheckFin.setSelection(true);
						}
					}
				});
				reqTexts[i]
						.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				optCheck[i] = new Button(optPanel, SWT.CHECK);
				optLabel[i] = new Label(optPanel, SWT.NONE);
				optTexts[i] = new Text(optPanel, SWT.BORDER);
				final Button optCheckFin = reqCheck[i];
				optTexts[i].addListener(SWT.KeyDown, new Listener() {
					public void handleEvent(Event arg0) {
						if (optCheckFin.getVisible()) {
							optCheckFin.setSelection(true);
						}
					}
				});
				optTexts[i]
						.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

			}

			for (int i = 0; i < customLen; i++) {
				customCheck[i] = new Button(customPanel, SWT.CHECK);
				customLabel[i] = new Label(customPanel, SWT.NONE);
				customTexts[i] = new Text(customPanel, SWT.BORDER);
				final Button customCheckFin = customCheck[i];
				customTexts[i].addListener(SWT.KeyDown, new Listener() {
					public void handleEvent(Event arg0) {
						if (customCheckFin.getVisible()) {
							customCheckFin.setSelection(true);
						}
					}
				});
				customTexts[i].setLayoutData(new GridData(
						GridData.FILL_HORIZONTAL));
			}

			inited = true;
		}
		for (int i = 0; i < BSConstant.BIB_TEX_PROPS.length; i++) {
			reqCheck[i].setVisible(false);
			reqLabel[i].setVisible(false);
			reqTexts[i].setVisible(false);
			optCheck[i].setVisible(false);
			optLabel[i].setVisible(false);
			optTexts[i].setVisible(false);
		}

		for (int i = 0; i < customLen; i++) {
			customCheck[i].setVisible(false);
			customLabel[i].setVisible(false);
			customTexts[i].setVisible(false);
		}

	}

	protected void updateReqOptPropertiesTabs(String doctype) {
		initDynProps();
		String[] reqProps = new String[0];
		String[] optProps = new String[0];
		String[] customProps = new String[0];

		if (doctype.equals(BSConstant.BIB_TEX_DOC_ARTICLE)) {
			reqProps = BSConstant.BIB_TEX_DOC_ARTICLE_REQ;
			optProps = BSConstant.BIB_TEX_DOC_ARTICLE_OPT;
		} else if (doctype.equals(BSConstant.BIB_TEX_DOC_BOOK)) {
			reqProps = BSConstant.BIB_TEX_DOC_BOOK_REQ;
			optProps = BSConstant.BIB_TEX_DOC_BOOK_OPT;
		} else if (doctype.equals(BSConstant.BIB_TEX_DOC_BOOKLET)) {
			reqProps = BSConstant.BIB_TEX_DOC_BOOKLET_REQ;
			optProps = BSConstant.BIB_TEX_DOC_BOOKLET_OPT;
		} else if (doctype.equals(BSConstant.BIB_TEX_DOC_CONFERENCE)) {
			reqProps = BSConstant.BIB_TEX_DOC_INPROCEEDINGS_REQ;
			optProps = BSConstant.BIB_TEX_DOC_INPROCEEDINGS_OPT;
		} else if (doctype.equals(BSConstant.BIB_TEX_DOC_INBOOK)) {
			reqProps = BSConstant.BIB_TEX_DOC_INBOOK_REQ;
			optProps = BSConstant.BIB_TEX_DOC_INBOOK_OPT;
		} else if (doctype.equals(BSConstant.BIB_TEX_DOC_INCOLLECTION)) {
			reqProps = BSConstant.BIB_TEX_DOC_INCOLLECTION_REQ;
			optProps = BSConstant.BIB_TEX_DOC_INCOLLECTION_OPT;
		} else if (doctype.equals(BSConstant.BIB_TEX_DOC_INPROCEEDINGS)) {
			reqProps = BSConstant.BIB_TEX_DOC_INPROCEEDINGS_REQ;
			optProps = BSConstant.BIB_TEX_DOC_INPROCEEDINGS_OPT;
		} else if (doctype.equals(BSConstant.BIB_TEX_DOC_MANUAL)) {
			reqProps = BSConstant.BIB_TEX_DOC_MANUAL_REQ;
			optProps = BSConstant.BIB_TEX_DOC_MANUAL_OPT;
		} else if (doctype.equals(BSConstant.BIB_TEX_DOC_MASTERTHESIS)) {
			reqProps = BSConstant.BIB_TEX_DOC_MASTERTHESIS_REQ;
			optProps = BSConstant.BIB_TEX_DOC_MASTERTHESIS_OPT;
		} else if (doctype.equals(BSConstant.BIB_TEX_DOC_MISC)) {
			reqProps = BSConstant.BIB_TEX_DOC_MISC_REQ;
			optProps = BSConstant.BIB_TEX_DOC_MISC_OPT;
		} else if (doctype.equals(BSConstant.BIB_TEX_DOC_PHDTHESIS)) {
			reqProps = BSConstant.BIB_TEX_DOC_PHDTHESIS_REQ;
			optProps = BSConstant.BIB_TEX_DOC_PHDTHESIS_OPT;
		} else if (doctype.equals(BSConstant.BIB_TEX_DOC_PROCEEDINGS)) {
			reqProps = BSConstant.BIB_TEX_DOC_PROCEEDINGS_REQ;
			optProps = BSConstant.BIB_TEX_DOC_PROCEEDINGS_OPT;
		} else if (doctype.equals(BSConstant.BIB_TEX_DOC_TECHREPORT)) {
			reqProps = BSConstant.BIB_TEX_DOC_TECHREPORT_REQ;
			optProps = BSConstant.BIB_TEX_DOC_TECHREPORT_OPT;
		} else if (doctype.equals(BSConstant.BIB_TEX_DOC_UNPUBLISHED)) {
			reqProps = BSConstant.BIB_TEX_DOC_UNPUBLISHED_REQ;
			optProps = BSConstant.BIB_TEX_DOC_UNPUBLISHED_OPT;
		}
		customProps = CustomAttributes.getInstance().getPropName(doctype);

		boolean showCheck = items.size() > 1 ? true : false;
		
		for (int i = 0; i < reqProps.length; i++) {
			reqCheck[i].setVisible(showCheck);
			reqTexts[i].setVisible(true);
			reqLabel[i].setVisible(true);
			reqLabel[i].setText(Resource.getText("prop." + reqProps[i]) + ":");
			reqTexts[i].setText(getPropText(reqProps[i]));
			reqTexts[i].setData(ITEM_PROP_NAME, reqProps[i]);
		}

		for (int i = 0; i < optProps.length; i++) {
			optCheck[i].setVisible(showCheck);
			optTexts[i].setVisible(true);
			optLabel[i].setVisible(true);
			optLabel[i].setText(Resource.getText("prop." + optProps[i]) + ":");
			optTexts[i].setText(getPropText(optProps[i]));
			optTexts[i].setData(ITEM_PROP_NAME, optProps[i]);
		}

		for (int i = 0; i < customProps.length; i++) {
			customCheck[i].setVisible(showCheck);
			customTexts[i].setVisible(true);
			customLabel[i].setVisible(true);
			customLabel[i].setText(customProps[i] + ":");
			customTexts[i].setText(getPropText(customProps[i]));
			customTexts[i].setData(ITEM_PROP_NAME, customProps[i]);
		}

		optPanel.pack();
		reqPanel.pack();
		customPanel.pack();
	}

	protected String getPropText(String propName) {
		if (items.size() == 0) {
			return "";
		} else if (items.size() == 1) {
			String attr = items.get(0).getAttribute(propName);
			if (attr == null) {
				return "";
			}
			return attr;
		}
		// multiple selected
		String first = items.get(0).getAttribute(propName);
		if (first == null) {
			return "";
		}
		String comp;
		for (ReferenceItem item : items) {
			comp = item.getAttribute(propName);
			if (comp == null) {
				return "";
			}
			if (!first.equals(comp)) {
				return "";
			}
		}
		return first;
	}

	protected void updateModelFromView() {
		Text req;
		for (int i = 0; i < reqTexts.length; i++) {
			req = reqTexts[i];
			if (reqCheck[i].getVisible()) {
				if (reqCheck[i].getSelection()) {
					updateItems((String) req.getData(ITEM_PROP_NAME), req
							.getText());
				}
			} else {
				//update only for visible attributes
				if(req.getVisible()) {
					updateItems((String) req.getData(ITEM_PROP_NAME), req.getText());
				}
			}
		}

		Text opt;
		for (int i = 0; i < optTexts.length; i++) {
			opt = optTexts[i];
			if (optCheck[i].getVisible()) {
				if (optCheck[i].getSelection()) {
					updateItems((String) opt.getData(ITEM_PROP_NAME), opt
							.getText());
				}
			} else {
				//update only for visible attributes
				if(opt.getVisible()) {
					updateItems((String) opt.getData(ITEM_PROP_NAME), opt.getText());
				}
			}
		}

		Text custom;
		for (int i = 0; i < customTexts.length; i++) {
			custom = customTexts[i];
			if (customCheck[i].getVisible()) {
				if (customCheck[i].getSelection()) {
					updateItems((String) custom.getData(ITEM_PROP_NAME), custom
							.getText());
				}
			} else {
				//update only for visible attributes
				if(custom.getVisible()) {
					updateItems((String) custom.getData(ITEM_PROP_NAME), custom
						.getText());
				}
			}
		}

		updateItems(BSConstant.ATTR_BIB_TEX_DOC_TYPE, docTypes.getText());

		if (items.size() == 1) {
			items.get(0).setName(name.getText());
			updateItems(BSConstant.ATTR_FILE, docPath.getText());
			updateItems(BSConstant.ATTR_FILE_TYPE, Util.getDocFileType(docPath
					.getText()));
		}
		saveGroupViewToModel();
	}

	protected void saveGroupViewToModel() {
		if (items.size() == 1) {
			ReferenceItem item = items.get(0);
			Set<ReferenceItemGroup> oldGroups = item.getGroups();
			List<ReferenceGroup> newGroups = new ArrayList<ReferenceGroup>();
			getSelectedGroupsFromTree(groupTree.getItem(0), newGroups);
			// should we add new
			boolean isOldInNewList;
			List<ReferenceItemGroup> listToRemove = new ArrayList<ReferenceItemGroup>();
			for (ReferenceItemGroup oldGroup : oldGroups) {
				isOldInNewList = false;
				for (ReferenceGroup newGroup : newGroups) {
					if (newGroup.getId() == oldGroup.getGroup().getId()) {
						isOldInNewList = true;
					}
				}
				// list to remove
				if (!isOldInNewList) {
					listToRemove.add(oldGroup);
				}
			}
			for (ReferenceItemGroup oldGroup : listToRemove) {
				oldGroup.getItem().removeGroup(oldGroup.getGroup());
				oldGroup.getGroup().removeItem(item);
			}

			boolean isNewInOldList;
			List<ReferenceGroup> listToAdd = new ArrayList<ReferenceGroup>();
			for (ReferenceGroup newGroup : newGroups) {
				isNewInOldList = false;
				for (ReferenceItemGroup oldGroup : oldGroups) {
					if (newGroup.getId() == oldGroup.getGroup().getId()) {
						isNewInOldList = true;
					}
				}
				// add
				if (!isNewInOldList) {
					listToAdd.add(newGroup);
				}
			}
			for (ReferenceGroup newGroup : listToAdd) {
				ReferenceItemGroup newItemGroup = controller.getModelRoot()
						.createReferenceItemGroup();
				item.addGroup(newItemGroup);
				newGroup.addItem(newItemGroup);
			}
		} else {
			List<ReferenceGroup> newGroups = new ArrayList<ReferenceGroup>();
			getSelectedGroupsFromTree(groupAddTree.getItem(0), newGroups);
			List<ReferenceGroup> removeGroups = new ArrayList<ReferenceGroup>();
			getSelectedGroupsFromTree(groupDeleteTree.getItem(0), removeGroups);
			for (ReferenceItem item : items) {
				// remove groups
				for (ReferenceGroup oldGroupT : removeGroups) {
					for (ReferenceItemGroup oldGroup : item.getGroups()) {
						if (oldGroup.getGroup().getId() == oldGroupT.getId()) {
							oldGroup.getItem().removeGroup(oldGroup.getGroup());
							oldGroup.getGroup().removeItem(item);
							break;
						}
					}
				}

				// correct new groups, remove the existed group
				boolean isNewInOldList;
				List<ReferenceGroup> newGroupsCorrected = new ArrayList<ReferenceGroup>();
				Set<ReferenceItemGroup> oldGroups = item.getGroups();
				for (ReferenceGroup newGroup : newGroups) {
					isNewInOldList = false;
					for (ReferenceItemGroup oldGroup : oldGroups) {
						if (newGroup.getId() == oldGroup.getGroup().getId()) {
							isNewInOldList = true;
						}
					}
					// list to add
					if (!isNewInOldList) {
						newGroupsCorrected.add(newGroup);
					}
				}

				for (ReferenceGroup newGroup : newGroupsCorrected) {
					ReferenceItemGroup newItemGroup = controller.getModelRoot()
							.createReferenceItemGroup();
					item.addGroup(newItemGroup);
					newGroup.addItem(newItemGroup);
				}
			}
		}
	}

	protected List<ReferenceGroup> getSelectedGroupsFromTree(TreeItem locTree,
			List<ReferenceGroup> res) {
		for (TreeItem tItem : locTree.getItems()) {
			if (tItem.getChecked()) {
				ReferenceGroup rg = (ReferenceGroup) tItem
						.getData(TREE_ATTR_ITEM);
				res.add(rg);
			}
			getSelectedGroupsFromTree(tItem, res);
		}
		return res;
	}

	protected void updateItems(String attrName, String attrValue) {
		if (attrName == null) {
			return;
		}
		for (ReferenceItem item : items) {
			item.setAttribute(attrName, attrValue);
		}
	}

	/**
	 * Update GUI properties.
	 * 
	 * @param property
	 * @param display
	 * @param mf
	 */
	public void updateProperty() {
		property.setEditDlgWindowH(shell.getBounds().height);
		property.setEditDlgWindowW(shell.getBounds().width);
		property.setEditDlgWindowX(shell.getBounds().x);
		property.setEditDlgWindowY(shell.getBounds().y);
	}

	public boolean isCloseOk() {
		return closeOk;
	}

}
