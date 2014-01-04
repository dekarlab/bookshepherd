package de.dekarlab.bookshepherd.action;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import de.dekarlab.bookshepherd.Resource;
import de.dekarlab.bookshepherd.citationhtml.harvard.HarvardFormatter;
import de.dekarlab.bookshepherd.model.ReferenceItem;
import bibtex.dom.BibtexFile;

public class ExportHtml {

	public static void export(List<ReferenceItem> lst, Shell shell) {
		FileDialog dialog = new FileDialog(shell, SWT.SAVE);
		dialog.setFilterNames(new String[] { "HTML Files", "All Files (*.*)" });
		dialog.setFilterExtensions(new String[] { "*.html", "*.*" });
		String selected = dialog.open();
		if (selected == null) {
			return;
		}
		if (!selected.endsWith(".html")) {
			selected += ".html";
		}

		if (new File(selected).exists()) {
			MessageBox messageBox = new MessageBox(shell, SWT.OK | SWT.CANCEL
					| SWT.ICON_QUESTION);
			messageBox.setText(Resource.getText("message.overwrite"));
			messageBox.setMessage(Resource.getText("message.overwrite"));
			if (messageBox.open() == SWT.CANCEL) {
				export(lst, shell);
				return;
			}
		}

		PrintWriter fw = null;
		FileOutputStream fos = null;
		Writer out = null;
		try {
			fos = new FileOutputStream(selected);
			out = new OutputStreamWriter(fos, "UTF-8");
			fw = new PrintWriter(out);
			printHarvard(lst, fw);
			Program.launch(selected);
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

	public static void printHarvard(List<ReferenceItem> lst, PrintWriter fw) {
		fw.println("<html>");
		fw.println("<head>");

		fw
				.println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8 \"/> ");
		fw.println("</head>");

		BibtexFile bib = new BibtexFile();
		fw.println("<body>");

		for (ReferenceItem item : lst) {
			fw.print(HarvardFormatter.format(ExportBibTex.getEntry(item, bib)));
			fw.println("<br/>");
		}
		fw.println("</body></html>");
	}
}
