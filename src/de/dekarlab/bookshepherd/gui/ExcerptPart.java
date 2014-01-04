package de.dekarlab.bookshepherd.gui;

import java.util.ArrayList;
import java.util.List;

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
import de.dekarlab.bookshepherd.action.SetBasePath;
import de.dekarlab.bookshepherd.controller.ModelController;
import de.dekarlab.bookshepherd.controller.ModelSearcher;
import de.dekarlab.bookshepherd.model.ExcerptGroup;
import de.dekarlab.bookshepherd.model.ExcerptItem;
import de.dekarlab.bookshepherd.model.ReferenceItem;

public class ExcerptPart {
	private Label counter;

	private static final String TREE_ATTR_ITEM = "item";

	private Table table;
	private Tree tree;

	private Property property;
	private ModelController controller;
	private Shell shell;

	private ExcerptGroup root;
	private List<ExcerptItem> items;
	private MainFrame mainFrame;
	private Text searchExc;
	private boolean performSearch;

	/**
	 * Check if user press ESC during edition of tree.
	 */
	private boolean updateItemNameInTree;

	public ExcerptPart(Property property, ModelController controller,
			Shell shell, MainFrame mainFrame) {
		this.property = property;
		this.controller = controller;
		this.shell = shell;
		this.mainFrame = mainFrame;
		setPerformSearch(true);
	}

	public void setRoot(ExcerptGroup root) {
		this.root = root;
	}

