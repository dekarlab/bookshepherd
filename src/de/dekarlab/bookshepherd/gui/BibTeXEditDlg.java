package de.dekarlab.bookshepherd.gui;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import de.dekarlab.bookshepherd.BSConstant;
import de.dekarlab.bookshepherd.Property;
import de.dekarlab.bookshepherd.Resource;
import de.dekarlab.bookshepherd.action.ExportBibTex;
import de.dekarlab.bookshepherd.action.GSSearch;
import de.dekarlab.bookshepherd.action.SetBasePath;
import de.dekarlab.bookshepherd.model.ReferenceAttribute;
import de.dekarlab.bookshepherd.model.ReferenceItem;
import de.dekarlab.bookshepherd.util.Util;
import bibtex.dom.BibtexAbstractValue;
import bibtex.dom.BibtexEntry;
import bibtex.dom.BibtexFile;
import bibtex.dom.BibtexString;
import bibtex.parser.BibtexParser;
import bibtex.parser.ParseException;

public class BibTeXEditDlg {
	private boolean closeOk;
	private ReferenceItem item;
	private Shell shell;
	private Button ok;
	private Button cancel;
	private Button updateGs;
	private Button show;
	private Property property;
	private Text text;

	public BibTeXEditDlg(Shell shell, ReferenceItem item, Property property) {
		this.item = item;
		this.shell = shell;
		this.property = property;
		shell.setText(Resource.getText("bibtex.title"));
		this.closeOk = false;
	}

	/**
	 * Update GUI properties.
	 * 
	 * @param property
	 * @param display
	 * @param mf
	 */
	public void updateProperty() {
	}

	public boolean isCloseOk() {
		return closeOk;
	}

	public boolean init() {
		shell.setBounds(property.getEditDlgWindowX(), property
				.getEditDlgWindowY(), property.getEditDlgWindowW(), property
				.getEditDlgWindowH());
		shell.setLayout(new GridLayout());
		shell.setLayoutData(new GridData(GridData.FILL_BOTH));

		text = new Text(shell, SWT.MULTI | SWT.BORDER);
		text.setLayoutData(new GridData(GridData.FILL_BOTH));

		Composite buttons = new Composite(shell, SWT.NONE);
		buttons.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayout gdb = new GridLayout();
		gdb.numColumns = 4;
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

		updateGs = new Button(buttons, SWT.PUSH);
		updateGs.setText(Resource.getText("btn.update.gs"));
		updateGs.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event arg0) {
				//save old attribute
				List<ReferenceAttribute> attrOld = new ArrayList<ReferenceAttribute>();
				for (ReferenceAttribute attr : item.getAttributes()) {
					attrOld.add(attr);
				}
				//update attributes from view (to get the last version of title)
				updateModelFromView();
				//find using title
				GSSearch.update(item);
				updateView();
				item.setAttributes(attrOld);
			}
		});

		show = new Button(buttons, SWT.PUSH);
		show.setText(Resource.getText("btn.show"));
		show.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event arg0) {
				String file = item.getAttribute(BSConstant.ATTR_FILE);
				if (file != null && !file.equals("")) {
					Program.launch(SetBasePath.getPath(file, property
							.getBasePath()));
				}
			}
		});

		ok = new Button(buttons, SWT.PUSH);
		ok.setText(Resource.getText("btn.ok"));
		ok.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event arg0) {
				if (updateModelFromView()) {
					closeOk = true;
					shell.close();
				}
			}
		});
		updateView();
		return true;
	}

	protected void updateView() {
		BibtexFile bib = new BibtexFile();
		List<ReferenceAttribute> attrs = new ArrayList<ReferenceAttribute>();
		ReferenceAttribute attr;
		for (int i = 0; i < item.getAttributes().size(); i++) {
			attr = item.getAttributes().get(i);
			if (attr.getValue() == null || attr.getValue().equals("")) {
				continue;
			}
			attrs.add(attr);
		}
		item.setAttributes(attrs);
		BibtexEntry entry = ExportBibTex.getEntry(item, bib);

		if (entry != null) {
			StringWriter stringWriter = new StringWriter();
			PrintWriter printWriter = new PrintWriter(stringWriter);
			entry.printBibtex(printWriter);
			text.setText(stringWriter.getBuffer().toString());
		}
	}

	protected boolean updateModelFromView() {
		BibtexParser parser = new BibtexParser(true);
		BibtexFile bib = new BibtexFile();
		StringReader reader = new StringReader(text.getText());
		try {
			parser.parse(bib, reader);
			if (bib.getEntries().size() != 1) {
				return false;
			}
			BibtexEntry entry = (BibtexEntry) bib.getEntries().get(0);
			Object[] keys = entry.getFields().keySet().toArray();
			BibtexAbstractValue value;
			item.getAttributes().clear();
			for (int i = 0; i < keys.length; i++) {
				value = entry.getFieldValue((String) keys[i]);
				if (value instanceof BibtexString) {
					if (keys[i].equals(BSConstant.BIB_TEX_AUTHOR)
							|| keys[i].equals(BSConstant.BIB_TEX_EDITOR)) {
						item.setAttribute((String) keys[i], Util
								.parseBibTeXString(((BibtexString) value)
										.getContent()));
					} else {
						item.setAttribute((String) keys[i],
								((BibtexString) value).getContent());
					}
				}
			}
			item.setAttribute(BSConstant.ATTR_BIB_TEX_DOC_TYPE, entry
					.getEntryType());

		} catch (ParseException e) {
			Logger.getLogger("bookshepherd").log(Level.SEVERE, e.getMessage(),
					e);

			return false;
		} catch (IOException e) {
			Logger.getLogger("bookshepherd").log(Level.SEVERE, e.getMessage(),
					e);

			return false;
		}
		return true;
	}

}
