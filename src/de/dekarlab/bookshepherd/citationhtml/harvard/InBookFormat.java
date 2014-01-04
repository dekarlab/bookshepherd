package de.dekarlab.bookshepherd.citationhtml.harvard;

import de.dekarlab.bookshepherd.BSConstant;
import bibtex.dom.BibtexEntry;
import bibtex.dom.BibtexPersonList;
import bibtex.dom.BibtexString;

public class InBookFormat {
	public static String format(BibtexEntry entry) {
		StringBuffer res = new StringBuffer();
		BibtexPersonList authors = (BibtexPersonList) entry
				.getFieldValue(BSConstant.BIB_TEX_AUTHOR);
		BookFormat.getAuthors(res, authors, false);

		BibtexString val = (BibtexString) entry
				.getFieldValue(BSConstant.BIB_TEX_YEAR);
		if (val != null && !val.equals("")) {
			res.append(val.getContent());
			res.append(". ");
		}
		val = (BibtexString) entry.getFieldValue(BSConstant.BIB_TEX_TITLE);
		if (val != null && !val.equals("")) {
			res.append(val.getContent());
			res.append(". ");
		}
		res.append("In: ");
		BibtexPersonList editors = (BibtexPersonList) entry
		.getFieldValue(BSConstant.BIB_TEX_AUTHOR);
		BookFormat.getAuthors(res, editors, true);

		val = (BibtexString) entry.getFieldValue(BSConstant.BIB_TEX_BOOKTITLE);
		if (val != null && !val.equals("")) {
			res.append("<i>");
			res.append(val.getContent());
			res.append(".</i> ");
		}
		
		val = (BibtexString) entry.getFieldValue(BSConstant.BIB_TEX_EDITION);
		if (val != null && !val.equals("")) {
			res.append(val.getContent());
			res.append(". ");
		}

		val = (BibtexString) entry.getFieldValue(BSConstant.BIB_TEX_ADDRESS);
		if (val != null && !val.equals("")) {
			res.append(val.getContent());
			res.append(": ");
		}

		val = (BibtexString) entry.getFieldValue(BSConstant.BIB_TEX_PUBLISHER);
		if (val != null && !val.equals("")) {
			res.append(val.getContent());
			res.append(". ");
		}

		return res.toString();
	}

}
