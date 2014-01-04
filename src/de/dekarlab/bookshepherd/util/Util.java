package de.dekarlab.bookshepherd.util;

import java.util.StringTokenizer;

import de.dekarlab.bookshepherd.BSConstant;
import de.dekarlab.bookshepherd.Resource;
import de.dekarlab.bookshepherd.model.ReferenceItem;
import bibtex.dom.BibtexFile;
import bibtex.dom.BibtexPersonList;

public class Util {

	public static String getTitle(String fileName, boolean isModified) {
		String out = Resource.getText("title");
		out += " - [ ";
		if (fileName != null) {
			out += fileName;
		}
		if (isModified) {
			out += " *";
		}
		out += " ]";
		return out;
	}

	public static String generateName(ReferenceItem item) {
		String authorStr = item.getAttribute(BSConstant.BIB_TEX_AUTHOR);
		if (authorStr == null) {
			return "";
		} else {
			authorStr = authorStr.replace(" ", "");
			authorStr = authorStr.replace(",", "");
		}
		String yearStr = item.getAttribute(BSConstant.BIB_TEX_YEAR);
		if (yearStr == null) {
			return "";
		}
		return authorStr + yearStr;
	}

	public static String getDocFileType(String fileName) {
		if (fileName == null || fileName.equals("")) {
			return BSConstant.FILE_TYPE_PAPER;
		}

		if (fileName.endsWith(".doc")) {
			return BSConstant.FILE_TYPE_DOC;
		} else if (fileName.endsWith(".djvu")) {
			return BSConstant.FILE_TYPE_DJVU;
		} else if (fileName.endsWith(".pdf")) {
			return BSConstant.FILE_TYPE_PDF;
		} else if (fileName.endsWith(".ppt")) {
			return BSConstant.FILE_TYPE_PPT;
		} else {
			int k = fileName.lastIndexOf(".");
			if (k != -1) {
				return fileName.substring(k+1, fileName.length());
			} else {
				return "";
			}
		}
	}

	public static String xmlEncode(String in) {
		in = replace(in, "\"", "&quot;");
		in = replace(in, "'", "&apos;");
		in = replace(in, "&", "&amp;");
		in = replace(in, "<", "&lt;");
		in = replace(in, ">", "&gt;");
		return in;
	}

	public static String xmlDecode(String in) {
		in = replace(in, "&quot;", "\"");
		in = replace(in, "&apos;", "'");
		in = replace(in, "&amp;", "&");
		in = replace(in, "&lt;", "<");
		in = replace(in, "&gt;", ">");
		return in;
	}

	public static String replace(final String aInput, final String aOldPattern,
			final String aNewPattern) {
		if (aOldPattern.equals("")) {
			throw new IllegalArgumentException("Old pattern must have content.");
		}

		final StringBuffer result = new StringBuffer();
		// startIdx and idxOld delimit various chunks of aInput; these
		// chunks always end where aOldPattern begins
		int startIdx = 0;
		int idxOld = 0;
		while ((idxOld = aInput.indexOf(aOldPattern, startIdx)) >= 0) {
			// grab a part of aInput which does not include aOldPattern
			result.append(aInput.substring(startIdx, idxOld));
			// add aNewPattern to take place of aOldPattern
			result.append(aNewPattern);

			// reset the startIdx to just after the current match, to see
			// if there are any further matches
			startIdx = idxOld + aOldPattern.length();
		}
		// the final chunk will go to the end of aInput
		result.append(aInput.substring(startIdx));
		return result.toString();
	}

	public static String parseBibTeXString(String bibtex) {
		final StringBuffer result = new StringBuffer();
		// startIdx and idxOld delimit various chunks of aInput; these
		// chunks always end where aOldPattern begins
		int startIdx = 0;
		int idxOld = 0;
		while ((idxOld = bibtex.indexOf(" and ", startIdx)) >= 0) {
			// grab a part of aInput which does not include aOldPattern
			result.append(convertFullNameFromBibTeX(bibtex.substring(startIdx,
					idxOld)));
			// add aNewPattern to take place of aOldPattern
			result.append(", ");

			// reset the startIdx to just after the current match, to see
			// if there are any further matches
			startIdx = idxOld + " and ".length();
		}
		result.append(convertFullNameFromBibTeX(bibtex.substring(startIdx)));

		return result.toString();
	}

	public static String convertFullNameFromBibTeX(String fullName) {
		String firstName = "";
		String lastName = "";

		int k = fullName.indexOf(",");
		if (k != -1) {
			lastName = fullName.substring(0, k).trim();
			firstName = fullName.substring(k + 1).trim();
			return lastName + ", " + firstName;
		} else {
			k = fullName.indexOf("van ");
			if (k == -1) {
				k = fullName.indexOf("von ");
				if (k == -1) {
					k = fullName.indexOf("zu ");
					if (k == -1) {
						k = 0;
					} else {
						k += "zu ".length();
					}
				} else {
					k += "von ".length();
				}
			} else {
				k += "van ".length();
			}
			k = fullName.indexOf(" ", k);
			if (k != -1) {
				lastName = fullName.substring(0, k).trim();
				firstName = fullName.substring(k + 1).trim();
				return lastName + ", " + firstName;
			}
		}
		return fullName;
	}

	public static BibtexPersonList parsePersonList(BibtexFile bib,
			String persons) {
		// parse the list of authors
		BibtexPersonList personList = bib.makePersonList();
		StringTokenizer st = new StringTokenizer(persons, ",", false);
		String authorFirst = null;
		String authorLast = null;
		if (st.countTokens() != 0 && st.countTokens() % 2 == 0) {
			while (st.hasMoreTokens()) {
				if (authorLast == null) {
					authorLast = st.nextToken().trim() + " ";
				} else {
					authorFirst = st.nextToken().trim();
				}
				if (authorFirst != null && authorLast != null) {
					personList.add(bib.makePerson(authorFirst, null,
							authorLast, null, false));
					authorFirst = null;
					authorLast = null;
				}
			}
		} else {
			personList.add(bib.makePerson("", null, persons, null, false));
		}

		return personList;
	}

	public static String removeBibTeXEscapes(String content) {
		StringTokenizer strt = new StringTokenizer(content, "\n\r{} \t", false);
		String out = "";
		boolean first = true;
		while (strt.hasMoreTokens()) {
			if (!first) {
				out += " ";
			}
			first = false;
			out += strt.nextToken();
		}
		return out;
	}
}
