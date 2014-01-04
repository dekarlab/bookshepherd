package de.dekarlab.bookshepherd.action;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.dekarlab.bookshepherd.BSConstant;
import de.dekarlab.bookshepherd.controller.ModelController;
import de.dekarlab.bookshepherd.model.ModelRoot;
import de.dekarlab.bookshepherd.model.ReferenceGroup;
import de.dekarlab.bookshepherd.model.ReferenceItem;
import de.dekarlab.bookshepherd.model.ReferenceItemGroup;
import de.dekarlab.bookshepherd.util.Util;

public class ImportFiles {

	public static List<ReferenceItem> importFiles(ReferenceGroup group,
			File dir, int format, String basePath, List<ReferenceItem> refList,
			ModelController controller, boolean useFolderAsCat) {
		List<ReferenceItem> res = new ArrayList<ReferenceItem>();
		if (!dir.isDirectory()) {
			return res;
		}
		File[] files = dir.listFiles();
		ReferenceItem item;
		for (File file : files) {
			if (file.isDirectory()) {
				// create new category
				if (useFolderAsCat) {
					// if has files, then create
					if (hasFiles(file, refList, basePath)) {
						ReferenceGroup groupNew = controller.getModelRoot()
								.createReferenceGroup();
						groupNew.setName(file.getName());
						controller.insert(groupNew, group);
						res.addAll(importFiles(groupNew, file, format,
								basePath, refList, controller, useFolderAsCat));
					}
				} else {
					res.addAll(importFiles(group, file, format, basePath,
							refList, controller, useFolderAsCat));
				}
			} else {
				String path = file.getAbsolutePath();
				item = controller.getModelRoot().createReferenceItem();
				item.addAttribute(controller.getModelRoot()
						.createReferenceAttribute(BSConstant.ATTR_FILE,
								SetBasePath.getBasedPath(path, basePath)));
				String fileType = Util.getDocFileType(path);
				item.addAttribute(controller.getModelRoot()
						.createReferenceAttribute(BSConstant.ATTR_FILE_TYPE,
								fileType));
				if (fileType.equals(BSConstant.FILE_TYPE_PAPER)) {
					continue;
				}

				String fileName = file.getName();
				parseFileName(item, fileName, format, controller.getModelRoot());
				ReferenceItemGroup rig = controller.getModelRoot()
						.createReferenceItemGroup();
				if (hasFileInDb(item.getAttribute(BSConstant.ATTR_FILE),
						refList)) {
					continue;
				}
				item.addGroup(rig);
				group.addItem(rig);
				res.add(item);
			}
		}
		/**
		 * ReferenceItem ri; for (int i = 0; i < 1000; i++) { ri = new
		 * ReferenceItem(); ri.setName("Test" + i); ri.addAttribute(new
		 * ReferenceAttribute( Constant.ATTR_BIB_TEX_DOC_TYPE,
		 * Constant.BIB_TEX_DOC_MISC)); res.add(ri); }
		 **/
		return res;
	}

	/**
	 * Check if the directory has files.
	 * 
	 * @param dir
	 * @return
	 */
	protected static boolean hasFiles(File dir, List<ReferenceItem> refList,
			String basePath) {
		if (dir.isDirectory()) {
			File[] files = dir.listFiles();
			boolean status = false;
			for (File file : files) {
				if (hasFiles(file, refList, basePath)) {
					status = true;
				}
			}
			return status;
		} else {
			String path = dir.getAbsolutePath();
			path = SetBasePath.getBasedPath(path, basePath);
			if (hasFileInDb(path, refList)) {
				return false;
			}
			return true;
		}
	}

	protected static boolean hasFileInDb(String file, List<ReferenceItem> refI) {
		int count = refI.size();
		if (file == null || file.equals("")) {
			return false;
		}
		String fileComp;
		ReferenceItem ri;
		for (int i = 0; i < count; i++) {
			ri = refI.get(i);
			fileComp = ri.getAttribute(BSConstant.ATTR_FILE);
			if (fileComp == null) {
				continue;
			} else {
				if (fileComp.equals(file)) {
					return true;
				}
			}
		}
		return false;
	}

	protected static void parseFileName(ReferenceItem item, String fileName,
			int format, ModelRoot mr) {
		try {
			switch (format) {
			case 1:
				parseFileNameF1(item, fileName, mr);
				break;
			case 2:
				parseFileNameF2(item, fileName, mr);
				break;
			case 3:
				parseFileNameF3(item, fileName, mr);
				break;
			case 4:
				parseFileNameF4(item, fileName, mr);
				break;
			case 5:
				parseFileNameF5(item, fileName, mr);
				break;
			default:
				parseFileNameF0(item, fileName, mr);
				break;
			}
		} catch (Exception e) {
			Logger.getLogger("bookshepherd").log(Level.SEVERE, e.getMessage(),
					e);

		}
		String name = Util.generateName(item);
		if (name.equals("")) {
			name = fileName;
		}
		item.setName(name);
	}

