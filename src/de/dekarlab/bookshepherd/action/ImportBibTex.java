package de.dekarlab.bookshepherd.action;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.dekarlab.bookshepherd.BSConstant;
import de.dekarlab.bookshepherd.controller.ModelController;
import de.dekarlab.bookshepherd.model.ReferenceGroup;
import de.dekarlab.bookshepherd.model.ReferenceItem;
import de.dekarlab.bookshepherd.model.ReferenceItemGroup;
import de.dekarlab.bookshepherd.util.Util;
import bibtex.dom.BibtexAbstractValue;
import bibtex.dom.BibtexEntry;
import bibtex.dom.BibtexFile;
import bibtex.dom.BibtexString;
import bibtex.parser.BibtexParser;

public class ImportBibTex {
	public static List<ReferenceItem> importBibTex(ReferenceGroup group,
			File bibtex, ModelController mr) {
		BibtexParser parser = new BibtexParser(true);
		BibtexFile bibtexFile = new BibtexFile();
		FileInputStream fis = null;
		InputStreamReader isr = null;
		BufferedReader br = null;
		try {
			fis = new FileInputStream(bibtex);
			isr = new InputStreamReader(fis, "UTF-8");
			br = new BufferedReader(isr);
			parser.parse(bibtexFile, br);
		} catch (Exception e) {
			Logger.getLogger("bookshepherd").log(Level.SEVERE,
					e.getMessage(), e);			

		} finally {
			try {
				if (isr != null) {
					isr.close();
				}
				if (fis != null) {
					fis.close();
				}
				if (br != null) {
					br.close();
				}
			} catch (IOException e) {
				// Nothing
			}
		}

		List<ReferenceItem> lst = new ArrayList<ReferenceItem>();
		BibtexEntry entry;
		ReferenceItem item = null;
		ReferenceItemGroup rig;
		for (int j = 0; j < bibtexFile.getEntries().size(); j++) {
			if (!(bibtexFile.getEntries().get(j) instanceof BibtexEntry)) {
				Logger.getLogger("bookshepherd").log(Level.WARNING,
						bibtexFile.getEntries().get(j).getClass().getName());
				continue;
			}
			entry = (BibtexEntry) bibtexFile.getEntries().get(j);
			Object[] keys = entry.getFields().keySet().toArray();
			BibtexAbstractValue value;
			item = mr.getModelRoot().createReferenceItem();
			rig = mr.getModelRoot().createReferenceItemGroup();
			item.addGroup(rig);
			group.addItem(rig);
			lst.add(item);
			for (int i = 0; i < keys.length; i++) {
				value = entry.getFieldValue((String) keys[i]);
				if (value instanceof BibtexString) {
					if (keys[i].equals(BSConstant.BIB_TEX_AUTHOR)
							|| keys[i].equals(BSConstant.BIB_TEX_EDITOR)) {
						item
								.setAttribute(
										(String) keys[i],
										Util
												.removeBibTeXEscapes(Util
														.parseBibTeXString(((BibtexString) value)
																.getContent())));
					} else {
						item.setAttribute((String) keys[i], Util
								.removeBibTeXEscapes(((BibtexString) value)
										.getContent()));
					}
				}
			}
			item.setAttribute(BSConstant.ATTR_BIB_TEX_DOC_TYPE, entry
					.getEntryType());
		}
		return lst;
	}
}
