package de.dekarlab.bookshepherd.action;

import java.util.List;

import org.eclipse.swt.widgets.ProgressBar;

import de.dekarlab.bookshepherd.BSConstant;
import de.dekarlab.bookshepherd.controller.ModelController;
import de.dekarlab.bookshepherd.model.ExcerptItem;
import de.dekarlab.bookshepherd.model.ReferenceItem;

public class SetBasePath {
	public static final String BASE_PATH = "{BASE_PATH}";

	public static void updateBasePath(String basePath, ProgressBar bar, ModelController controller) {

		List<ReferenceItem> lst = controller.getItems();
		String prevPath;
		for (ReferenceItem ri : lst) {
			prevPath = ri.getAttribute(BSConstant.ATTR_FILE);
			if (prevPath != null) {
				ri.setAttribute(BSConstant.ATTR_FILE, getBasedPath(prevPath,
						basePath));
			}
			bar.setSelection(bar.getSelection() + 1);
		}

		List<ExcerptItem> lst2 = controller.getExcerptItems(lst, null);
		for (ExcerptItem ei : lst2) {
			prevPath = ei.getAttribute(BSConstant.ATTR_EXC_IMAGE);
			if (prevPath != null) {
				ei.setAttribute(BSConstant.ATTR_EXC_IMAGE, getBasedPath(prevPath,
						basePath));
			}
			bar.setSelection(bar.getSelection() + 1);
		}

	}

	public static void updateBasePathAbsoulte(String basePath, ProgressBar bar,
			ModelController controller) {
		List<ReferenceItem> lst = controller.getItems();
		String prevPath;
		for (ReferenceItem ri : lst) {
			prevPath = ri.getAttribute(BSConstant.ATTR_FILE);
			if (prevPath != null) {
				ri
						.setAttribute(BSConstant.ATTR_FILE, getPath(prevPath,
								basePath));
			}
			bar.setSelection(bar.getSelection() + 1);
		}

		List<ExcerptItem> lst2 = controller.getExcerptItems(lst, null);
		for (ExcerptItem ei : lst2) {
			prevPath = ei.getAttribute(BSConstant.ATTR_EXC_IMAGE);
			if (prevPath != null) {
				ei.setAttribute(BSConstant.ATTR_EXC_IMAGE, getPath(prevPath,
						basePath));
			}
			bar.setSelection(bar.getSelection() + 1);
		}

	}

	public static String getBasedPath(String path, String basePath) {

		if (basePath == null || basePath.equals("")) {
			return path;
		}
		int k = path.indexOf(basePath);
		if (k == 0) {
			return (BASE_PATH + path
					.substring(basePath.length(), path.length())).replace("\\",
					"/");
		}
		return path.replace("\\", "/");

	}

	public static String getPath(String path, String basePath) {
		int k = path.indexOf(BASE_PATH);
		if (k == 0) {
			return (basePath + path
					.substring(BASE_PATH.length(), path.length())).replace(
					"\\", "/");
		}
		return path.replace("\\", "/");
	}
}
