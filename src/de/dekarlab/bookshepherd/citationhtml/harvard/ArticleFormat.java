package de.dekarlab.bookshepherd.citationhtml.harvard;

import java.util.Iterator;

import de.dekarlab.bookshepherd.BSConstant;
import bibtex.dom.BibtexEntry;
import bibtex.dom.BibtexPerson;
import bibtex.dom.BibtexPersonList;
import bibtex.dom.BibtexString;


public class ArticleFormat {
	public static String format(BibtexEntry entry) {
		StringBuffer res = new StringBuffer();
		BibtexPersonList authors = (BibtexPersonList) entry
				.getFieldValue(BSConstant.BIB_TEX_AUTHOR);
		if (authors != null) {
			Iterator<?> it = authors.getList().iterator();
			BibtexPerson person;
			String tmp;
			while (it.hasNext()) {
				person = (BibtexPerson) it.next();
				tmp = person.getLast();
				if (tmp != null) {
					res.append(tmp.trim());
					res.append(", ");
				}
				tmp = person.getFirst();
				if (tmp != null) {
					res.append(tmp.trim());
				}
				res.append(", ");
			}
		}
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
		val = (BibtexString) entry.getFieldValue(BSConstant.BIB_TEX_JOURNAL);
		if (val != null && !val.equals("")) {
			res.append("<i>");
			res.append(val.getContent());
			res.append(", ");
			res.append("</i>");
		}

		val = (BibtexString) entry.getFieldValue(BSConstant.BIB_TEX_VOLUME);
		if (val != null && !val.equals("")) {
			res.append(val.getContent());
		}

		val = (BibtexString) entry.getFieldValue(BSConstant.BIB_TEX_NUMBER);
		if (val != null && !val.equals("")) {
			res.append(" (");
			res.append(val.getContent());
			res.append("), ");
		}
		val = (BibtexString) entry.getFieldValue(BSConstant.BIB_TEX_PAGES);
		if (val != null && !val.equals("")) {
			res.append("pp. ");
			res.append(val.getContent());
		}

		return res.toString();
	}
}
