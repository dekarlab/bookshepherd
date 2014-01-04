/** $Header: $ */
/*
 * Created on 2004-06-30
 *
 * @author Trent Apted <tapted@it.usyd.edu.au>
 *
 */
//package bibtex;
package de.dekarlab.bookshepherd.endnote;

import java.io.FileReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.dekarlab.bookshepherd.CustomAttributes;
import de.dekarlab.bookshepherd.EndnoteAttributes;
import bibtex.dom.BibtexAbstractValue;
import bibtex.dom.BibtexEntry;
import bibtex.dom.BibtexFile;
import bibtex.dom.BibtexNode;
import bibtex.dom.BibtexPerson;
import bibtex.dom.BibtexPersonList;
import bibtex.dom.BibtexString;
import bibtex.expansions.CrossReferenceExpander;
import bibtex.expansions.ExpansionException;
import bibtex.expansions.MacroReferenceExpander;
import bibtex.expansions.PersonListExpander;
import bibtex.parser.BibtexParser;

class StrStr {
	public String first, second;

	public StrStr(String f, String s) {
		first = f;
		second = s;
	}
}

/**
 * This is a converter of BibTeX to Endnote's hackish XML.
 * 
 * @author Trent Apted <tapted@it.usyd.edu.au>
 */
public final class BibEndnote {

	/** Is the order the XML tags appear in each record important? */
	public static final boolean ENDNOTESUCKS = true;

	/** Can we separate records with a new line character? */
	public static final boolean RECORDSEP = true;

	/** Should we do the "newer" style of <style ..> tags */
	public static boolean NEWSTYLETAGS = false;

	/** Pick a 'REFNUM' to keep EndNote Happy */
	public static int ord = 0;

	/** Hack to display what BibTeX fields were skipped at the end */
	public static Set done;
	/** Hack to get the 'ALTERNATE_TITLE' in the right order for EndNote */
	public static String alternate;

	public static String docType;

	public static String toETag(String v7) {
		return toTag(v7, true, "", "");
	}

	public static String toTag(String v7) {
		return toTag(v7, false, "", "");
	}

	public static String toTag(String v7, String attrs) {
		return toTag(v7, false, attrs, "");
	}

	public static String toTag(String v7, String attrs, String content) {
		return toTag(v7, false, attrs, content);
	}

	public static String toTag(String v7, boolean end, String attrs,
			String content) {
		String endstr = "";
		if (attrs.length() > 0 && attrs.charAt(0) != ' ')
			attrs = " " + attrs;
		if (end) {
			endstr = "/";
			attrs = "";
		}
		if (!end && content.length() > 0) {
			content += toETag(v7);
		}
		if (!NEWSTYLETAGS)
			return "<" + endstr + v7 + attrs + ">" + content;
		return "<" + endstr + v7.toLowerCase().replace('_', '-') + attrs + ">"
				+ content;
	}

	/** Fields common to all BibTeX types, keep in order */
	public static StrStr DEFAULT[] = {
			// new StrStr("address", "PLACE_PUBLISHED"),
			// new StrStr("location", "PLACE_PUBLISHED"),
			new StrStr("address", "PUB_LOCATION"),
			new StrStr("location", "PUB_LOCATION"),

			new StrStr("publisher", "PUBLISHER"),
			new StrStr("institution", "PUBLISHER"),
			new StrStr("school", "PUBLISHER"), new StrStr("volume", "VOLUME"),
			new StrStr("number", "NUMBER"), new StrStr("pages", "PAGES"),
			new StrStr("edition", "EDITION"),
			// new StrStr("month", "DATE"),
			new StrStr("type", "work-type") };

	public static StrStr OTHERS[] = { new StrStr("abstract", "ABSTRACT"), // abstracts
			// are
			// currently
			// disliked..
			new StrStr("note", "NOTES") };

	public static String toStyle(String face) {
		return toTag("style", "face=\"" + face
				+ "\" font=\"default\" size=\"100%\"");
	}

	public static String normalStyle() {
		if (NEWSTYLETAGS)
			return toStyle("normal");
		else
			return "";
	}

	public static String endStyle() {
		if (NEWSTYLETAGS)
			return "</style>";
		else
			return "";
	}

	/** Do a single XML tag */
	public static boolean doValue(Map m, String inkey, String outkey,
			boolean err, PrintWriter out) {
		if (m.containsKey(inkey)) {
			BibtexAbstractValue bav = (BibtexAbstractValue) m.get(inkey);
			if (bav instanceof BibtexString) {
				outkey = getValue(inkey, outkey);
				out.print(toTag(outkey)
						+ toXML(((BibtexString) bav).getContent())
						+ toETag(outkey));
				done.add(inkey);
				return true;
			} else {
				Logger.getLogger("bookshepherd").log(
						Level.INFO,
						" can't do " + inkey + " = " + m.get(inkey) + " --> "
								+ outkey);
			}
		} else {
			if (err)
				Logger.getLogger("bookshepherd").log(Level.INFO,
						" !NO '" + inkey + "'");
		}
		return false;
	}

	/* Tack stuff on the end */
	public static void doOthers(Map m, PrintWriter out) {
		for (int i = 0; i < OTHERS.length; ++i)
			doValue(m, OTHERS[i].first, OTHERS[i].second, false, out);
	}

	/** Do stuff that's the same for all entry types */
	public static void doDefaults(Map m, PrintWriter out, String bibkey) {
		for (int i = 0; i < DEFAULT.length; ++i)
			doValue(m, DEFAULT[i].first, DEFAULT[i].second, false, out);
		if (alternate != null)
			out.print(toTag("ALTERNATE_TITLE") + toXML(alternate)
					+ toETag("ALTERNATE_TITLE"));
		doISXN(m, out);
		out.print(toTag("LABEL") + toXML(bibkey) + toETag("LABEL"));
		doOthers(m, out);
		doURI(m, out);
	}

	public static void doCustom(Map m, PrintWriter out, String bibkey,
			String doctype) {
		String[] attrs = CustomAttributes.getInstance().getPropName(doctype);
		for (int i = 0; i < attrs.length; ++i)
			doValue(m, attrs[i], attrs[i], false, out);
	}

	public static void doAlternateTitle(Map m, String key, PrintWriter out) {
		if (NEWSTYLETAGS) {
			doValue(m, key, "translated-title", false, out);
		} else {
			doAlternate(m, key);
		}
	}

	public static void doAlternate(Map m, String key) {
		if (alternate == null && m.containsKey(key)) {
			BibtexAbstractValue bav = (BibtexAbstractValue) m.get(key);
			if (bav instanceof BibtexString) {
				alternate = ((BibtexString) bav).getContent();
				done.add(key);
			}
		}
	}

