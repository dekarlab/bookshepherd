package de.dekarlab.bookshepherd.action;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

import de.dekarlab.bookshepherd.BSConstant;
import de.dekarlab.bookshepherd.model.ReferenceAttribute;
import de.dekarlab.bookshepherd.model.ReferenceItem;
import de.dekarlab.bookshepherd.util.Util;
import bibtex.dom.BibtexEntry;
import bibtex.dom.BibtexFile;

public class ExportBibTex {
	public static void export(List<ReferenceItem> lst, Shell shell) {
		FileDialog dialog = new FileDialog(shell, SWT.SAVE);
		dialog
				.setFilterNames(new String[] { "TeX Bib Files",
						"All Files (*.*)" });
		dialog.setFilterExtensions(new String[] { "*.bib", "*.*" });
		String selected = dialog.open();
		if (selected == null) {
			return;
		}

		PrintWriter fw = null;
		FileOutputStream fos = null;
		Writer out = null;
		try {
			fos = new FileOutputStream(selected);
			out = new OutputStreamWriter(fos, "UTF-8");
			fw = new PrintWriter(out);
			BibtexFile bib = new BibtexFile();
			for (ReferenceItem item : lst) {
				getEntry(item, bib);
			}
			bib.printBibtex(fw);
		} catch (IOException e) {
			Logger.getLogger("bookshepherd").log(Level.SEVERE,
					e.getMessage(), e);			
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					Logger.getLogger("bookshepherd").log(Level.SEVERE,
							e.getMessage(), e);		
				}
			}
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					Logger.getLogger("bookshepherd").log(Level.SEVERE,
							e.getMessage(), e);		
				}
			}
			if (fw != null) {
				fw.close();
			}
		}
	}

	public static BibtexEntry getEntry(ReferenceItem item, BibtexFile bib) {
		List<ReferenceAttribute> attrs = item.getAttributes();
		BibtexEntry entry = null;
		// create entry
		for (ReferenceAttribute attr : attrs) {
			if (attr.getValue().equals("")) {
				continue;
			}
			if (attr.getName().equals(BSConstant.ATTR_BIB_TEX_DOC_TYPE)) {
				// entry = bib.makeEntry(attr.getValue(), attr.getValue());
				entry = bib.makeEntry(attr.getValue(), Util.generateName(item));
				bib.addEntry(entry);
				break;
			}
		}
		if (entry != null) {
			for (ReferenceAttribute attr : attrs) {
				if (attr.getName().equals(BSConstant.ATTR_BIB_TEX_DOC_TYPE)) {
					continue;
				}
				if (attr.getName().equals(BSConstant.BIB_TEX_AUTHOR)
						|| attr.getName().equals(BSConstant.BIB_TEX_EDITOR)) {
					entry.setField(attr.getName(), Util.parsePersonList(bib,
							attr.getValue()));
				} else {
					entry.setField(attr.getName(), bib.makeString(attr
							.getValue()));
				}
			}
		}
		return entry;
	}

}
