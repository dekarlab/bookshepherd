package de.dekarlab.bookshepherd.action;

import java.io.File;
import java.util.List;

import de.dekarlab.bookshepherd.BSConstant;
import de.dekarlab.bookshepherd.model.ReferenceItem;
import de.dekarlab.bookshepherd.util.Util;

public class RenameFiles {

	public static void rename(String absolutePath, ReferenceItem item,
			String basePath) {
		File file = new File(absolutePath);
		if (!file.exists()) {
			return;
		}
		String nameOld = file.getName();
		int k = nameOld.lastIndexOf(".");
		String ext = "";
		if (k != -1) {
			ext = nameOld.substring(k + 1, nameOld.length());
		}
		// Title
		String nameNew = item.getAttribute(BSConstant.BIB_TEX_TITLE);
		// String year = item.getAttribute(BSConstant.BIB_TEX_YEAR);
		// String month = item.getAttribute(BSConstant.BIB_TEX_MONTH);
		if (nameNew != null && !nameNew.equals("")) {
			nameNew = Util.replace(nameNew, ":", " ");
			nameNew = Util.replace(nameNew, "/", " ");
			nameNew = Util.replace(nameNew, "\\", " ");
			// if (year == null) {
			// year = "";
			// }
			// if (month == null) {
			// month = "";
			// }

			// File newFile = new File(file.getParent() + File.separator +
			// nameNew
			// + " " + year + "_" + month + "." + ext);
			File newFile = new File(file.getParent() + File.separator + nameNew
					+ "." + ext);

			file.renameTo(newFile);
			// update File attribute
			item.setAttribute(BSConstant.ATTR_FILE, SetBasePath.getBasedPath(
					newFile.getAbsolutePath(), basePath));

		}
	}

	public static void renameFiles(List<ReferenceItem> list, String basePath) {
		for (ReferenceItem item : list) {
			String file = item.getAttribute(BSConstant.ATTR_FILE);
			if (file != null && !file.equals("")) {
				rename(SetBasePath.getPath(file, basePath), item, basePath);
			}
		}
	}
}
