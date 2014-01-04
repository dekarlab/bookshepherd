package de.dekarlab.bookshepherd.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import de.dekarlab.bookshepherd.BSConstant;
import de.dekarlab.bookshepherd.Property;
import de.dekarlab.bookshepherd.Resource;
import de.dekarlab.bookshepherd.action.ExportBibTex;
import de.dekarlab.bookshepherd.action.ExportHtml;
import de.dekarlab.bookshepherd.action.SetBasePath;
import de.dekarlab.bookshepherd.controller.ModelController;
import de.dekarlab.bookshepherd.controller.ModelSearcher;
import de.dekarlab.bookshepherd.model.ExcerptItem;
import de.dekarlab.bookshepherd.model.ReferenceGroup;
import de.dekarlab.bookshepherd.model.ReferenceItem;
import de.dekarlab.bookshepherd.model.loader.XmlLoader;
import de.dekarlab.bookshepherd.model.loader.XmlSaver;

public class ReferencePart {
	public static final String TREE_ATTR_ITEM = "item";
	private Label counter;

	private Table table;
	private Tree tree;
	private List<ReferenceItem> items;
	private Property property;
	private Shell shell;
	private ReferenceGroup root;
	private MainFrame mainFrame;
	private Text searchRef;
	private boolean performSearch;
	private ModelController controller;

	private Button publishBt;
	private Button importBt;
	/**
	 * Check if user press ESC during edition of tree.
	 */
	private boolean updateItemNameInTree;

	public ReferencePart(MainFrame mainFrame, Property property,
			ModelController controller, Shell shell) {
		this.property = property;
		this.controller = controller;
		this.shell = shell;
		this.mainFrame = mainFrame;
		setPerformSearch(true);
	}

