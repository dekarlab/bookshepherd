package de.dekarlab.bookshepherd.citationhtml.harvard;

import java.util.Iterator;

import de.dekarlab.bookshepherd.BSConstant;
import bibtex.dom.BibtexEntry;
import bibtex.dom.BibtexPerson;
import bibtex.dom.BibtexPersonList;
import bibtex.dom.BibtexString;

public class BookFormat {
	public static String format(BibtexEntry entry) {
		StringBuffer res = new StringBuffer();
		BibtexPersonList authors = (BibtexPersonList) entry
				.getFieldValue(BSConstant.BIB_TEX_AUTHOR);
		BibtexPersonList editors = (BibtexPersonList) entry
				.getFieldValue(BSConstant.BIB_TEX_EDITOR);
		if (authors == null) {
			getAuthors(res, editors, true);
		} else {
			getAuthors(res, authors, false);
		}

		BibtexString val = (BibtexString) entry
				.getFieldValue(BSConstant.BIB_TEX_YEAR);
		if (val != null && !val.equals("")) {
			res.append(val.getContent());
			res.append(". ");
		}
		val = (BibtexString) entry.getFieldValue(BSConstant.BIB_TEX_TITLE);
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

	public static void getAuthors(StringBuffer res, BibtexPersonList authors,
			boolean editor) {
		int i = 0;
		if (authors != null) {
			Iterator<?> it = authors.getList().iterator();
			BibtexPerson person;
			String tmp;
			int size = authors.getList().size();
			while (it.hasNext()) {
				i++;
				if (size <= 2) {
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
					if (!editor) {
						res.append(", ");
					} else {
						if (size == 1) {
							res.append(" ed., ");
						} else {
							if (!it.hasNext()) {
								res.append(" eds., ");
							}
						}
					}
				} else if (size >= 3 && size <= 4) {
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
					if (i == 2) {
						res.append(" & ");
					} else {
						if (!editor) {
							res.append(", ");
						} else {
							if (it.hasNext()) {
								res.append(", ");
							} else {
								res.append(" eds., ");
							}
						}
					}
				} else {
					if (i == 1) {
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
					} else {
						if (!editor) {
							res.append(" et al., ");
						} else {
							res.append(" et al. eds., ");
						}
						break;
					}
				}
			}
		}

	}

}