	/**
	 * Autor, Autor - Title - Year.pdf
	 * 
	 * @param item
	 * @param fileName
	 */
	protected static void parseFileNameF1(ReferenceItem item, String fileName,
			ModelRoot mr) {
		StringTokenizer strt = new StringTokenizer(fileName, "-.");
		item.addAttribute(mr.createReferenceAttribute(
				BSConstant.ATTR_BIB_TEX_DOC_TYPE,
				BSConstant.BIB_TEX_DOC_ARTICLE));
		item.addAttribute(mr.createReferenceAttribute(
				BSConstant.BIB_TEX_AUTHOR, strt.nextToken().trim()));
		item.addAttribute(mr.createReferenceAttribute(BSConstant.BIB_TEX_TITLE,
				strt.nextToken().trim()));
		item.addAttribute(mr.createReferenceAttribute(BSConstant.BIB_TEX_YEAR,
				strt.nextToken().trim()));
	}

	/**
	 * Title.pdf
	 * 
	 * @param item
	 * @param fileName
	 */
	protected static void parseFileNameF0(ReferenceItem item, String fileName,
			ModelRoot mr) {
		item.addAttribute(mr.createReferenceAttribute(
				BSConstant.ATTR_BIB_TEX_DOC_TYPE, BSConstant.BIB_TEX_DOC_BOOK));
		int k = fileName.lastIndexOf(".");
		String title = fileName;
		if (k != -1) {
			title = fileName.substring(0, k);
		}
		item.addAttribute(mr.createReferenceAttribute(BSConstant.BIB_TEX_TITLE,
				title));
	}

	/**
	 * Publisher - Title.pdf
	 * 
	 * @param item
	 * @param fileName
	 */
	protected static void parseFileNameF2(ReferenceItem item, String fileName,
			ModelRoot mr) {
		StringTokenizer strt = new StringTokenizer(fileName, "-.");

		item.addAttribute(mr.createReferenceAttribute(
				BSConstant.ATTR_BIB_TEX_DOC_TYPE, BSConstant.BIB_TEX_DOC_BOOK));
		item.addAttribute(mr.createReferenceAttribute(
				BSConstant.BIB_TEX_PUBLISHER, strt.nextToken().trim()));
		item.addAttribute(mr.createReferenceAttribute(BSConstant.BIB_TEX_TITLE,
				strt.nextToken().trim()));
	}

	/**
	 * Journal - Title.pdf
	 * 
	 * @param item
	 * @param fileName
	 */
	protected static void parseFileNameF3(ReferenceItem item, String fileName,
			ModelRoot mr) {
		StringTokenizer strt = new StringTokenizer(fileName, "-.");
		item.addAttribute(mr.createReferenceAttribute(
				BSConstant.ATTR_BIB_TEX_DOC_TYPE,
				BSConstant.BIB_TEX_DOC_ARTICLE));
		item.addAttribute(mr.createReferenceAttribute(
				BSConstant.BIB_TEX_JOURNAL, strt.nextToken().trim()));
		item.addAttribute(mr.createReferenceAttribute(BSConstant.BIB_TEX_TITLE,
				strt.nextToken().trim()));

	}

	/**
	 * Publisher - Title - Year.pdf
	 * 
	 * @param item
	 * @param fileName
	 */
	protected static void parseFileNameF4(ReferenceItem item, String fileName,
			ModelRoot mr) {
		StringTokenizer strt = new StringTokenizer(fileName, "-.");

		item.addAttribute(mr.createReferenceAttribute(
				BSConstant.ATTR_BIB_TEX_DOC_TYPE, BSConstant.BIB_TEX_DOC_BOOK));
		item.addAttribute(mr.createReferenceAttribute(
				BSConstant.BIB_TEX_PUBLISHER, strt.nextToken().trim()));
		item.addAttribute(mr.createReferenceAttribute(BSConstant.BIB_TEX_TITLE,
				strt.nextToken().trim()));
		item.addAttribute(mr.createReferenceAttribute(BSConstant.BIB_TEX_YEAR,
				strt.nextToken().trim()));

	}

	/**
	 * Autor, Autor_Title_Year.pdf
	 * 
	 * @param item
	 * @param fileName
	 */
	protected static void parseFileNameF5(ReferenceItem item, String fileName,
			ModelRoot mr) {
		StringTokenizer strt = new StringTokenizer(fileName, "_.");
		item.addAttribute(mr.createReferenceAttribute(
				BSConstant.ATTR_BIB_TEX_DOC_TYPE,
				BSConstant.BIB_TEX_DOC_ARTICLE));
		item.addAttribute(mr.createReferenceAttribute(
				BSConstant.BIB_TEX_AUTHOR, strt.nextToken().trim()));
		item.addAttribute(mr.createReferenceAttribute(BSConstant.BIB_TEX_TITLE,
				strt.nextToken().trim()));
		item.addAttribute(mr.createReferenceAttribute(BSConstant.BIB_TEX_YEAR,
				strt.nextToken().trim()));

	}

}