	protected void createTree(Composite parent) {
		tree = new Tree(parent, SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
		GridData gd = new GridData(GridData.FILL_BOTH);
		tree.setLayoutData(gd);
		root = controller.getReferenceTree();
		updateTreeFromModel();
		tree.setMenu(createTreeMenu(tree));
		tree.pack();
		tree.getVerticalBar().setVisible(false);
		tree.getHorizontalBar().setVisible(false);
		// dragAndDrop();
	}

	protected void createTable(Composite parent) {
		table = new Table(parent, SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER);
		table.setLayoutData(new GridData(GridData.FILL_BOTH));

		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		TableColumn column;
		List<String> columnNames = property.getColumnNames();
		List<Integer> columnSorts = property.getColumnSorts();
		List<Integer> columnWidths = property.getColumnWidths();
		int i = 0;
		for (String columnName : columnNames) {
			column = new TableColumn(table, SWT.LEFT | SWT.ARROW_UP);
			column.setText(Resource.getText("prop." + columnName) + "  ");
			column.setWidth(columnWidths.get(i));
			if (columnSorts.get(i) == SWT.UP) {
				table.setSortColumn(column);
				table.setSortDirection(SWT.UP);
			} else if (columnSorts.get(i) == SWT.DOWN) {
				table.setSortColumn(column);
				table.setSortDirection(SWT.UP);
			}
			column.setData(Property.PROP_SHOW_COLUMN, columnName);
			column.setMoveable(true);
			column.setResizable(true);
			column.addListener(SWT.Selection,
					ColumnSorter.getListener(ColumnSorter.STRING_COMPARATOR));
			i++;
		}
		table.setMenu(createTableMenu(shell));
		final OpenReferenceEditListener oreListener = new OpenReferenceEditListener();

		table.addListener(SWT.MouseDown, new Listener() {
			public void handleEvent(Event e) {
				if ((e.stateMask & SWT.ALT) != 0) {
					oreListener.handleEvent(e);
					return;
				}
			}
		});

		table.addListener(SWT.MouseDoubleClick, new Listener() {
			public void handleEvent(Event e) {
				TableItem[] tableItems = table.getSelection();
				if (tableItems.length == 1) {
					ReferenceItem ri = (ReferenceItem) tableItems[0]
							.getData(ColumnSorter.TABLE_COLUMN_ATTR_ITEM);
					String file = ri.getAttribute(BSConstant.ATTR_FILE);
					if (file != null && !file.equals("")) {
						Program.launch(SetBasePath.getPath(file,
								property.getBasePath()));
					}
				}
			}
		});
		setItems(controller.getItems(getSortField()));
		updateTableFromModel();
		table.pack();
	}

	protected Menu createTableMenu(final Composite parent) {
		Menu pop = new Menu(parent.getShell(), SWT.POP_UP);
		MenuItem itemI = new MenuItem(pop, SWT.PUSH);
		itemI.setText(Resource.getText("menu.table.reference"));

		MenuItem itemBibTeX = new MenuItem(pop, SWT.PUSH);
		itemBibTeX.setText(Resource.getText("menu.table.bibtex"));

		MenuItem itemN = new MenuItem(pop, SWT.PUSH);
		itemN.setText(Resource.getText("menu.table.new"));

		MenuItem itemD = new MenuItem(pop, SWT.PUSH);
		itemD.setText(Resource.getText("menu.table.delete"));
		new MenuItem(pop, SWT.SEPARATOR);
		MenuItem itemE = new MenuItem(pop, SWT.PUSH);
		itemE.setText(Resource.getText("menu.table.addexcerpt"));

		itemE.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event arg0) {
				TableItem[] items = table.getSelection();
				if (items.length != 1) {
					return;
				}
				final ReferenceItem ri = (ReferenceItem) items[0]
						.getData(ColumnSorter.TABLE_COLUMN_ATTR_ITEM);
				final List<ExcerptItem> tItemsLst = new ArrayList<ExcerptItem>();
				ExcerptItem excerptItem = controller.getModelRoot()
						.createExcerptItem();
				excerptItem.setReference(ri);
				tItemsLst.add(excerptItem);
				Shell shellDlg = new Shell(shell, SWT.APPLICATION_MODAL
						| SWT.SHELL_TRIM);
				final ExcerptEditDlg dlg = new ExcerptEditDlg(shellDlg,
						property, excerptItem, controller, getItems());
				shellDlg.addListener(SWT.Close, new Listener() {
					public void handleEvent(Event arg0) {
						if (dlg.isCloseOk()) {
							controller.insertE(ri, tItemsLst);
							mainFrame.getExcerptPart().insertToTable(tItemsLst);
						}
						dlg.updateProperty();
					}
				});
				if (dlg.init()) {
					shellDlg.open();
				}
			}
		});

		itemI.addListener(SWT.Selection, new OpenReferenceEditListener());

		itemD.addListener(SWT.Selection, new Listener() {

			public void handleEvent(Event arg0) {
				final TableItem[] tItems = getTable().getSelection();
				if (tItems.length == 0) {
					return;
				}

				MessageBox messageBox = new MessageBox(shell, SWT.OK
						| SWT.CANCEL | SWT.ICON_WARNING);
				messageBox.setText(Resource.getText("message.delete"));
				messageBox.setMessage(Resource.getText("message.delete"));
				if (messageBox.open() == SWT.CANCEL) {
					return;
				}

				List<ReferenceItem> ritems = new ArrayList<ReferenceItem>();
				for (TableItem tItem : tItems) {
					ritems.add((ReferenceItem) tItem
							.getData(ColumnSorter.TABLE_COLUMN_ATTR_ITEM));
					tItem.dispose();
				}
				controller.remove(ritems);
			}

		});

		itemN.addListener(SWT.Selection, new Listener() {

			public void handleEvent(Event arg0) {
				final List<ReferenceItem> tItemsLst = new ArrayList<ReferenceItem>();
				final ReferenceItem ritem = controller.getModelRoot()
						.createReferenceItem();
				ritem.addAttribute(controller.getModelRoot()
						.createReferenceAttribute(
								BSConstant.ATTR_BIB_TEX_DOC_TYPE,
								BSConstant.BIB_TEX_DOC_BOOK));
				ritem.setName(Resource.getText("editdlg.defaultname"));

				tItemsLst.add(ritem);

				Shell shellDlg = new Shell(shell, SWT.APPLICATION_MODAL
						| SWT.SHELL_TRIM);
				final ReferenceEditDlg dlg = new ReferenceEditDlg(shellDlg,
						tItemsLst, property, controller);
				shellDlg.addListener(SWT.Close, new Listener() {
					public void handleEvent(Event arg0) {
						if (dlg.isCloseOk()) {
							controller.insert(tItemsLst);
							insertToTable(tItemsLst);
						}
						dlg.updateProperty();
					}
				});
				if (dlg.init()) {
					shellDlg.open();
				}
			}

		});

		itemBibTeX.addListener(SWT.Selection,
				new OpenReferenceBibTeXEditListener());

		return pop;
	}

	protected void updateTreeFromModel() {
		tree.removeAll();
		TreeItem groups = new TreeItem(tree, SWT.NONE);
		groups.setText(root.getName());
		groups.setData(TREE_ATTR_ITEM, root);
		addToTree(groups, root.getChildren());
		groups.setExpanded(true);
		tree.select(groups);
	}

	public void setRoot(ReferenceGroup root) {
		this.root = root;
	}

	protected void updateTableFromModel() {
		table.removeAll();
		TableItem tableItem;
		String[] values;
		List<String> columnNames = property.getColumnNames();
		for (ReferenceItem item : items) {
			int count = columnNames.size();
			values = new String[count];
			for (int i = 0; i < count; i++) {
				values[i] = item.getAttribute(columnNames.get(i));
			}
			tableItem = new TableItem(table, SWT.NONE);
			tableItem.setText(values);
			tableItem.setData(ColumnSorter.TABLE_COLUMN_ATTR_ITEM, item);
		}
	}

	public Table getTable() {
		return table;
	}

	public Tree getTree() {
		return tree;
	}

	public void updateProperty() {
		int count = getTable().getColumnCount();
		List<String> columnNames = new ArrayList<String>();
		List<Integer> columnWidths = new ArrayList<Integer>();
		List<Integer> columnSorts = new ArrayList<Integer>();
		TableColumn sort = getTable().getSortColumn();
		TableColumn column;
		int sortdir = getTable().getSortDirection();
		int[] order = getTable().getColumnOrder();
		for (int i = 0; i < count; i++) {
			column = getTable().getColumn(order[i]);
			if (sort != null && column.getText().equals(sort.getText())) {
				columnSorts.add(sortdir);
			} else {
				columnSorts.add(0);
			}
			columnWidths.add(column.getWidth());
			columnNames.add((String) column.getData(Property.PROP_SHOW_COLUMN));
		}
		property.setColumnNames(columnNames);
		property.setColumnSorts(columnSorts);
		property.setColumnWidths(columnWidths);

	}

	protected Menu createTreeMenu(Composite parent) {
		Menu pop = new Menu(parent.getShell(), SWT.POP_UP);
		MenuItem itemN = new MenuItem(pop, SWT.PUSH);
		itemN.setText(Resource.getText("tree.ref.add"));
		MenuItem itemD = new MenuItem(pop, SWT.PUSH);
		itemD.setText(Resource.getText("tree.ref.delete"));
		MenuItem itemE = new MenuItem(pop, SWT.PUSH);
		itemE.setText(Resource.getText("tree.ref.edit"));
		new MenuItem(pop, SWT.SEPARATOR);
		MenuItem itemUP = new MenuItem(pop, SWT.PUSH);
		itemUP.setText(Resource.getText("tree.ref.up"));
		MenuItem itemDN = new MenuItem(pop, SWT.PUSH);
		itemDN.setText(Resource.getText("tree.ref.down"));
		new MenuItem(pop, SWT.SEPARATOR);
		MenuItem itemUL = new MenuItem(pop, SWT.PUSH);
		itemUL.setText(Resource.getText("tree.ref.upperlevel"));
		MenuItem itemLL = new MenuItem(pop, SWT.PUSH);
		itemLL.setText(Resource.getText("tree.ref.lowerlevel"));

		final TreeEditor editor = new TreeEditor(tree);
		// The editor must have the same size as the cell and must
		// not be any smaller than 50 pixels.
		editor.horizontalAlignment = SWT.LEFT;
		editor.grabHorizontal = true;
		editor.minimumWidth = 50;

		updateItemNameInTree = true;

		tree.addListener(SWT.MouseDown, new Listener() {
			public void handleEvent(Event arg0) {
				Control oldEditor = editor.getEditor();
				if (oldEditor != null)
					oldEditor.dispose();
			}
		});

		tree.addListener(SWT.KeyDown, new Listener() {
			public void handleEvent(Event e) {
				if (e.keyCode == SWT.DEL) {
					new TreeOnDeleteEditListener().handleEvent(e);
				}
			}
		});

		itemN.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				final TreeItem[] tItems = getTree().getSelection();
				if (tItems.length != 1) {
					return;
				}
				final ReferenceGroup referenceGroup = controller.getModelRoot()
						.createReferenceGroup();
				referenceGroup.setName(Resource
						.getText("tree.refgroup.defaultname"));
				ReferenceGroup parent = (ReferenceGroup) tItems[0]
						.getData(TREE_ATTR_ITEM);
				controller.insert(referenceGroup, parent);
				addToTree(tItems[0], referenceGroup, -1);
				tItems[0].setExpanded(true);
				new TreeOnStartEditListener(editor).handleEvent(e);
			}
		});
		tree.addListener(SWT.MouseDoubleClick, new TreeOnStartEditListener(
				editor));

		addFilterListener(tree);

		itemE.addListener(SWT.Selection, new TreeOnStartEditListener(editor));
		itemD.addListener(SWT.Selection, new TreeOnDeleteEditListener());

		itemUL.addListener(SWT.Selection, new TreeOnUpperLevelListener());
		itemLL.addListener(SWT.Selection, new TreeOnLowerLevelListener());
		itemDN.addListener(SWT.Selection, new TreeOnUpDownPosListener(false));
		itemUP.addListener(SWT.Selection, new TreeOnUpDownPosListener(true));

		return pop;
	}

	protected class TreeOnUpperLevelListener implements Listener {
		public void handleEvent(Event arg0) {
			final TreeItem[] tItems = getTree().getSelection();
			if (tItems.length != 1) {
				return;
			}
			// find new parent
			TreeItem prevParentGui = tItems[0].getParentItem();
			if (prevParentGui == null) {
				return;
			}
			TreeItem newParentGui = prevParentGui.getParentItem();
			if (newParentGui == null) {
				return;
			}
			// find index to insert
			int index = 0;
			int count = newParentGui.getItemCount();
			for (int i = 0; i < count; i++) {
				if (newParentGui.getItem(i) == prevParentGui) {
					index = i + 1;
					break;
				}
			}

			ReferenceGroup newParent = (ReferenceGroup) newParentGui
					.getData(TREE_ATTR_ITEM);
			ReferenceGroup group = (ReferenceGroup) tItems[0]
					.getData(TREE_ATTR_ITEM);
			// delete from GUI
			tItems[0].dispose();
			// delete from children
			ReferenceGroup prevParent = group.getParent();
			controller.remove(group, prevParent);
			// add to new parent
			controller.insert(group, newParent, index);
			// add to GUI
			addToTree(newParentGui, group, index);
			tree.redraw();
		}
	}

	protected class TreeOnLowerLevelListener implements Listener {
		public void handleEvent(Event arg0) {
			final TreeItem[] tItems = getTree().getSelection();
			if (tItems.length != 1) {
				return;
			}
			// find new parent
			TreeItem newParentGui = tItems[0].getParentItem();
			if (newParentGui == null) {
				return;
			}
			int count = newParentGui.getItemCount();
			int index = 0;
			for (int i = 0; i < count; i++) {
				if (newParentGui.getItem(i) == tItems[0]) {
					index = i;
					break;
				}
			}
			if (index == 0) {
				// not possible to move lower
				return;
			}

			newParentGui = newParentGui.getItem(index - 1);
			ReferenceGroup newParent = (ReferenceGroup) newParentGui
					.getData(TREE_ATTR_ITEM);
			ReferenceGroup group = (ReferenceGroup) tItems[0]
					.getData(TREE_ATTR_ITEM);
			// delete from GUI
			tItems[0].dispose();
			// delete from children
			ReferenceGroup prevParent = group.getParent();
			controller.remove(group, prevParent);
			// add to new parent
			controller.insert(group, newParent);
			// add to GUI
			addToTree(newParentGui, group, -1);
			tree.redraw();
		}
	}

	protected class TreeOnUpDownPosListener implements Listener {
		private boolean moveUp;

		public TreeOnUpDownPosListener(boolean moveUp) {
			this.moveUp = moveUp;
		}

		public void handleEvent(Event arg0) {
			final TreeItem[] tItems = getTree().getSelection();
			if (tItems.length != 1) {
				return;
			}
			// find new place
			TreeItem parentGui = tItems[0].getParentItem();
			if (parentGui == null) {
				return;
			}
			int count = parentGui.getItemCount();
			int index = 0;
			for (int i = 0; i < count; i++) {
				if (parentGui.getItem(i) == tItems[0]) {
					if (moveUp) {
						index = i - 1;
					} else {
						index = i + 1;
					}
					break;
				}
			}
			if (moveUp) {
				if (index == -1) {
					// not possible to move upper
					return;
				}
			} else {
				if (index == count) {
					// not possible to move lower
					return;
				}
			}

			ReferenceGroup parent = (ReferenceGroup) parentGui
					.getData(TREE_ATTR_ITEM);
			ReferenceGroup group = (ReferenceGroup) tItems[0]
					.getData(TREE_ATTR_ITEM);
			// delete from GUI
			tItems[0].dispose();
			// delete from children
			controller.remove(group, parent);
			// add to new position
			controller.insert(group, parent, index);
			// add to GUI
			addToTree(parentGui, group, index);
			tree.redraw();

		}
	}

	protected class TreeOnDeleteEditListener implements Listener {

		public void handleEvent(Event arg0) {
			final TreeItem[] tItems = getTree().getSelection();
			if (tItems.length != 1) {
				return;
			}
			ReferenceGroup group = (ReferenceGroup) tItems[0]
					.getData(TREE_ATTR_ITEM);
			if (group.getParent() == null) {
				return;
			}
			MessageBox messageBox = new MessageBox(shell, SWT.OK | SWT.CANCEL
					| SWT.ICON_WARNING);
			messageBox.setText(Resource.getText("message.delete"));
			messageBox.setMessage(Resource.getText("message.delete"));
			if (messageBox.open() == SWT.CANCEL) {
				return;
			}

			controller.remove(group, group.getParent());
			tItems[0].dispose();
		}

	}

	protected class TreeOnStartEditListener implements Listener {
		private TreeEditor editor;

		public TreeOnStartEditListener(TreeEditor editor) {
			this.editor = editor;
		}

		public void handleEvent(Event e) {
			// Clean up any previous editor control
			Control oldEditor = editor.getEditor();
			if (oldEditor != null)
				oldEditor.dispose();
			// Identify the selected row
			final TreeItem[] tItems = getTree().getSelection();
			if (tItems.length != 1) {
				return;
			}
			ReferenceGroup group = (ReferenceGroup) tItems[0]
					.getData(TREE_ATTR_ITEM);
			if (group.getParent() == null) {
				return;
			}
			// The control that will be the editor must be a child
			// of the Tree
			Text newEditor = new Text(tree, SWT.NONE);
			newEditor.setText(tItems[0].getText());

			newEditor.addListener(SWT.KeyDown,
					new TreeOnKeyDownListener(editor));
			newEditor.addListener(SWT.Dispose,
					new TreeOnDisposeListener(editor));

			newEditor.selectAll();
			newEditor.setFocus();
			editor.setEditor(newEditor, tItems[0]);
		}
	}

	/**
	 * Enter or ESC during edit on the Editor in Tree.
	 * 
	 * 
	 */
	protected class TreeOnKeyDownListener implements Listener {
		private TreeEditor editor;

		public TreeOnKeyDownListener(TreeEditor editor) {
			this.editor = editor;
		}

		public void handleEvent(Event e) {
			if (e.keyCode == SWT.CR) {
				Control oldEditor = editor.getEditor();
				if (oldEditor != null)
					oldEditor.dispose();
			} else if (e.keyCode == SWT.ESC) {
				updateItemNameInTree = false;
				Control oldEditor = editor.getEditor();
				if (oldEditor != null)
					oldEditor.dispose();
			}
		}
	}

	/**
	 * Finish Edit on the Editor in Tree.
	 */
	protected class TreeOnDisposeListener implements Listener {
		private TreeEditor editor;

		public TreeOnDisposeListener(TreeEditor editor) {
			this.editor = editor;
		}

		public void handleEvent(Event arg0) {
			if (updateItemNameInTree) {
				Text text = (Text) editor.getEditor();
				editor.getItem().setText(text.getText());
				ReferenceGroup referenceGroup = (ReferenceGroup) editor
						.getItem().getData(TREE_ATTR_ITEM);
				referenceGroup.setName(text.getText());
				controller.change(referenceGroup);
			} else {
				updateItemNameInTree = true;
			}
		}
	}

	public void addFilterListener(Tree source) {
		source.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event arg0) {
				TreeItem[] titems = tree.getSelection();
				if (titems.length != 1) {
					return;
				}
				ReferenceGroup gr = (ReferenceGroup) titems[0]
						.getData(TREE_ATTR_ITEM);
				if (gr.getParent() != null) {
					setItems(ModelSearcher.searchReferenceItem(
							controller.getItems(gr, getSortField()),
							getSearchRef().getText()));
				} else {
					setItems(ModelSearcher.searchReferenceItem(controller
							.getItems(getSortField()), getSearchRef().getText()));
				}

				mainFrame.getExcerptPart().setItems(
						ModelSearcher.searchExcerptItem(controller
								.getExcerptItems(getItems(), mainFrame
										.getExcerptPart().getSortField()),
								mainFrame.getExcerptPart().getSearchExc()
										.getText()));

				shell.getDisplay().asyncExec(new Runnable() {
					public void run() {
						updateTableFromModel();
						mainFrame.getExcerptPart().updateTableFromModel();
					}
				});
			}
		});

	}

	protected void addToTree(TreeItem parent, ReferenceGroup child, int index) {
		TreeItem groups;
		if (index == -1) {
			groups = new TreeItem(parent, SWT.NONE);
		} else {
			groups = new TreeItem(parent, SWT.NONE, index);
		}
		groups.setText(child.getName());
		groups.setData(TREE_ATTR_ITEM, child);
		addToTree(groups, child.getChildren());
		tree.setSelection(groups);
	}

	protected void addToTree(TreeItem parent, List<ReferenceGroup> children) {
		if (children == null) {
			return;
		}
		int ind = 0;
		for (ReferenceGroup group : children) {
			addToTree(parent, group, -1);
			ind++;
		}
	}

	public void setItems(List<ReferenceItem> items) {
		this.items = items;
		mainFrame.updateCounter();
	}

	public class OpenReferenceEditListener implements Listener {
		public void handleEvent(Event arg0) {
			final TableItem[] tItems = getTable().getSelection();
			if (tItems.length == 0) {
				return;
			}
			final List<ReferenceItem> tItemsLst = new ArrayList<ReferenceItem>();
			ReferenceItem ri;
			for (TableItem tItem : tItems) {
				ri = (ReferenceItem) tItem
						.getData(ColumnSorter.TABLE_COLUMN_ATTR_ITEM);
				tItemsLst.add(ri);
			}
			Shell shellDlg = new Shell(shell, SWT.APPLICATION_MODAL
					| SWT.SHELL_TRIM);
			final ReferenceEditDlg dlg = new ReferenceEditDlg(shellDlg,
					tItemsLst, property, controller);
			shellDlg.addListener(SWT.Close, new Listener() {
				public void handleEvent(Event arg0) {
					if (dlg.isCloseOk()) {
						controller.change(tItemsLst);
						refreshTableRows(tItems);
					}
					dlg.updateProperty();
				}
			});
			if (dlg.init()) {
				shellDlg.open();
			}
		}
	}

	public class OpenReferenceBibTeXEditListener implements Listener {
		public void handleEvent(Event arg0) {
			final TableItem[] tItems = getTable().getSelection();
			if (tItems.length != 1) {
				return;
			}
			final ReferenceItem ri = (ReferenceItem) tItems[0]
					.getData(ColumnSorter.TABLE_COLUMN_ATTR_ITEM);
			Shell shellDlg = new Shell(shell, SWT.APPLICATION_MODAL
					| SWT.SHELL_TRIM);

			final BibTeXEditDlg dlg = new BibTeXEditDlg(shellDlg, ri, property);

			shellDlg.addListener(SWT.Close, new Listener() {
				public void handleEvent(Event arg0) {
					if (dlg.isCloseOk()) {
						controller.change(ri);
						refreshTableRows(tItems);
					}
					dlg.updateProperty();
				}
			});
			if (dlg.init()) {
				shellDlg.open();
			}
		}
	}

	public List<ReferenceItem> getItems() {
		return items;
	}

	protected void createSearch(Composite parent) {
		Composite group = new Composite(parent, SWT.NONE);
		group.setBackground(group.getShell().getDisplay()
				.getSystemColor(SWT.COLOR_GRAY));
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		group.setLayoutData(gd);
		GridLayout gl = new GridLayout();
		gl.numColumns = 5;
		group.setLayout(gl);

		counter = new Label(group, SWT.NONE);
		counter.setBackground(group.getBackground());
		GridData gd3 = new GridData(GridData.HORIZONTAL_ALIGN_CENTER
				| GridData.FILL_HORIZONTAL);
		counter.setLayoutData(gd3);

		importBt = new Button(group, SWT.PUSH);
		importBt.setText(Resource.getText("btn.ref.import"));
		final Shell shelLoc = shell;
		final ReferencePart refPart = this;
		importBt.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event arg0) {
				Shell shellDlg = new Shell(shelLoc, SWT.APPLICATION_MODAL
						| SWT.SHELL_TRIM);
				final ImportFileDlg dlg = new ImportFileDlg(shellDlg, property,
						refPart, controller);
				shellDlg.addListener(SWT.Close, new Listener() {
					public void handleEvent(Event arg0) {
						dlg.updateProperty();
					}
				});
				dlg.init();
				shellDlg.open();
			}

		});
		publishBt = new Button(group, SWT.PUSH);
		publishBt.setText(Resource.getText("btn.ref.publish"));
		publishBt.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event arg0) {
				// export sorted
				ExportHtml.export(controller.getItems(getSortField()), shell);
				// ExportHtml.export(list, shell);
			}

		});

		Label label = new Label(group, SWT.NONE);
		label.setBackground(group.getBackground());
		label.setText(Resource.getText("btn.search"));
		label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
		searchRef = new Text(group, SWT.SEARCH);
		GridData gd2 = new GridData();
		gd2.widthHint = 150;
		searchRef.setLayoutData(gd2);
		searchRef.addListener(SWT.Modify, new Listener() {
			public void handleEvent(Event arg0) {
				if (!isPerformSearch()) {
					return;
				}
				shell.getDisplay().asyncExec(new Runnable() {
					public void run() {
						TreeItem[] titems = getTree().getSelection();
						if (titems.length != 1) {
							return;
						}
						final ReferenceGroup gr = (ReferenceGroup) titems[0]
								.getData(ReferencePart.TREE_ATTR_ITEM);
						String sort = "";
						if (getTable().getSortColumn() != null) {
							sort = (String) getTable().getSortColumn().getData(
									Property.PROP_SHOW_COLUMN);
						}

						if (gr.getParent() != null) {
							setItems(ModelSearcher.searchReferenceItem(
									controller.getItems(gr, sort),
									searchRef.getText()));
						} else {
							setItems(ModelSearcher.searchReferenceItem(
									controller.getItems(sort),
									searchRef.getText()));
						}

						String sortE = "";
						if (mainFrame.getExcerptPart().getTable()
								.getSortColumn() != null) {
							sortE = (String) mainFrame.getExcerptPart()
									.getTable().getSortColumn()
									.getData(Property.PROP_SHOW_COLUMN);
						}

						mainFrame.getExcerptPart().setItems(
								ModelSearcher.searchExcerptItem(controller
										.getExcerptItems(getItems(), sortE),
										getSearchRef().getText()));

						updateTableFromModel();
						mainFrame.getExcerptPart().updateTableFromModel();
					}
				});
			}
		});

	}

	public Text getSearchRef() {
		return searchRef;
	}

	public Label getCounter() {
		return counter;
	}

	public boolean isPerformSearch() {
		return performSearch;
	}

	public void setPerformSearch(boolean performSearch) {
		this.performSearch = performSearch;
	}

	public void dragAndDrop() {
		Transfer[] types = new Transfer[] { TextTransfer.getInstance() };
		int operations = DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK;

		final DragSource source = new DragSource(tree, operations);
		source.setTransfer(types);
		final TreeItem[] dragSourceItem = new TreeItem[1];
		source.addDragListener(new DragSourceListener() {
			public void dragStart(DragSourceEvent event) {
				TreeItem[] selection = tree.getSelection();
				if (selection.length > 0 && selection[0].getItemCount() == 0) {
					event.doit = true;
					dragSourceItem[0] = selection[0];
				} else {
					event.doit = false;
				}
			};

			public void dragSetData(DragSourceEvent event) {
				StringBuffer out = new StringBuffer();
				XmlSaver.saveReferenceGroup((ReferenceGroup) dragSourceItem[0]
						.getData(TREE_ATTR_ITEM), out, 0);
				event.data = out.toString();
			}

			public void dragFinished(DragSourceEvent event) {
				if (event.detail == DND.DROP_MOVE)
					dragSourceItem[0].dispose();
				dragSourceItem[0] = null;
			}
		});

		DropTarget target = new DropTarget(tree, operations);
		target.setTransfer(types);
		target.addDropListener(new DropTargetAdapter() {
			public void dragOver(DropTargetEvent event) {
				event.feedback = DND.FEEDBACK_EXPAND | DND.FEEDBACK_SCROLL;
				if (event.item != null) {
					TreeItem item = (TreeItem) event.item;
					Point pt = shell.getDisplay().map(null, tree, event.x,
							event.y);
					Rectangle bounds = item.getBounds();
					if (pt.y < bounds.y + bounds.height / 3) {
						event.feedback |= DND.FEEDBACK_INSERT_BEFORE;
					} else if (pt.y > bounds.y + 2 * bounds.height / 3) {
						event.feedback |= DND.FEEDBACK_INSERT_AFTER;
					} else {
						event.feedback |= DND.FEEDBACK_SELECT;
					}
				}
			}

			public void drop(DropTargetEvent event) {
				if (event.data == null || event.item == null) {
					event.detail = DND.DROP_NONE;
					return;
				}

				// ReferenceGroup groupTo = (ReferenceGroup) event.item
				// .getData(TREE_ATTR_ITEM);
				// ReferenceGroup prevGroup = null;
				ReferenceGroup group;
				try {
					group = (ReferenceGroup) XmlLoader.parse(
							(String) event.data, controller.getModelRoot());
				} catch (Exception e) {
					Logger.getLogger("bookshepherd").log(Level.SEVERE,
							e.getMessage(), e);

					event.detail = DND.DROP_NONE;
					return;
				}
				TreeItem item = (TreeItem) event.item;
				Point pt = shell.getDisplay().map(null, tree, event.x, event.y);
				Rectangle bounds = item.getBounds();
				TreeItem parent = item.getParentItem();
				if (parent != null) {
					TreeItem[] items = parent.getItems();
					int index = 0;
					for (int i = 0; i < items.length; i++) {
						if (items[i] == item) {
							index = i;
							break;
						}
					}
					if (pt.y < bounds.y + bounds.height / 3) {
						TreeItem newItem = new TreeItem(parent, SWT.NONE, index);
						newItem.setText(group.getName());
						newItem.setData(TREE_ATTR_ITEM, group);
						// prevGroup = groupTo.getChildren().get(index);
					} else if (pt.y > bounds.y + 2 * bounds.height / 3) {
						TreeItem newItem = new TreeItem(parent, SWT.NONE,
								index + 1);
						newItem.setText(group.getName());
						newItem.setData(TREE_ATTR_ITEM, group);
						// prevGroup = groupTo.getChildren().get(index + 1);
					} else {
						TreeItem newItem = new TreeItem(item, SWT.NONE);
						newItem.setText(group.getName());
						newItem.setData(TREE_ATTR_ITEM, group);
					}

				}
				// else {
				// TreeItem[] items = tree.getItems();
				// int index = 0;
				// for (int i = 0; i < items.length; i++) {
				// if (items[i] == item) {
				// index = i;
				// break;
				// }
				// }
				// if (pt.y < bounds.y + bounds.height / 3) {
				// TreeItem newItem = new TreeItem(tree, SWT.NONE, index);
				// newItem.setText(text);
				// } else if (pt.y > bounds.y + 2 * bounds.height / 3) {
				// TreeItem newItem = new TreeItem(tree, SWT.NONE,
				// index + 1);
				// newItem.setText(text);
				// } else {
				// TreeItem newItem = new TreeItem(item, SWT.NONE);
				// newItem.setText(text);
				// }
				// }

				// ((XmlDao) dao).insertGroup(groupTo, prevGroup, group);
			}
		});
	}

	public void insertToTable(List<ReferenceItem> tItemsUpdate) {
		TableItem tableItem;
		String[] values;
		List<String> columnNames = property.getColumnNames();
		for (ReferenceItem item : tItemsUpdate) {
			int count = columnNames.size();
			values = new String[count];
			for (int i = 0; i < count; i++) {
				values[i] = item.getAttribute(columnNames.get(i));
			}
			tableItem = new TableItem(table, SWT.NONE);
			tableItem.setText(values);
			tableItem.setData(ColumnSorter.TABLE_COLUMN_ATTR_ITEM, item);
			items.add(item);
		}
	}

	protected void refreshTableRows(TableItem[] tItems) {
		String[] values;
		List<String> columnNames = property.getColumnNames();
		ReferenceItem item;
		for (TableItem tItem : tItems) {
			item = (ReferenceItem) tItem
					.getData(ColumnSorter.TABLE_COLUMN_ATTR_ITEM);
			int count = columnNames.size();
			values = new String[count];
			for (int i = 0; i < count; i++) {
				values[i] = item.getAttribute(columnNames.get(i));
			}
			tItem.setText(values);
		}
	}

	public String getSortField() {
		String sort = "";
		if (getTable().getSortColumn() != null) {
			sort = (String) getTable().getSortColumn().getData(
					Property.PROP_SHOW_COLUMN);
		}
		return sort;
	}

}