	/*
	 * public static void doNewTitle(Map m, PrintWriter out, boolean needtwo) {
	 * boolean doed = false; out.print("<titles>"); if (doValue(m, "title",
	 * "title", true, out)) { if (m.containsKey("journal")) { doed = true;
	 * doValue(m, "journal", "secondarytitle", needtwo, out); doValue(m,
	 * "series", "title", false, out); doValue(m, "booktitle", "title", false,
	 * out); } else { doed = true; doValue(m, "booktitle", "title", needtwo,
	 * out); doValue(m, "series", "title", false, out); } } else if (doValue(m,
	 * "booktitle", "title", true, out)) { if (doValue(m, "journal", "title",
	 * needtwo, out)) { doed = true; } doValue(m, "series", "title", false,
	 * out); } else { doValue(m, "journal", "title", true, out); doValue(m,
	 * "series", "title", false, out); } out.print("</titles>"); if (doed)
	 * doEditor(m, out); }
	 */

	public static void doTitle(Map m, PrintWriter out, boolean needtwo) {
		boolean delayed = false;
		boolean doed = false;
		if (NEWSTYLETAGS) {
			// doNewTitle(m, out, needtwo);
			// return;
			delayed = true;
			out.print("<titles>");
		}
		if (doValue(m, "title", "TITLE", true, out)) {
			if (m.containsKey("journal")) {
				doed = doed || !needtwo && (delayed || doEditor(m, out));

				doValue(m, "journal", "SECONDARY_TITLE", needtwo, out);
				if (m.containsKey("series"))
					doAlternateTitle(m, "series", out);
				else
					doAlternateTitle(m, "booktitle", out);
			} else {
				doed = doed || !needtwo && (delayed || doEditor(m, out));
				doValue(m, "booktitle", "SECONDARY_TITLE", needtwo, out);
				doAlternateTitle(m, "series", out);
			}
		} else if (doValue(m, "booktitle", "TITLE", true, out)) {
			if (doValue(m, "journal", "SECONDARY_TITLE", needtwo, out)) {
				doed = doed || delayed || doEditor(m, out);

				doAlternateTitle(m, "series", out);
			} else {
				doValue(m, "series", "SECONDARY_TITLE", false, out);
			}
		} else {
			doValue(m, "journal", "TITLE", true, out);
			doValue(m, "series", "SECONDARY_TITLE", false, out);
		}
		if (NEWSTYLETAGS)
			out.print("</titles>");
		if (delayed && doed)
			doEditor(m, out);

	}

	public static void doISXN(Map m, PrintWriter out) {
		if (doValue(m, "isbn", "ISBN", false, out))
			doValue(m, "issn", "CALL_NUMBER", false, out);
		else
			doValue(m, "issn", "ISBN", false, out);
	}

	static String bibUriOrder[] = {
	// "doi",
			"url", "urlpdf", "pdf", "source", "bibsource" };

	static String endUriOrder[] = {
	// "URL",
			"AUTHOR_ADDRESS", "CAPTION" };

	public static void doHull(Map m, String bibkeys[], String endkeys[],
			PrintWriter out) {
		int i, j;
		for (i = 0, j = 0; i < bibkeys.length && j < endkeys.length; ++i) {
			if (doValue(m, bibkeys[i], endkeys[j], false, out))
				++j;
		}
	}

	public static void doURI(Map m, PrintWriter out) {
		doValue(m, "doi", "URL", false, out);
		doHull(m, bibUriOrder, endUriOrder, out);
		/*
		 * if (doValue(m, "url", "URL", false, out)) { if (doValue(m, "doi",
		 * "AUTHOR_ADDRESS", false, out)) { if (!doValue(m, "urlpdf", "CAPTION",
		 * false, out)) doValue(m, "pdf", "CAPTION", false, out); } else { //no
		 * doi if (doValue(m, "urlpdf", "AUTHOR_ADDRESS", false, out))
		 * doValue(m, "pdf", "CAPTION", false, out); else doValue(m, "pdf",
		 * "AUTHOR_ADDRESS", false, out); } } else if (doValue(m, "doi", "URL",
		 * false, out)) { if (doValue(m, "urlpdf", "AUTHOR_ADDRESS", false,
		 * out)) doValue(m, "pdf", "CAPTION", false, out); else doValue(m,
		 * "pdf", "AUTHOR_ADDRESS", false, out); } else { //no url or doi if
		 * (doValue(m, "urlpdf", "URL", false, out)) doValue(m, "pdf",
		 * "AUTHOR_ADDRESS", false, out); else //no url or doi or pdfurl
		 * doValue(m, "pdf", "URL", false, out); }
		 */
	}

	public static void doInnerAuthor(BibtexPersonList authors, PrintWriter out,
			boolean sec) {
		boolean first = true;
		if (authors == null) {
		}
		// String tag = (sec ? "SECONDARY_" : "") + "AUTHOR";
		String tag = "AUTHOR";
		out.print(toTag(tag));
		for (Iterator it = authors.getList().iterator(); it.hasNext();) {
			if (first) {
				first = false;
			} else {
				out.print(toETag(tag) + toTag(tag));
			}
			BibtexPerson p = (BibtexPerson) it.next();

			if (p.isOthers()) {
				out.print("others");
			} else {
				StringBuffer namestr = new StringBuffer();
				if (p.getPreLast() != null)
					namestr.append(p.getPreLast() + " ");
				/* always put in a comma, even if there is no first name */
				namestr.append(p.getLast().trim() + ", ");
				if (p.getFirst() != null)
					namestr.append(p.getFirst());
				if (p.getLineage() != null)
					namestr.append(", " + p.getLineage());
				out.print(toXML(namestr.toString()));
				// p.printBibtex(out);
			}
		}
		out.print(toETag(tag));
	}

	public static void doRecNumber(PrintWriter out) {
		out.print(toTag("rec-number") + ++ord + toETag("rec-number"));
	}

	public static void doAuthor(Map m, PrintWriter out, boolean editor) {
		if (!m.containsKey("author")) {
			Logger.getLogger("bookshepherd").log(Level.INFO, " !NO author ");
			return;
		}
		Object alist = m.get("author");
		if (!(alist instanceof BibtexPersonList)) {
			Logger.getLogger("bookshepherd").log(
					Level.INFO,
					"\n\t!ERROR IN BibTeX: Author is not a PersonList = `"
							+ alist.toString() + "'");
			Logger
					.getLogger("bookshepherd")
					.log(Level.INFO,
							"\t !! Are you using the 'and' operator (not commas) to separate authors?\n\t");
			return;
		}
		if (NEWSTYLETAGS)
			doContributors(m, out, alist, editor);
		else
			doOldAuthor(m, out, alist);
	}