	protected void createTree(Composite parent) {
		tree = new Tree(parent, SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
		GridData gd = new GridData(GridData.FILL_VERTICAL);
		tree.setLayoutData(gd);
		root = controller.getExcerptTree();
		updateTreeFromModel();
		tree.setMenu(createTreeMenu(parent));
		tree.pack();
		tree.getVerticalBar().setVisible(false);
		tree.getHorizontalBar().setVisible(false);
		// dragAndDrop();
	}

	protected Menu createTreeMenu(Composite parent) {
		Menu pop = new Menu(parent.getShell(), SWT.POP_UP);
		MenuItem itemN = new MenuItem(pop, SWT.PUSH);
		itemN.setText(Resource.getText("tree.excerpt.add"));
		MenuItem itemD = new MenuItem(pop, SWT.PUSH);
		itemD.setText(Resource.getText("tree.excerpt.delete"));
		MenuItem itemE = new MenuItem(pop, SWT.PUSH);
		itemE.setText(Resource.getText("tree.excerpt.edit"));
		new MenuItem(pop, SWT.SEPARATOR);
		MenuItem itemUP = new MenuItem(pop, SWT.PUSH);
		itemUP.setText(Resource.getText("tree.excerpt.up"));
		MenuItem itemDN = new MenuItem(pop, SWT.PUSH);
		itemDN.setText(Resource.getText("tree.excerpt.down"));
		new MenuItem(pop, SWT.SEPARATOR);
		MenuItem itemUL = new MenuItem(pop, SWT.PUSH);
		itemUL.setText(Resource.getText("tree.excerpt.upperlevel"));
		MenuItem itemLL = new MenuItem(pop, SWT.PUSH);
		itemLL.setText(Resource.getText("tree.excerpt.lowerlevel"));

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
				final ExcerptGroup excerptGroup = controller.getModelRoot()
						.createExcerptGroup();
				excerptGroup.setName(Resource
						.getText("tree.excerptgroup.defaultname"));
				ExcerptGroup parent = (ExcerptGroup) tItems[0]
						.getData(TREE_ATTR_ITEM);
				controller.insert(excerptGroup, parent);
				addToTree(tItems[0], excerptGroup, -1);
				tItems[0].setExpanded(true);
				new TreeOnStartEditListener(editor).handleEvent(e);
			}
		});
		tree.addListener(SWT.MouseDoubleClick, new TreeOnStartEditListener(
				editor));

		itemE.addListener(SWT.Selection, new TreeOnStartEditListener(editor));
		itemD.addListener(SWT.Selection, new TreeOnDeleteEditListener());

		itemUL.addListener(SWT.Selection, new TreeOnUpperLevelListener());
		itemLL.addListener(SWT.Selection, new TreeOnLowerLevelListener());
		itemDN.addListener(SWT.Selection, new TreeOnUpDownPosListener(false));
		itemUP.addListener(SWT.Selection, new TreeOnUpDownPosListener(true));

		addFilterListener(tree);
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
			ExcerptGroup newParent = (ExcerptGroup) newParentGui
					.getData(TREE_ATTR_ITEM);
			ExcerptGroup group = (ExcerptGroup) tItems[0]
					.getData(TREE_ATTR_ITEM);
			// delete from GUI
			tItems[0].dispose();
			// delete from children
			ExcerptGroup prevParent = group.getParent();
			controller.remove(group, prevParent);
			// add to new parent, new parent null if this is a root
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
			ExcerptGroup newParent = (ExcerptGroup) newParentGui
					.getData(TREE_ATTR_ITEM);
			ExcerptGroup group = (ExcerptGroup) tItems[0]
					.getData(TREE_ATTR_ITEM);
			// delete from GUI
			tItems[0].dispose();
			// delete from children
			ExcerptGroup prevParent = group.getParent();
			controller.remove(group, prevParent);
			// add to new parent
			controller.insert(group, newParent);
			// save to new parent
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

			ExcerptGroup parent = (ExcerptGroup) parentGui
					.getData(TREE_ATTR_ITEM);
			ExcerptGroup group = (ExcerptGroup) tItems[0]
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
			ExcerptGroup group = (ExcerptGroup) tItems[0]
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
			ExcerptGroup group = (ExcerptGroup) tItems[0]
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
				ExcerptGroup excerptGroup = (ExcerptGroup) editor.getItem()
						.getData(TREE_ATTR_ITEM);
				excerptGroup.setName(text.getText());
				controller.change(excerptGroup);
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
				ExcerptGroup gr = (ExcerptGroup) titems[0]
						.getData(TREE_ATTR_ITEM);
				if (gr.getParent() != null) {
					setItems(ModelSearcher.searchExcerptItem(gr, controller
							.getExcerptItems(mainFrame.getReferencePart()
									.getItems(), getSortField()),
							getSearchExc().getText()));
				} else {
					setItems(ModelSearcher.searchExcerptItem(controller
							.getExcerptItems(mainFrame.getReferencePart()
									.getItems(), getSortField()),
							getSearchExc().getText()));
				}

				shell.getDisplay().asyncExec(new Runnable() {
					public void run() {
						updateTableFromModel();
					}
				});
			}
		});

	}

	protected void addToTree(TreeItem parent, ExcerptGroup child, int index) {
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

	protected void addToTree(TreeItem parent, List<ExcerptGroup> children) {
		if (children == null) {
			return;
		}
		int ind = 0;
		for (ExcerptGroup group : children) {
			addToTree(parent, group, -1);
			ind++;
		}
	}

	protected void createTable(Composite parent) {
		table = new Table(parent, SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER);
		table.setLayoutData(new GridData(GridData.FILL_BOTH));

		// Large size for rows
		Listener paintListener = new Listener() {
			public void handleEvent(Event event) {
				if (event.index != 2) {
					return;
				}
				switch (event.type) {
				case SWT.MeasureItem: {
					TableItem item = (TableItem) event.item;
					ExcerptItem ei = (ExcerptItem) item
							.getData(ColumnSorter.TABLE_COLUMN_ATTR_ITEM);
					String text = ei.getAttribute(BSConstant.ATTR_EXC_TEXT);
					if (text == null) {
						text = "";
					}
					Point size = event.gc.textExtent(text);
					event.width = size.x;
					event.height = Math.max(event.height, size.y);
					break;
				}
				case SWT.PaintItem: {
					TableItem item = (TableItem) event.item;
					ExcerptItem ei = (ExcerptItem) item
							.getData(ColumnSorter.TABLE_COLUMN_ATTR_ITEM);
					String text = ei.getAttribute(BSConstant.ATTR_EXC_TEXT);
					if (text == null) {
						text = "";
					}
					Point size = event.gc.textExtent(text);
					int offset2 = event.index == 0 ? Math.max(0,
							(event.height - size.y) / 2) : 0;
					event.gc.drawText(text, event.x, event.y + offset2, true);
					break;
				}
				case SWT.EraseItem: {
					event.detail &= ~SWT.FOREGROUND;
					break;
				}
				}
			}
		};
		table.addListener(SWT.MeasureItem, paintListener);
		table.addListener(SWT.PaintItem, paintListener);
		table.addListener(SWT.EraseItem, paintListener);

		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		TableColumn column;
		List<String> columnNames = property.getColumnNamesExc();
		List<Integer> columnSorts = property.getColumnSortsExc();
		List<Integer> columnWidths = property.getColumnWidthsExc();
		int i = 0;
		for (String columnName : columnNames) {
			column = new TableColumn(table, SWT.LEFT | SWT.ARROW_UP);
			column.setText(Resource.getText("excerpt.column." + columnName)
					+ "  ");
			column.setWidth(columnWidths.get(i));
			if (columnSorts.get(i) == SWT.UP) {
				table.setSortColumn(column);
				table.setSortDirection(SWT.UP);
			} else if (columnSorts.get(i) == SWT.DOWN) {
				table.setSortColumn(column);
				table.setSortDirection(SWT.UP);
			}
			column.setData(Property.PROP_SHOW_COLUMN_EXC, columnName);
			column.setMoveable(true);
			column.setResizable(true);
			column.addListener(SWT.Selection, ColumnSorter
					.getListener(ColumnSorter.STRING_COMPARATOR));
			i++;
		}

		table.addListener(SWT.MouseDoubleClick, new Listener() {
			public void handleEvent(Event arg0) {
				TableItem[] tableItems = table.getSelection();
				if (tableItems.length == 1) {
					ExcerptItem ei = (ExcerptItem) tableItems[0]
							.getData(ColumnSorter.TABLE_COLUMN_ATTR_ITEM);
					// if image
					String file = ei.getAttribute(BSConstant.ATTR_EXC_IMAGE);
					if (file != null && !file.equals("")) {
						Program.launch(SetBasePath.getPath(file, property
								.getBasePath()));
					} else {

						ReferenceItem ri = ei.getReference();
						if (ri == null) {
							return;
						}
						file = ri.getAttribute(BSConstant.ATTR_FILE);
						if (file != null && !file.equals("")) {
							Program.launch(SetBasePath.getPath(file, property
									.getBasePath()));
						}
					}
				}
			}
		});

		table.setMenu(createTableMenu(shell));
		items = controller.getExcerptItems(mainFrame.getReferencePart()
				.getItems(), getSortField());
		updateTableFromModel();
		table.pack();
	}

	protected Menu createTableMenu(Composite parent) {
		Menu pop = new Menu(parent.getShell(), SWT.POP_UP);
		MenuItem itemI = new MenuItem(pop, SWT.PUSH);
		itemI.setText(Resource.getText("menu.table.excerpt"));
		MenuItem itemN = new MenuItem(pop, SWT.PUSH);
		itemN.setText(Resource.getText("menu.table.new"));
		MenuItem itemD = new MenuItem(pop, SWT.PUSH);
		itemD.setText(Resource.getText("menu.table.delete"));

		itemI.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event arg0) {
				final TableItem[] tItems = getTable().getSelection();
				if (tItems.length != 1) {
					return;
				}
				final List<ExcerptItem> tItemsLst = new ArrayList<ExcerptItem>();
				ExcerptItem ei;
				for (TableItem tItem : tItems) {
					ei = (ExcerptItem) tItem
							.getData(ColumnSorter.TABLE_COLUMN_ATTR_ITEM);
					tItemsLst.add(ei);
				}
				Shell shellDlg = new Shell(shell, SWT.APPLICATION_MODAL
						| SWT.SHELL_TRIM);
				final ExcerptEditDlg dlg = new ExcerptEditDlg(shellDlg,
						property, tItemsLst.get(0), controller, mainFrame
								.getReferencePart().getItems());
				shellDlg.addListener(SWT.Close, new Listener() {
					public void handleEvent(Event arg0) {
						if (dlg.isCloseOk()) {
							controller.changeE(tItemsLst);
							refreshTableRows(tItems);
						}
						dlg.updateProperty();
					}
				});
				if (dlg.init()) {
					shellDlg.open();
				}
			}
		});

		itemN.addListener(SWT.Selection, new Listener() {

			public void handleEvent(Event arg0) {
				final List<ExcerptItem> tItemsLst = new ArrayList<ExcerptItem>();
				final ExcerptItem excerptItem = controller.getModelRoot()
						.createExcerptItem();
				tItemsLst.add(excerptItem);
				Shell shellDlg = new Shell(shell, SWT.APPLICATION_MODAL
						| SWT.SHELL_TRIM);
				final ExcerptEditDlg dlg = new ExcerptEditDlg(shellDlg,
						property, excerptItem, controller, mainFrame
								.getReferencePart().getItems());
				shellDlg.addListener(SWT.Close, new Listener() {
					public void handleEvent(Event arg0) {
						if (dlg.isCloseOk()) {
							controller.insertE(excerptItem.getReference(),
									tItemsLst);
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

				ExcerptItem ritem;
				for (TableItem tItem : tItems) {
					ritem = (ExcerptItem) tItem
							.getData(ColumnSorter.TABLE_COLUMN_ATTR_ITEM);
					controller.remove(ritem);
					tItem.dispose();
				}
			}

		});

		return pop;
	}

	protected void refreshTableRows(TableItem[] tItems) {
		ExcerptItem item;
		for (TableItem tItem : tItems) {
			item = (ExcerptItem) tItem
					.getData(ColumnSorter.TABLE_COLUMN_ATTR_ITEM);
			tItem.setText(getValuesForRow(item));
		}
	}

	protected String[] getValuesForRow(ExcerptItem item) {
		List<String> columnNames = property.getColumnNamesExc();
		int count = columnNames.size();
		String[] values = new String[count];
		for (int i = 0; i < count; i++) {
			if (columnNames.get(i).equals(BSConstant.ATTR_EXC_REFERENCE)) {
				values[i] = ExcerptEditDlg
						.getReferenceName(item.getReference());
			} else {
				values[i] = item.getAttribute(columnNames.get(i));
			}
		}
		return values;
	}

	public void insertToTable(List<ExcerptItem> tItemsUpdate) {
		TableItem tableItem;
		for (ExcerptItem item : tItemsUpdate) {
			tableItem = new TableItem(table, SWT.NONE);
			tableItem.setText(getValuesForRow(item));
			tableItem.setData(ColumnSorter.TABLE_COLUMN_ATTR_ITEM, item);
			items.add(item);
		}
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
			columnNames.add((String) column
					.getData(Property.PROP_SHOW_COLUMN_EXC));
		}
		property.setColumnNamesExc(columnNames);
		property.setColumnSortsExc(columnSorts);
		property.setColumnWidthsExc(columnWidths);

	}

	public Table getTable() {
		return table;
	}

	public Tree getTree() {
		return tree;
	}

	protected void updateTreeFromModel() {
		tree.removeAll();
		TreeItem groups = new TreeItem(tree, SWT.NONE);
		groups.setText(Resource.getText("tree.excerpt"));
		groups.setData(TREE_ATTR_ITEM, root);
		addToTree(groups, root.getChildren());
		groups.setExpanded(true);
		tree.select(groups);
	}

	protected void updateTableFromModel() {
		table.removeAll();
		TableItem tableItem;
		// update table
		for (ExcerptItem item : items) {
			tableItem = new TableItem(table, SWT.NONE);
			tableItem.setText(getValuesForRow(item));
			tableItem.setData(ColumnSorter.TABLE_COLUMN_ATTR_ITEM, item);
		}
	}

	public void setItems(List<ExcerptItem> items) {
		this.items = items;
		mainFrame.updateCounter();
	}

	protected void createSearch(Composite parent) {
		Composite group = new Composite(parent, SWT.NONE);
		group.setBackground(group.getShell().getDisplay().getSystemColor(
				SWT.COLOR_GRAY));
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		group.setLayoutData(gd);
		GridLayout gl = new GridLayout();
		gl.numColumns = 4;
		group.setLayout(gl);

		counter = new Label(group, SWT.NONE);
		counter.setBackground(group.getBackground());
		GridData gd3 = new GridData(GridData.HORIZONTAL_ALIGN_CENTER
				| GridData.FILL_HORIZONTAL);
		// gd3.widthHint = 50;

		counter.setLayoutData(gd3);

		Label label = new Label(group, SWT.NONE);
		label.setBackground(group.getBackground());
		label.setText(Resource.getText("btn.search.notes"));
		label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));

		searchExc = new Text(group, SWT.SEARCH);
		GridData gd2 = new GridData();
		gd2.widthHint = 150;
		searchExc.setLayoutData(gd2);
		searchExc.addListener(SWT.Modify, new Listener() {
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
						final ExcerptGroup gr = (ExcerptGroup) titems[0]
								.getData(ExcerptPart.TREE_ATTR_ITEM);
						if (gr.getParent() != null) {
							setItems(ModelSearcher.searchExcerptItem(gr,
									controller.getExcerptItems(mainFrame
											.getReferencePart().getItems(),
											getSortField()), searchExc
											.getText()));
						} else {
							setItems(ModelSearcher.searchExcerptItem(controller
									.getExcerptItems(mainFrame
											.getReferencePart().getItems(),
											getSortField()), searchExc
									.getText()));
						}
						mainFrame.getReferencePart().updateTableFromModel();
						updateTableFromModel();
					}
				});
			}
		});
	}

	public Text getSearchExc() {
		return searchExc;
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
				event.data = dragSourceItem[0].getText();
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
				if (event.data == null) {
					event.detail = DND.DROP_NONE;
					return;
				}
				String text = (String) event.data;
				if (event.item == null) {
					TreeItem item = new TreeItem(tree, SWT.NONE);
					item.setText(text);
				} else {
					TreeItem item = (TreeItem) event.item;
					Point pt = shell.getDisplay().map(null, tree, event.x,
							event.y);
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
							TreeItem newItem = new TreeItem(parent, SWT.NONE,
									index);
							newItem.setText(text);
						} else if (pt.y > bounds.y + 2 * bounds.height / 3) {
							TreeItem newItem = new TreeItem(parent, SWT.NONE,
									index + 1);
							newItem.setText(text);
						} else {
							TreeItem newItem = new TreeItem(item, SWT.NONE);
							newItem.setText(text);
						}

					} else {
						TreeItem[] items = tree.getItems();
						int index = 0;
						for (int i = 0; i < items.length; i++) {
							if (items[i] == item) {
								index = i;
								break;
							}
						}
						if (pt.y < bounds.y + bounds.height / 3) {
							TreeItem newItem = new TreeItem(tree, SWT.NONE,
									index);
							newItem.setText(text);
						} else if (pt.y > bounds.y + 2 * bounds.height / 3) {
							TreeItem newItem = new TreeItem(tree, SWT.NONE,
									index + 1);
							newItem.setText(text);
						} else {
							TreeItem newItem = new TreeItem(item, SWT.NONE);
							newItem.setText(text);
						}
					}

				}
			}
		});
	}

	public String getSortField() {
		String sort = "";
		if (getTable().getSortColumn() != null) {
			sort = (String) getTable().getSortColumn().getData(
					Property.PROP_SHOW_COLUMN);
		}
		return sort;
	}

	public List<ExcerptItem> getItems() {
		return items;
	}

}
