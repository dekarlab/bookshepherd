package de.dekarlab.bookshepherd;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Property {

	public final static String PROP_COMMENT = "GUI Properties";
	public final static String FILE_NAME = "guiprops.xml";

	public final static String PROP_WINDOW_H = "window_h";
	public final static String PROP_WINDOW_W = "window_w";
	public final static String PROP_WINDOW_X = "window_x";
	public final static String PROP_WINDOW_Y = "window_y";

	public final static String PROP_EDITDLG_WINDOW_H = "editdlg_window_h";
	public final static String PROP_EDITDLG_WINDOW_W = "editdlg_window_w";
	public final static String PROP_EDITDLG_WINDOW_X = "editdlg_window_x";
	public final static String PROP_EDITDLG_WINDOW_Y = "editdlg_window_y";

	public final static String PROP_EXCGR_EDITDLG_WINDOW_H = "excgr_editdlg_window_h";
	public final static String PROP_EXCGR_EDITDLG_WINDOW_W = "excgr_editdlg_window_w";
	public final static String PROP_EXCGR_EDITDLG_WINDOW_X = "excgr_editdlg_window_x";
	public final static String PROP_EXCGR_EDITDLG_WINDOW_Y = "excgr_editdlg_window_y";

	public final static String PROP_REF_SASH_W_TREE = "ref_sash_w_tree";
	public final static String PROP_REF_SASH_W_TABLE = "ref_sash_w_table";

	public final static String PROP_EXC_SASH_W_TREE = "exc_sash_w_tree";
	public final static String PROP_EXC_SASH_W_TABLE = "exc_sash_w_table";

	public final static String PROP_SHOW_COLUMN = "show_column_";
	public final static String PROP_COLUMN_W = "column_w_";
	public final static String PROP_COLUMN_SORT = "column_sort_";

	public final static String PROP_SHOW_COLUMN_EXC = "show_column_exc_";
	public final static String PROP_COLUMN_W_EXC = "column_w_exc_";
	public final static String PROP_COLUMN_SORT_EXC = "column_sort_exc_";

	public final static String PROP_FILE_DLG_FILTER_PATH = "file_dlg_filter_path";
	public final static String PROP_FILE_DLG_IMAGE_PATH = "file_dlg_image_path";

	public final static String PROP_REF_BASE_PATH = "prop_ref_base_path";
	public final static String PROP_LAST_OPENED_FILE = "prop_last_opened_file";

	private String basePath;

	private Properties props;

	private int windowW;
	private int windowH;
	private int windowX;
	private int windowY;

	private int editDlgWindowW;
	private int editDlgWindowH;
	private int editDlgWindowX;
	private int editDlgWindowY;

	private List<String> columnNames;
	private List<Integer> columnWidths;
	private List<Integer> columnSorts;

	private List<String> columnNamesExc;
	private List<Integer> columnWidthsExc;
	private List<Integer> columnSortsExc;

	private int excSashWTree;
	private int excSashWTable;
	private int refSashWTree;
	private int refSashWTable;

	private String fileDlgFilterPath;
	private String fileDlgImagePath;
	private String lastOpenedFile;

	public Property() {
		props = new Properties();
	}

	public void loadProperty() throws BSException {
		props = new Properties();
		FileInputStream in = null;
		try {
			in = new FileInputStream(FILE_NAME);
			props.loadFromXML(in);
			// synchronize
			String temp = props.getProperty(PROP_WINDOW_H);
			if (temp != null) {
				windowH = Integer.parseInt(temp);
			}
			temp = props.getProperty(PROP_WINDOW_W);
			if (temp != null) {
				windowW = Integer.parseInt(temp);
			}

			temp = props.getProperty(PROP_WINDOW_X);
			if (temp != null) {
				windowX = Integer.parseInt(temp);
			}

			temp = props.getProperty(PROP_WINDOW_Y);
			if (temp != null) {
				windowY = Integer.parseInt(temp);
			}

			temp = props.getProperty(PROP_EDITDLG_WINDOW_H);
			if (temp != null) {
				editDlgWindowH = Integer.parseInt(temp);
			}
			temp = props.getProperty(PROP_EDITDLG_WINDOW_W);
			if (temp != null) {
				editDlgWindowW = Integer.parseInt(temp);
			}

			temp = props.getProperty(PROP_EDITDLG_WINDOW_X);
			if (temp != null) {
				editDlgWindowX = Integer.parseInt(temp);
			}

			temp = props.getProperty(PROP_EDITDLG_WINDOW_Y);
			if (temp != null) {
				editDlgWindowY = Integer.parseInt(temp);
			}

			temp = props.getProperty(PROP_EXC_SASH_W_TABLE);
			if (temp != null) {
				excSashWTable = Integer.parseInt(temp);
			}
			temp = props.getProperty(PROP_EXC_SASH_W_TREE);
			if (temp != null) {
				excSashWTree = Integer.parseInt(temp);
			}

			temp = props.getProperty(PROP_REF_SASH_W_TABLE);
			if (temp != null) {
				refSashWTable = Integer.parseInt(temp);
			}

			temp = props.getProperty(PROP_REF_SASH_W_TREE);
			if (temp != null) {
				refSashWTree = Integer.parseInt(temp);
			}

			fileDlgFilterPath = props.getProperty(PROP_FILE_DLG_FILTER_PATH);
			fileDlgImagePath = props.getProperty(PROP_FILE_DLG_IMAGE_PATH);
			lastOpenedFile = props.getProperty(PROP_LAST_OPENED_FILE);
			int i = 0;
			columnNames = new ArrayList<String>();
			columnSorts = new ArrayList<Integer>();
			columnWidths = new ArrayList<Integer>();
			while (true) {
				temp = props.getProperty(PROP_SHOW_COLUMN + i);
				if (temp != null) {
					columnNames.add(temp);
					temp = props.getProperty(PROP_COLUMN_SORT + i);
					if (temp != null) {
						columnSorts.add(Integer.parseInt(temp));
					} else {
						columnSorts.add(0);
					}
					temp = props.getProperty(PROP_COLUMN_W + i);
					if (temp != null) {
						columnWidths.add(Integer.parseInt(temp));
					} else {
						columnWidths.add(20);
					}
				} else {
					break;
				}
				i++;
			}

			i = 0;
			columnNamesExc = new ArrayList<String>();
			columnSortsExc = new ArrayList<Integer>();
			columnWidthsExc = new ArrayList<Integer>();
			while (true) {
				temp = props.getProperty(PROP_SHOW_COLUMN_EXC + i);
				if (temp != null) {
					columnNamesExc.add(temp);
					temp = props.getProperty(PROP_COLUMN_SORT_EXC + i);
					if (temp != null) {
						columnSortsExc.add(Integer.parseInt(temp));
					} else {
						columnSortsExc.add(0);
					}
					temp = props.getProperty(PROP_COLUMN_W_EXC + i);
					if (temp != null) {
						columnWidthsExc.add(Integer.parseInt(temp));
					} else {
						columnWidthsExc.add(20);
					}
				} else {
					break;
				}
				i++;
			}

			temp = props.getProperty(PROP_REF_BASE_PATH);
			if (temp != null) {
				basePath = temp;
			}

		} catch (FileNotFoundException e) {
			initDefaultProperties();
		} catch (IOException e) {
			throw new BSException(e);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					// not relevant
				}
			}
		}

	}

	public void saveProperty() throws BSException {
		// synchronize
		props.put(PROP_WINDOW_H, String.valueOf(windowH));
		props.put(PROP_WINDOW_W, String.valueOf(windowW));
		props.put(PROP_WINDOW_X, String.valueOf(windowX));
		props.put(PROP_WINDOW_Y, String.valueOf(windowY));

		props.put(PROP_EDITDLG_WINDOW_H, String.valueOf(editDlgWindowH));
		props.put(PROP_EDITDLG_WINDOW_W, String.valueOf(editDlgWindowW));
		props.put(PROP_EDITDLG_WINDOW_X, String.valueOf(editDlgWindowX));
		props.put(PROP_EDITDLG_WINDOW_Y, String.valueOf(editDlgWindowY));

		props.put(PROP_EXC_SASH_W_TABLE, String.valueOf(excSashWTable));
		props.put(PROP_EXC_SASH_W_TREE, String.valueOf(excSashWTree));
		props.put(PROP_REF_SASH_W_TREE, String.valueOf(refSashWTree));
		props.put(PROP_REF_SASH_W_TABLE, String.valueOf(refSashWTable));

		props.put(PROP_FILE_DLG_FILTER_PATH, getFileDlgFilterPath());
		props.put(PROP_FILE_DLG_IMAGE_PATH, getFileDlgImagePath());
		props.put(PROP_LAST_OPENED_FILE, getLastOpenedFile());

		for (int i = 0; i < columnNames.size(); i++) {
			props.put(PROP_SHOW_COLUMN + i, columnNames.get(i));
			props.put(PROP_COLUMN_SORT + i, String.valueOf(columnSorts.get(i)));
			props.put(PROP_COLUMN_W + i, String.valueOf(columnWidths.get(i)));
		}

		for (int i = 0; i < columnNamesExc.size(); i++) {
			props.put(PROP_SHOW_COLUMN_EXC + i, columnNamesExc.get(i));
			props.put(PROP_COLUMN_SORT_EXC + i, String.valueOf(columnSortsExc
					.get(i)));
			props.put(PROP_COLUMN_W_EXC + i, String.valueOf(columnWidthsExc
					.get(i)));
		}

		props.put(PROP_REF_BASE_PATH, getBasePath());
		// save
		FileOutputStream os = null;
		try {
			os = new FileOutputStream(FILE_NAME);
			props.storeToXML(os, PROP_COMMENT);
		} catch (FileNotFoundException e) {
			throw new BSException(e);
		} catch (IOException e) {
			throw new BSException(e);
		} finally {
			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {
					// not relevant
				}
			}
		}

	}

	protected void initDefaultProperties() {
		// attributes to show
		columnNames = new ArrayList<String>();
		columnWidths = new ArrayList<Integer>();
		columnSorts = new ArrayList<Integer>();

		columnNames.add(BSConstant.BIB_TEX_AUTHOR);
		columnWidths.add(200);
		columnSorts.add(0);

		columnNames.add(BSConstant.BIB_TEX_TITLE);
		columnWidths.add(300);
		columnSorts.add(0);

		columnNames.add(BSConstant.BIB_TEX_YEAR);
		columnWidths.add(300);
		columnSorts.add(0);

		columnNames.add(BSConstant.ATTR_SHORT_NAME);
		columnWidths.add(80);
		columnSorts.add(0);

		columnNames.add(BSConstant.ATTR_BIB_TEX_DOC_TYPE);
		columnWidths.add(70);
		columnSorts.add(0);

		columnNames.add(BSConstant.ATTR_FILE_TYPE);
		columnWidths.add(70);
		columnSorts.add(0);

		columnNames.add(BSConstant.ATTR_FILE);
		columnWidths.add(100);
		columnSorts.add(0);

		columnNamesExc = new ArrayList<String>();
		columnWidthsExc = new ArrayList<Integer>();
		columnSortsExc = new ArrayList<Integer>();
		columnNamesExc.add(BSConstant.ATTR_EXC_REFERENCE);
		columnWidthsExc.add(200);
		columnSortsExc.add(0);

		columnNamesExc.add(BSConstant.ATTR_SHORT_NAME);
		columnWidthsExc.add(100);
		columnSortsExc.add(0);

		columnNamesExc.add(BSConstant.ATTR_EXC_TEXT);
		columnWidthsExc.add(200);
		columnSortsExc.add(0);

		columnNamesExc.add(BSConstant.ATTR_EXC_IMAGE);
		columnWidthsExc.add(100);
		columnSortsExc.add(0);

		windowH = 700;
		windowW = 1000;
		windowX = 30;
		windowY = 30;

		editDlgWindowH = 400;
		editDlgWindowW = 600;
		editDlgWindowX = 30;
		editDlgWindowY = 30;

		excSashWTable = 80;
		excSashWTree = 20;
		refSashWTable = 80;
		refSashWTree = 20;

		fileDlgFilterPath = System.getProperty("user.home");
		basePath = "";
	}

	public void setWindowW(int windowW) {
		this.windowW = windowW;
	}

	public void setWindowH(int windowH) {
		this.windowH = windowH;
	}

	public void setColumnNames(List<String> columnNames) {
		this.columnNames = columnNames;
	}

	public List<String> getColumnNames() {
		return columnNames;
	}

	public int getWindowW() {
		return windowW;
	}

	public int getWindowH() {
		return windowH;
	}

	public int getWindowX() {
		return windowX;
	}

	public void setWindowX(int windowX) {
		this.windowX = windowX;
	}

	public int getWindowY() {
		return windowY;
	}

	public void setWindowY(int windowY) {
		this.windowY = windowY;
	}

	public List<Integer> getColumnWidths() {
		return columnWidths;
	}

	public void setColumnWidths(List<Integer> columnWidths) {
		this.columnWidths = columnWidths;
	}

	public List<Integer> getColumnSorts() {
		return columnSorts;
	}

	public void setColumnSorts(List<Integer> columnSorts) {
		this.columnSorts = columnSorts;
	}

	public int getEditDlgWindowW() {
		return editDlgWindowW;
	}

	public void setEditDlgWindowW(int editDlgWindowW) {
		this.editDlgWindowW = editDlgWindowW;
	}

	public int getEditDlgWindowH() {
		return editDlgWindowH;
	}

	public void setEditDlgWindowH(int editDlgWindowH) {
		this.editDlgWindowH = editDlgWindowH;
	}

	public int getEditDlgWindowX() {
		return editDlgWindowX;
	}

	public void setEditDlgWindowX(int editDlgWindowX) {
		this.editDlgWindowX = editDlgWindowX;
	}

	public int getEditDlgWindowY() {
		return editDlgWindowY;
	}

	public void setEditDlgWindowY(int editDlgWindowY) {
		this.editDlgWindowY = editDlgWindowY;
	}

	public List<String> getColumnNamesExc() {
		return columnNamesExc;
	}

	public void setColumnNamesExc(List<String> columnNamesExc) {
		this.columnNamesExc = columnNamesExc;
	}

	public List<Integer> getColumnWidthsExc() {
		return columnWidthsExc;
	}

	public void setColumnWidthsExc(List<Integer> columnWidthsExc) {
		this.columnWidthsExc = columnWidthsExc;
	}

	public List<Integer> getColumnSortsExc() {
		return columnSortsExc;
	}

	public void setColumnSortsExc(List<Integer> columnSortsExc) {
		this.columnSortsExc = columnSortsExc;
	}

	public int getExcSashWTree() {
		return excSashWTree;
	}

	public void setExcSashWTree(int excSashWTree) {
		this.excSashWTree = excSashWTree;
	}

	public int getExcSashWTable() {
		return excSashWTable;
	}

	public void setExcSashWTable(int excSashWTable) {
		this.excSashWTable = excSashWTable;
	}

	public int getRefSashWTree() {
		return refSashWTree;
	}

	public void setRefSashWTree(int refSashWTree) {
		this.refSashWTree = refSashWTree;
	}

	public int getRefSashWTable() {
		return refSashWTable;
	}

	public void setRefSashWTable(int refSashWTable) {
		this.refSashWTable = refSashWTable;
	}

	public String getFileDlgFilterPath() {
		return fileDlgFilterPath;
	}

	public void setFileDlgFilterPath(String fileDlgFilterPath) {
		this.fileDlgFilterPath = fileDlgFilterPath;
	}

	public String getBasePath() {
		if (basePath == null) {
			return "";
		}
		return basePath;
	}

	public void setBasePath(String basePath) {
		this.basePath = basePath;
	}

	public String getFileDlgImagePath() {
		if (fileDlgImagePath == null) {
			return "";
		}
		return fileDlgImagePath;
	}

	public void setFileDlgImagePath(String fileDlgImagePath) {
		this.fileDlgImagePath = fileDlgImagePath;
	}

	public String getLastOpenedFile() {
		if (lastOpenedFile == null) {
			return "";
		}
		return lastOpenedFile;
	}

	public void setLastOpenedFile(String lastOpenedFile) {
		this.lastOpenedFile = lastOpenedFile;
	}
}