	public static void doContributors(Map m, PrintWriter out, Object alist,
			boolean editor) {
		out.print("<contributors><authors>");
		doInnerAuthor((BibtexPersonList) alist, out, false);
		out.print("</authors>");
		if (NEWSTYLETAGS && editor) {
			if (m.containsKey("editor")) {
				doEditor(m, out);
			}
		}
		out.print("</contributors>");
		done.add("editor");
		done.add("author");
	}

	public static void doOldAuthor(Map m, PrintWriter out, Object alist) {
		out.print("<REFNUM>" + ++ord + "</REFNUM>");
		out.print("<AUTHORS>");
		doInnerAuthor((BibtexPersonList) alist, out, false);
		out.print("</AUTHORS>");
		done.add("author");
	}

	public static boolean doEditor(Map m, PrintWriter out) {
		if (!m.containsKey("editor")) {
			Logger.getLogger("bookshepherd").log(Level.INFO, " !NO editor ");
			return false;
		}
		Object edlist = m.get("editor");
		if (!(edlist instanceof BibtexPersonList)) {
			Logger.getLogger("bookshepherd").log(
					Level.INFO,
					"\n\t!ERROR IN BibTeX: Editor is not a PersonList = `"
							+ edlist.toString() + "'");
			Logger
					.getLogger("bookshepherd")
					.log(Level.INFO,
							"\t !! Are you using the 'and' operator (not commas) to separate editors?\n\t");
			return false;
		}
		out.print("<secondary-authors>");
		doInnerAuthor((BibtexPersonList) m.get("editor"), out, true);
		out.print("</secondary-authors>");
		done.add("editor");
		return true;
	}

	public static void doYear(Map m, PrintWriter out) {
		if (NEWSTYLETAGS) {
			out.print(toTag("dates"));
			doValue(m, "year", "YEAR", true, out);
			if (m.containsKey("month")) {
				out.print(toTag("pub-dates"));
				doValue(m, "month", "DATE", false, out);
				out.print(toETag("pub-dates"));
			}
			out.print(toETag("dates"));
		} else {
			doValue(m, "year", "YEAR", true, out);
			doValue(m, "month", "DATE", false, out);
		}
	}

	public static void doStart(Map m, PrintWriter out, int reftype,
			String v8name, boolean editor) {
		if (NEWSTYLETAGS) {
			out.print(toTag("database",
					"name=\"bib.enl\" path=\"C:\\bib.enl\"", "bib.enl"));
			out.print(toTag("source-app", "name=\"EndNote\" version=\"8.0\"",
					"EndNote"));
			doRecNumber(out);
			out.print(toTag("ref-type", "name=\"" + v8name + "\"") + reftype
					+ toETag("ref-type"));
		} else {
			out.print("<REFERENCE_TYPE>" + reftype + "</REFERENCE_TYPE>");
		}
		doAuthor(m, out, editor);
		doYear(m, out);
	}

	public static void doArticle(Map m, PrintWriter out, String bibkey) {
		// doStart(m, out, 0, "Journal Article");
		doStart(m, out, 17, "Journal Article", true);
		doTitle(m, out, true);
		doDefaults(m, out, bibkey);
	}

	public static void doInProc(Map m, PrintWriter out, String bibkey) {
		// doStart(m, out, 3, "Conference Paper");
		// doStart(m, out, 47, "Conference Paper");
		doStart(m, out, 10, "Conference Proceedings", true);
		doTitle(m, out, true);
		doDefaults(m, out, bibkey);
	}

	public static void doConf(Map m, PrintWriter out, String bibkey) {
		doStart(m, out, 47, "Conference Paper", true);
		doTitle(m, out, true);
		doDefaults(m, out, bibkey);
	}

	/*
	 * 2006-01-20: on suggestion from Claudius Regn cregn@regn.de:
	 * 
	 * incollection is basically EndNote's book chapter type. This is also what
	 * EndNote does when exporting a file via the export function using the
	 * output style "BibTeX": it converts its own book chapter type to an
	 * incollection.
	 * 
	 * In my EndNote 9, there is a "Book Section", which is output as ref-type
	 * 5, I'm still hoping to maintain backward compatability with Endnote 7
	 * (EndNote 9 appears to be backward compatible for its own XML, despite
	 * some sweeping changes in the XML format, including support now for
	 * abstracts and keywords)
	 */
	/**
	 * \todo Add optional support for EndNote 9's XML format -- keywords and
	 * abstracts, etc.
	 */
	public static void doInCollection(Map m, PrintWriter out, String bibkey) {
		doStart(m, out, 5, "In Collection", true);
		doTitle(m, out, true);
		doDefaults(m, out, bibkey);
	}

	/*
	 * 2006-01-20: also on suggestion from Claudius:
	 * 
	 * it might be an idea to convert field types that you/EndNote doesn't know
	 * (e.g. custom fields) to EndNote's customizable CUSTOM fields, maybe
	 * depending on a user's checkbox choice.
	 * 
	 * In my EndNote "Generic" comes out as ref-type 13.
	 * 
	 * I hope these changes don't break EndNote 7 (I can't test this easily).
	 */
	public static void doGeneric(Map m, PrintWriter out, String bibkey) {
		doStart(m, out, 13, "Generic", false);
		doTitle(m, out, false);
		doDefaults(m, out, bibkey);
	}

	public static void doTech(Map m, PrintWriter out, String bibkey) {
		// doStart(m, out, 10, "Technical Report");
		doStart(m, out, 27, "Report", false);
		doTitle(m, out, false);
		// save type
		// doValue(m, "type", "work-type", false, out);
		doDefaults(m, out, bibkey);
	}

	public static void doThesis(Map m, PrintWriter out, String bibkey) {
		// doStart(m, out, 2, "Thesis");
		doStart(m, out, 32, "Thesis", false);
		doTitle(m, out, false);
		doDefaults(m, out, bibkey);
	}

	public static void doBook(Map m, PrintWriter out, String bibkey) {
		// doStart(m, out, 1, "Book");
		doStart(m, out, 6, "Book", false);
		doTitle(m, out, false);
		doDefaults(m, out, bibkey);
	}

	public static void doInBook(Map m, PrintWriter out, String bibkey) {
		// doStart(m, out, 3, "Book Chapter");
		doStart(m, out, 5, "Book Chapter", true);
		doTitle(m, out, true);
		doDefaults(m, out, bibkey);
	}

