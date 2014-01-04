package de.dekarlab.bookshepherd.citationhtml.harvard;

import de.dekarlab.bookshepherd.BSConstant;
import bibtex.dom.BibtexEntry;

public class HarvardFormatter {

	public static String format(BibtexEntry entry) {
		if (entry.getEntryType().equals(BSConstant.BIB_TEX_DOC_ARTICLE)) {
			return ArticleFormat.format(entry);
		} else if (entry.getEntryType().equals(BSConstant.BIB_TEX_DOC_BOOK)) {
			return BookFormat.format(entry);
		} else if (entry.getEntryType().equals(BSConstant.BIB_TEX_DOC_INBOOK)
				|| entry.getEntryType().equals(
						BSConstant.BIB_TEX_DOC_INCOLLECTION)) {
			return InBookFormat.format(entry);
		} else {
			return MiscFormat.format(entry);
		}
	}

}
