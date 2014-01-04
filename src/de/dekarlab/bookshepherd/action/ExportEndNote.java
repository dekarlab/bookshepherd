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

import de.dekarlab.bookshepherd.endnote.BibEndnote;
import de.dekarlab.bookshepherd.model.ReferenceItem;
import bibtex.dom.BibtexFile;

public class ExportEndNote {

	public static void exportEndNote(List<ReferenceItem> lst, Shell shell) {
		FileDialog dialog = new FileDialog(shell, SWT.SAVE);
		dialog.setFilterNames(new String[] { "XML Files", "All Files (*.*)" });
		dialog.setFilterExtensions(new String[] { "*.xml", "*.*" });
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
				// create entry
				ExportBibTex.getEntry(item, bib);
			}
			BibEndnote.NEWSTYLETAGS = true;
			BibEndnote.printEndnoteXML(bib, fw);
		} catch (IOException e) {
			Logger.getLogger("bookshepherd").log(Level.SEVERE,
					e.getMessage(), e);			

		} finally {
			if (fw != null) {
				fw.close();
			}

			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					Logger.getLogger("bookshepherd").log(Level.SEVERE,
							e.getMessage(), e);		
				}
			}

			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					Logger.getLogger("bookshepherd").log(Level.SEVERE,
							e.getMessage(), e);			
				}
			}
		}
	}
}