	public static void doProc(Map m, PrintWriter out, String bibkey) {
		// doStart(m, out, 3, "Proceedings");
		doStart(m, out, 10, "Proceedings", true);
		doTitle(m, out, true);
		doDefaults(m, out, bibkey);
	}

	public static void doUnpubl(Map m, PrintWriter out, String bibkey) {
		doStart(m, out, 34, "Unpublished Work", true);
		doTitle(m, out, true);
		doDefaults(m, out, bibkey);
	}

	public static void doMisc(Map m, PrintWriter out, String bibkey) {
		doStart(m, out, 16, "Misc", false);
		doTitle(m, out, false);
		doDefaults(m, out, bibkey);
	}

	public static void endnoteEntry(BibtexEntry e, PrintWriter out) {
		String k = e.getEntryKey();
		Logger.getLogger("bookshepherd").log(
				Level.INFO,
				"\nConverting '" + k + "': " + (k.length() < 9 ? "\t" : "")
						+ (k.length() < 17 ? "\t" : "")
						+ (k.length() < 25 ? "\t" : ""));
		done = new HashSet();
		alternate = null;
		String type = e.getEntryType().toLowerCase();
		if (type.equals("comment")) {
			Logger.getLogger("bookshepherd").log(Level.INFO,
					" <<@comment: skipping>>");
			return;
		}
		out.print(toTag("RECORD"));
		docType = type;
		if (type.equals("article"))
			doArticle(e.getFields(), out, e.getEntryKey());
		else if (type.equals("inproceedings"))
			doInProc(e.getFields(), out, e.getEntryKey());
		else if (type.equals("conference"))
			doConf(e.getFields(), out, e.getEntryKey());
		else if (type.equals("incollection"))
			doInCollection(e.getFields(), out, e.getEntryKey());
		else if (type.equals("techreport"))
			doTech(e.getFields(), out, e.getEntryKey());
		else if (type.equals("phdthesis") || type.equals("mastersthesis"))
			doThesis(e.getFields(), out, e.getEntryKey());
		else if (type.equals("book"))
			doBook(e.getFields(), out, e.getEntryKey());
		else if (type.equals("inbook"))
			doInBook(e.getFields(), out, e.getEntryKey());
		else if (type.equals("proceedings"))
			doProc(e.getFields(), out, e.getEntryKey());
		else if (type.equals("unpublished"))
			doUnpubl(e.getFields(), out, e.getEntryKey());

		else if (type.equals("misc"))
			doMisc(e.getFields(), out, e.getEntryKey());
		else {
			Logger
					.getLogger("bookshepherd")
					.log(
							Level.INFO,
							"\n\tNo customised conversion for '"
									+ type
									+ "'. This may break Endnote. Trying 'Generic'\n\t");
			doGeneric(e.getFields(), out, e.getEntryKey());
		}
		doCustom(e.getFields(), out, e.getEntryKey(), type);

		if (ENDNOTESUCKS && !RECORDSEP)
			out.print(toETag("RECORD"));
		else
			out.println(toETag("RECORD"));
		for (Iterator it = e.getFields().keySet().iterator(); it.hasNext();) {
			String key = (String) it.next();
			if (!done.contains(key))
				Logger.getLogger("bookshepherd").log(Level.INFO,
						" ~" + key + "~");
		}
	}

	public static void printEndnoteXML(BibtexFile bib, PrintWriter out) {
		//PrintWriter err = new PrintWriter(System.err);
		BibtexEntry prev = null;
		if (NEWSTYLETAGS)
			out.println("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
		out.print(toTag("XML") + toTag("RECORDS"));
		if (RECORDSEP || !ENDNOTESUCKS)
			out.println();
		for (Iterator iter = bib.getEntries().iterator(); iter.hasNext();) {
			BibtexNode node = (BibtexNode) iter.next();
			if (node instanceof BibtexEntry) {
				prev = (BibtexEntry) node;
				endnoteEntry(prev, out);
			} else if (prev == null) {
				Logger
						.getLogger("bookshepherd")
						.log(
								Level.INFO,
								"Error - prev node is null (parse error at start or may be in a JabRef meta @comment)");
			} else {
				Logger.getLogger("bookshepherd").log(
						Level.INFO,
						"\nWARNING: Parse error in or after entry '"
								+ prev.getEntryKey()
								+ "': not a valid entry (will try to recover)");
				Logger
						.getLogger("bookshepherd")
						.log(Level.INFO,
								"\tNote: this is usually caused by mismatched curly braces");
				// node.printBibtex(err);
			}
			Thread.yield();
		}
		out.print(toETag("RECORDS") + toETag("XML"));
		if (RECORDSEP || !ENDNOTESUCKS)
			out.println();
		out.flush();
	}

	public static void usage() {
		Logger
				.getLogger("bookshepherd")
				.log(
						Level.INFO,
						"\nUsage: BibEndnote <file.bib>\n"
								+ "\nThe output will be given on stdout, errors and messages will be printed to stderr.\n\n");
	}

	public static void main(String[] args) throws Exception {
		// long startTime = System.currentTimeMillis();
		if (args.length < 1) {
			usage();
			return;
		}
		doFile(new java.io.File(args[args.length - 1]), new PrintWriter(
				System.out));
	}

	public static void doFile(java.io.File file, PrintWriter out)
			throws Exception {
		BibtexFile bibtexFile = new BibtexFile();
		BibtexParser parser = new BibtexParser(false);
		boolean expandMacros = false;
		boolean dropMacros = false;
		boolean expandCrossrefs = false;
		boolean expandPersonLists = false;
		boolean noOutput = false;
		expandPersonLists = expandCrossrefs = expandMacros = dropMacros = true;

		try {
			// String filename = file;
			// System.err.println("Parsing \"" + filename + "\" ... ");
			parser.parse(bibtexFile, new FileReader(file));
		} catch (Exception e) {
			Logger.getLogger("bookshepherd").log(Level.SEVERE, e.getMessage(),
					e);

			// System.err.println("Fatal exception: ");
			// e.printStackTrace();
			// System.err.println(e);
			throw e;
		} finally {
			printNonFatalExceptions(parser.getExceptions());
		}
		Thread.yield();
		try {
			if (expandMacros) {
				Logger.getLogger("bookshepherd").log(Level.INFO,
						"\n\nExpanding macros ...");
				MacroReferenceExpander expander = new MacroReferenceExpander(
						true, true, dropMacros, false);
				expander.expand(bibtexFile);
				printNonFatalExceptions(expander.getExceptions());
				Thread.yield();

			}
			if (expandCrossrefs) {
				Logger.getLogger("bookshepherd").log(Level.INFO,
						"\n\nExpanding crossrefs ...");
				CrossReferenceExpander expander = new CrossReferenceExpander(
						false);
				expander.expand(bibtexFile);
				printNonFatalExceptions(expander.getExceptions());
				Thread.yield();
			}
			if (expandPersonLists) {
				Logger.getLogger("bookshepherd").log(Level.INFO,
						"\n\nExpanding person lists ...");
				PersonListExpander expander = new PersonListExpander(true,
						true, false);
				expander.expand(bibtexFile);
				printNonFatalExceptions(expander.getExceptions());
				Thread.yield();
			}
		} catch (ExpansionException e1) {
			// e1.printStackTrace();
			Logger.getLogger("bookshepherd").log(Level.SEVERE, e1.getMessage(),
					e1);

			// System.err.println(e1);
			throw e1;
		}
		if (noOutput)
			return;
		Logger.getLogger("bookshepherd").log(Level.SEVERE,
				"\n\nGenerating output ...");

		// System.err.println("\n\nGenerating output ...");

		printEndnoteXML(bibtexFile, out);

		// bibtexFile.printBibtex(out);
		Logger.getLogger("bookshepherd").log(Level.INFO,
				"\n\n~bibfield~ means that field could not be converted");
		out.println();
		out.flush();

		// System.gc();
		//System.err.println("Memory used:"+(Runtime.getRuntime().totalMemory()-
		// Runtime.getRuntime().freeMemory()));
		// System.err.println("This run took "+(System.currentTimeMillis()-
		// startTime)+" ms.");
		// System.out.println("Press any key to exit.");
		// try { System.in.read(); } catch(Exception e){ e.printStackTrace();}
	}

	private static void printNonFatalExceptions(Exception[] exceptions) {
		if (exceptions.length > 0) {
			Logger.getLogger("bookshepherd").log(Level.INFO,
					"Non-fatal exceptions: ");
			for (int i = 0; i < exceptions.length; i++) {
				// exceptions[i].printStackTrace();
				Logger.getLogger("bookshepherd")
						.log(Level.INFO, exceptions[i].getMessage(),
								exceptions[i] /* "===================" */);
			}
		}
	}

	static Pattern pattern = Pattern.compile(".*\\{\\\\.*[a-zA-Z]\\}.*");
	static Pattern emphasis = Pattern.compile("%(e)mph%([^%]*)%");
	static Pattern superscript = Pattern
			.compile("\\$([0-9]+)\\^((st)|(nd)|(rd)|(th))\\$");

	public static String toXML(String fieldText) {
		return toXML(fieldText, false);
	}

	public static String toXML(String fieldText, boolean nostyle) {
		fieldText = firstFormat(fieldText);

		// if (!pattern.matcher(fieldText).matches())
		// return restFormat(fieldText);

		for (Iterator i = XML_CHARS.keySet().iterator(); i.hasNext();) {
			String s = (String) i.next();
			fieldText = fieldText.replaceAll(s, (String) XML_CHARS.get(s));
		}
		// RemoveBrackets rb = new RemoveBrackets();
		if (nostyle)
			return restFormat(fieldText);
		else
			return normalStyle() + restFormat(fieldText) + endStyle();
	}

	private static String firstFormat(String s) {
		return s.replaceAll("([^\\\\])\\\\ ", "\\1 ").replaceAll("([^\\\\])~",
				"\\1 ").replaceAll("\\s+", " ").replaceAll("&|\\\\&",
				"&#x0026;").replaceAll("--", "-"/* "&#x2013;" */); // EndNote
		// gets
		// plurals
		// wrong if
		// we don't
		// use a
		// regular
		// hypen -
		// THA

	}

	private static String doStyles(Pattern pat, String styleface,
			int matchgroup, String s, String replace) {
		if (NEWSTYLETAGS)
			return doNewStyles(pat, styleface, matchgroup, s, replace);
		else
			return doOldStyles(pat, styleface, matchgroup, s, replace);
	}

	private static String doNewStyles(Pattern pat, String styleface,
			int matchgroup, String s, String replace) {
		String ret = s;
		Matcher mat = pat.matcher(ret);
		StringBuffer out = new StringBuffer();
		int spos = 0;
		// styles.append("<styles>");
		while (mat.find()) {
			int oldlen = ret.length();
			int supendi = mat.end();
			out.append(ret.substring(spos, mat.start()));
			out.append(endStyle());
			out.append(toStyle(styleface));
			ret = mat.replaceFirst(replace);
			spos = supendi - oldlen + ret.length();
			out.append(ret.substring(mat.start(), spos));
			out.append(endStyle());
			out.append(normalStyle());
			mat = pat.matcher(ret);
		}
		out.append(ret.substring(spos));
		return out.toString();
	}

	private static String doOldStyles(Pattern pat, String styleface,
			int matchgroup, String s, String replace) {
		String ret = s;
		Matcher mat = pat.matcher(ret);
		StringBuffer styles = new StringBuffer();
		styles.append("<styles>");
		while (mat.find()) {
			int supstart = mat.start() + mat.group(1).length();
			styles.append("<style face='" + styleface + "' start='" + supstart
					+ "'></style>");
			styles.append("<style start='"
					+ (supstart + mat.group(matchgroup).length())
					+ "'></style>");
			ret = mat.replaceFirst(replace);
			mat = pat.matcher(ret);
		}
		styles.append("</styles>");
		styles.append(ret);
		ret = styles.toString();
		return ret;
	}

	private static String restFormat(String s) {
		String ret = s.replaceAll("\\\\emph\\{([^}]*)\\}", "%emph%$1%")
				.replaceAll("\\\\url\\{", "").replaceAll("\\\\upppercase\\{",
						"").replaceAll("\\}", "").replaceAll("\\{", "")
				.replaceAll("<", "&#x3c;").replaceAll("\\texttrademark",
						"&#x2122;").replaceAll("\\\\\\\\", "\\\\");
		Matcher emph = emphasis.matcher(ret);
		if (NEWSTYLETAGS) {
			if (emph.find())
				ret = doNewStyles(emphasis, "italic", 2, ret, "$2");
			Matcher supm = superscript.matcher(ret);
			if (supm.find())
				ret = doNewStyles(superscript, "superscript", 2, ret, "$1$2");

		} else {
			if (emph.find()) {
				ret = doOldStyles(emphasis, "2", 2, ret, "$2");

				Matcher supm = superscript.matcher(ret);
				if (supm.find())
					Logger
							.getLogger("bookshepherd")
							.log(Level.INFO,
									"[todo] Not replacing superscripts because emphasis was also found..");
				return ret;
			}
			Matcher supm = superscript.matcher(ret);
			if (supm.find()) {
				ret = doOldStyles(superscript, "32", 2, ret, "$1$2");
			}
		}
		return ret;
	}

	public static String removeLatex(String field) {
		StringBuffer sb = new StringBuffer("");
		char c;
		boolean escaped = false, incommand = false;
		for (int i = 0; i < field.length(); i++) {
			c = field.charAt(i);
			if (escaped && (c == '\\')) {
				sb.append('\\');
				escaped = false;
			} else if (c == '\\') {
				escaped = true;
				incommand = true;
			} else if (Character.isLetter((char) c)) {
				escaped = false;
				if (!incommand)
					sb.append((char) c);
				// Else we are in a command, and should not keep the letter.
			} else {
				if (!incommand || ((c != '{') && !Character.isWhitespace(c)))
					sb.append((char) c);
				incommand = false;
				escaped = false;
			}
		}

		return sb.toString();
		// field.replaceAll("\\\\emph", "").replaceAll("\\\\em",
		// "").replaceAll("\\\\textbf", "");
	}

	public static HashMap HTML_CHARS = new HashMap(),
			XML_CHARS = new HashMap();
	static {

		HTML_CHARS.put("\\{\\\\\\\"\\{a\\}\\}", "&auml;");
		HTML_CHARS.put("\\{\\\\\\\"\\{A\\}\\}", "&Auml;");
		HTML_CHARS.put("\\{\\\\\\\"\\{e\\}\\}", "&euml;");
		HTML_CHARS.put("\\{\\\\\\\"\\{E\\}\\}", "&Euml;");
		HTML_CHARS.put("\\{\\\\\\\"\\{i\\}\\}", "&iuml;");
		HTML_CHARS.put("\\{\\\\\\\"\\{I\\}\\}", "&Iuml;");
		HTML_CHARS.put("\\{\\\\\\\"\\{o\\}\\}", "&ouml;");
		HTML_CHARS.put("\\{\\\\\\\"\\{O\\}\\}", "&Ouml;");
		HTML_CHARS.put("\\{\\\\\\\"\\{u\\}\\}", "&uuml;");
		HTML_CHARS.put("\\{\\\\\\\"\\{U\\}\\}", "&Uuml;");

		HTML_CHARS.put("\\{\\\\\\`\\{e\\}\\}", "&egrave;");
		HTML_CHARS.put("\\{\\\\\\`\\{E\\}\\}", "&Egrave;");
		HTML_CHARS.put("\\{\\\\\\`\\{i\\}\\}", "&igrave;");
		HTML_CHARS.put("\\{\\\\\\`\\{I\\}\\}", "&Igrave;");
		HTML_CHARS.put("\\{\\\\\\`\\{o\\}\\}", "&ograve;");
		HTML_CHARS.put("\\{\\\\\\`\\{O\\}\\}", "&Ograve;");
		HTML_CHARS.put("\\{\\\\\\`\\{u\\}\\}", "&ugrave;");
		HTML_CHARS.put("\\{\\\\\\`\\{U\\}\\}", "&Ugrave;");
		HTML_CHARS.put("\\{\\\\\\'\\{e\\}\\}", "&eacute;");
		HTML_CHARS.put("\\{\\\\\\'\\{E\\}\\}", "&Eacute;");
		HTML_CHARS.put("\\{\\\\\\'\\{i\\}\\}", "&iacute;");
		HTML_CHARS.put("\\{\\\\\\'\\{I\\}\\}", "&Iacute;");
		HTML_CHARS.put("\\{\\\\\\'\\{o\\}\\}", "&oacute;");
		HTML_CHARS.put("\\{\\\\\\'\\{O\\}\\}", "&Oacute;");
		HTML_CHARS.put("\\{\\\\\\'\\{u\\}\\}", "&uacute;");
		HTML_CHARS.put("\\{\\\\\\'\\{U\\}\\}", "&Uacute;");
		HTML_CHARS.put("\\{\\\\\\'\\{a\\}\\}", "&aacute;");
		HTML_CHARS.put("\\{\\\\\\'\\{A\\}\\}", "&Aacute;");

		HTML_CHARS.put("\\{\\\\\\^\\{o\\}\\}", "&ocirc;");
		HTML_CHARS.put("\\{\\\\\\^\\{O\\}\\}", "&Ocirc;");
		HTML_CHARS.put("\\{\\\\\\^\\{u\\}\\}", "&ucirc;");
		HTML_CHARS.put("\\{\\\\\\^\\{U\\}\\}", "&Ucirc;");
		HTML_CHARS.put("\\{\\\\\\^\\{e\\}\\}", "&ecirc;");
		HTML_CHARS.put("\\{\\\\\\^\\{E\\}\\}", "&Ecirc;");
		HTML_CHARS.put("\\{\\\\\\^\\{i\\}\\}", "&icirc;");
		HTML_CHARS.put("\\{\\\\\\^\\{I\\}\\}", "&Icirc;");
		HTML_CHARS.put("\\{\\\\\\~\\{o\\}\\}", "&otilde;");
		HTML_CHARS.put("\\{\\\\\\~\\{O\\}\\}", "&Otilde;");
		HTML_CHARS.put("\\{\\\\\\~\\{n\\}\\}", "&ntilde;");
		HTML_CHARS.put("\\{\\\\\\~\\{N\\}\\}", "&Ntilde;");
		HTML_CHARS.put("\\{\\\\\\~\\{a\\}\\}", "&atilde;");
		HTML_CHARS.put("\\{\\\\\\~\\{A\\}\\}", "&Atilde;");

		HTML_CHARS.put("\\{\\\\\\\"a\\}", "&auml;");
		HTML_CHARS.put("\\{\\\\\\\"A\\}", "&Auml;");
		HTML_CHARS.put("\\{\\\\\\\"e\\}", "&euml;");
		HTML_CHARS.put("\\{\\\\\\\"E\\}", "&Euml;");
		HTML_CHARS.put("\\{\\\\\\\"i\\}", "&iuml;");
		HTML_CHARS.put("\\{\\\\\\\"I\\}", "&Iuml;");
		HTML_CHARS.put("\\{\\\\\\\"o\\}", "&ouml;");
		HTML_CHARS.put("\\{\\\\\\\"O\\}", "&Ouml;");
		HTML_CHARS.put("\\{\\\\\\\"u\\}", "&uuml;");
		HTML_CHARS.put("\\{\\\\\\\"U\\}", "&Uuml;");

		HTML_CHARS.put("\\{\\\\\\`e\\}", "&egrave;");
		HTML_CHARS.put("\\{\\\\\\`E\\}", "&Egrave;");
		HTML_CHARS.put("\\{\\\\\\`i\\}", "&igrave;");
		HTML_CHARS.put("\\{\\\\\\`I\\}", "&Igrave;");
		HTML_CHARS.put("\\{\\\\\\`o\\}", "&ograve;");
		HTML_CHARS.put("\\{\\\\\\`O\\}", "&Ograve;");
		HTML_CHARS.put("\\{\\\\\\`u\\}", "&ugrave;");
		HTML_CHARS.put("\\{\\\\\\`U\\}", "&Ugrave;");
		HTML_CHARS.put("\\{\\\\\\'A\\}", "&eacute;");
		HTML_CHARS.put("\\{\\\\\\'E\\}", "&Eacute;");
		HTML_CHARS.put("\\{\\\\\\'i\\}", "&iacute;");
		HTML_CHARS.put("\\{\\\\\\'I\\}", "&Iacute;");
		HTML_CHARS.put("\\{\\\\\\'o\\}", "&oacute;");
		HTML_CHARS.put("\\{\\\\\\'O\\}", "&Oacute;");
		HTML_CHARS.put("\\{\\\\\\'u\\}", "&uacute;");
		HTML_CHARS.put("\\{\\\\\\'U\\}", "&Uacute;");
		HTML_CHARS.put("\\{\\\\\\'a\\}", "&aacute;");
		HTML_CHARS.put("\\{\\\\\\'A\\}", "&Aacute;");

		HTML_CHARS.put("\\{\\\\\\^o\\}", "&ocirc;");
		HTML_CHARS.put("\\{\\\\\\^O\\}", "&Ocirc;");
		HTML_CHARS.put("\\{\\\\\\^u\\}", "&ucirc;");
		HTML_CHARS.put("\\{\\\\\\^U\\}", "&Ucirc;");
		HTML_CHARS.put("\\{\\\\\\^e\\}", "&ecirc;");
		HTML_CHARS.put("\\{\\\\\\^E\\}", "&Ecirc;");
		HTML_CHARS.put("\\{\\\\\\^i\\}", "&icirc;");
		HTML_CHARS.put("\\{\\\\\\^I\\}", "&Icirc;");
		HTML_CHARS.put("\\{\\\\\\~o\\}", "&otilde;");
		HTML_CHARS.put("\\{\\\\\\~O\\}", "&Otilde;");
		HTML_CHARS.put("\\{\\\\\\~n\\}", "&ntilde;");
		HTML_CHARS.put("\\{\\\\\\~N\\}", "&Ntilde;");
		HTML_CHARS.put("\\{\\\\\\~a\\}", "&atilde;");
		HTML_CHARS.put("\\{\\\\\\~A\\}", "&Atilde;");

		HTML_CHARS.put("\\{\\\\c c\\}", "&ccedil;");
		HTML_CHARS.put("\\{\\\\c C\\}", "&Ccedil;");

		XML_CHARS.put("\\{\\\\\\\"\\{a\\}\\}", "&#x00E4;");
		XML_CHARS.put("\\{\\\\\\\"\\{A\\}\\}", "&#x00C4;");
		XML_CHARS.put("\\{\\\\\\\"\\{e\\}\\}", "&#x00EB;");
		XML_CHARS.put("\\{\\\\\\\"\\{E\\}\\}", "&#x00CB;");
		XML_CHARS.put("\\{\\\\\\\"\\{i\\}\\}", "&#x00EF;");
		XML_CHARS.put("\\{\\\\\\\"\\{I\\}\\}", "&#x00CF;");
		XML_CHARS.put("\\{\\\\\\\"\\{\\\\i\\}\\}", "&#x00EF;");
		XML_CHARS.put("\\{\\\\\\\"\\{I\\}\\}", "&#x00CF;");
		XML_CHARS.put("\\{\\\\\\\"\\{o\\}\\}", "&#x00F6;");
		XML_CHARS.put("\\{\\\\\\\"\\{O\\}\\}", "&#x00D6;");
		XML_CHARS.put("\\{\\\\\\\"\\{u\\}\\}", "&#x00FC;");
		XML_CHARS.put("\\{\\\\\\\"\\{U\\}\\}", "&#x00DC;");

		XML_CHARS.put("\\{\\\\\\`\\{e\\}\\}", "&#x00E8;");
		XML_CHARS.put("\\{\\\\\\`\\{E\\}\\}", "&#x00C8;");
		XML_CHARS.put("\\{\\\\\\`\\{i\\}\\}", "&#x00EC;");
		XML_CHARS.put("\\{\\\\\\`\\{\\\\i\\}\\}", "&#x00EC;");
		XML_CHARS.put("\\{\\\\\\`\\{I\\}\\}", "&#x00CC;");
		XML_CHARS.put("\\{\\\\\\`\\{o\\}\\}", "&#x00F2;");
		XML_CHARS.put("\\{\\\\\\`\\{O\\}\\}", "&#x00D2;");
		XML_CHARS.put("\\{\\\\\\`\\{u\\}\\}", "&#x00F9;");
		XML_CHARS.put("\\{\\\\\\`\\{U\\}\\}", "&#x00D9;");
		XML_CHARS.put("\\{\\\\\\'\\{e\\}\\}", "&#x00E9;");
		XML_CHARS.put("\\{\\\\\\'\\{E\\}\\}", "&#x00C9;");
		XML_CHARS.put("\\{\\\\\\'\\{i\\}\\}", "&#x00ED;");
		XML_CHARS.put("\\{\\\\\\'\\{\\\\i\\}\\}", "&#x00ED;");
		XML_CHARS.put("\\{\\\\\\'\\{I\\}\\}", "&#x00CD;");
		XML_CHARS.put("\\{\\\\\\'\\{o\\}\\}", "&#x00F3;");
		XML_CHARS.put("\\{\\\\\\'\\{O\\}\\}", "&#x00D3;");
		XML_CHARS.put("\\{\\\\\\'\\{u\\}\\}", "&#x00FA;");
		XML_CHARS.put("\\{\\\\\\'\\{U\\}\\}", "&#x00DA;");
		XML_CHARS.put("\\{\\\\\\'\\{a\\}\\}", "&#x00E1;");
		XML_CHARS.put("\\{\\\\\\'\\{A\\}\\}", "&#x00C1;");

		XML_CHARS.put("\\{\\\\\\^\\{o\\}\\}", "&#x00F4;");
		XML_CHARS.put("\\{\\\\\\^\\{O\\}\\}", "&#x00D4;");
		XML_CHARS.put("\\{\\\\\\^\\{u\\}\\}", "&#x00F9;");
		XML_CHARS.put("\\{\\\\\\^\\{U\\}\\}", "&#x00D9;");
		XML_CHARS.put("\\{\\\\\\^\\{e\\}\\}", "&#x00EA;");
		XML_CHARS.put("\\{\\\\\\^\\{E\\}\\}", "&#x00CA;");
		XML_CHARS.put("\\{\\\\\\^\\{i\\}\\}", "&#x00EE;");
		XML_CHARS.put("\\{\\\\\\^\\{\\\\i\\}\\}", "&#x00EE;");
		XML_CHARS.put("\\{\\\\\\^\\{I\\}\\}", "&#x00CE;");
		XML_CHARS.put("\\{\\\\\\~\\{o\\}\\}", "&#x00F5;");
		XML_CHARS.put("\\{\\\\\\~\\{O\\}\\}", "&#x00D5;");
		XML_CHARS.put("\\{\\\\\\~\\{n\\}\\}", "&#x00F1;");
		XML_CHARS.put("\\{\\\\\\~\\{N\\}\\}", "&#x00D1;");
		XML_CHARS.put("\\{\\\\\\~\\{a\\}\\}", "&#x00E3;");
		XML_CHARS.put("\\{\\\\\\~\\{A\\}\\}", "&#x00C3;");

		XML_CHARS.put("\\{\\\\\\\"a\\}", "&#x00E4;");
		XML_CHARS.put("\\{\\\\\\\"A\\}", "&#x00C4;");
		XML_CHARS.put("\\{\\\\\\\"e\\}", "&#x00EB;");
		XML_CHARS.put("\\{\\\\\\\"E\\}", "&#x00CB;");
		XML_CHARS.put("\\{\\\\\\\"i\\}", "&#x00EF;");
		XML_CHARS.put("\\{\\\\\\\"\\\\i\\}", "&#x00EF;");
		XML_CHARS.put("\\{\\\\\\\"I\\}", "&#x00CF;");
		XML_CHARS.put("\\{\\\\\\\"o\\}", "&#x00F6;");
		XML_CHARS.put("\\{\\\\\\\"O\\}", "&#x00D6;");
		XML_CHARS.put("\\{\\\\\\\"u\\}", "&#x00FC;");
		XML_CHARS.put("\\{\\\\\\\"U\\}", "&#x00DC;");

		XML_CHARS.put("\\{\\\\\\`e\\}", "&#x00E8;");
		XML_CHARS.put("\\{\\\\\\`E\\}", "&#x00C8;");
		XML_CHARS.put("\\{\\\\\\`i\\}", "&#x00EC;");
		XML_CHARS.put("\\{\\\\\\`\\\\i\\}", "&#x00EC;");
		XML_CHARS.put("\\{\\\\\\`I\\}", "&#x00CC;");
		XML_CHARS.put("\\{\\\\\\`o\\}", "&#x00F2;");
		XML_CHARS.put("\\{\\\\\\`O\\}", "&#x00D2;");
		XML_CHARS.put("\\{\\\\\\`u\\}", "&#x00F9;");
		XML_CHARS.put("\\{\\\\\\`U\\}", "&#x00D9;");
		XML_CHARS.put("\\{\\\\\\'e\\}", "&#x00E9;");
		XML_CHARS.put("\\{\\\\\\'E\\}", "&#x00C9;");
		XML_CHARS.put("\\{\\\\\\'i\\}", "&#x00ED;");
		XML_CHARS.put("\\{\\\\\\'\\\\i\\}", "&#x00ED;");
		XML_CHARS.put("\\{\\\\\\'I\\}", "&#x00CD;");
		XML_CHARS.put("\\{\\\\\\'o\\}", "&#x00F3;");
		XML_CHARS.put("\\{\\\\\\'O\\}", "&#x00D3;");
		XML_CHARS.put("\\{\\\\\\'u\\}", "&#x00FA;");
		XML_CHARS.put("\\{\\\\\\'U\\}", "&#x00DA;");
		XML_CHARS.put("\\{\\\\\\'a\\}", "&#x00E1;");
		XML_CHARS.put("\\{\\\\\\'A\\}", "&#x00C1;");

		XML_CHARS.put("\\{\\\\\\^o\\}", "&#x00F4;");
		XML_CHARS.put("\\{\\\\\\^O\\}", "&#x00D4;");
		XML_CHARS.put("\\{\\\\\\^u\\}", "&#x00F9;");
		XML_CHARS.put("\\{\\\\\\^U\\}", "&#x00D9;");
		XML_CHARS.put("\\{\\\\\\^e\\}", "&#x00EA;");
		XML_CHARS.put("\\{\\\\\\^E\\}", "&#x00CA;");
		XML_CHARS.put("\\{\\\\\\^i\\}", "&#x00EE;");
		XML_CHARS.put("\\{\\\\\\^\\\\i\\}", "&#x00EE;");
		XML_CHARS.put("\\{\\\\\\^I\\}", "&#x00CE;");
		XML_CHARS.put("\\{\\\\\\~o\\}", "&#x00F5;");
		XML_CHARS.put("\\{\\\\\\~O\\}", "&#x00D5;");
		XML_CHARS.put("\\{\\\\\\~n\\}", "&#x00F1;");
		XML_CHARS.put("\\{\\\\\\~N\\}", "&#x00D1;");
		XML_CHARS.put("\\{\\\\\\~a\\}", "&#x00E3;");
		XML_CHARS.put("\\{\\\\\\~A\\}", "&#x00C3;");

		XML_CHARS.put("\\{\\\\o\\}", "&#x00F8;");
		XML_CHARS.put("\\{\\\\O\\}", "&#x00D8;");
		XML_CHARS.put("\\{\\\\ss\\}", "&#x00DF;");
		XML_CHARS.put("\\{\\\\SS\\}", "SS");

		// XML_CHARS.put("\\u00E1", "&#x00E1;");
	}

	public static String getValue(String bibtex, String def) {
		String ret = null;
		try {
			ret = EndnoteAttributes.getInstance().getPropName(docType, bibtex);
		} catch (Exception ex) {
			ret = def;
		}
		if (ret == null) {
			ret = def;
		}
		return ret;
	}

}
