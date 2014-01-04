package de.dekarlab.bookshepherd;

public class BSConstant {
	/**
	 * Publisher's address (usually just the city, but can be the full address
	 * for lesser-known publishers)
	 */
	public static final String BIB_TEX_ADDRESS = "address";
	/**
	 * An annotation for annotated bibliography styles (not typical)
	 */
	public static final String BIB_TEX_ANNOTE = "annote";
	/**
	 * The name(s) of the author(s) (in the case of more than one author,
	 * separated by and)
	 */
	public static final String BIB_TEX_AUTHOR = "author";
	/**
	 * The title of the book, if only part of it is being cited
	 */
	public static final String BIB_TEX_BOOKTITLE = "booktitle";
	/**
	 * The chapter number
	 */
	public static final String BIB_TEX_CHAPTER = "chapter";
	/**
	 * The key of the cross-referenced entry
	 */
	public static final String BIB_TEX_CROSSREF = "crossref";
	/**
	 * The edition of a book, long form (such as "first" or "second")
	 */
	public static final String BIB_TEX_EDITION = "edition";
	/**
	 * The name(s) of the editor(s)
	 */
	public static final String BIB_TEX_EDITOR = "editor";
	/**
	 * A specification of an electronic publication, often a preprint or a
	 * technical report
	 */
	public static final String BIB_TEX_EPRINT = "eprint";
	/**
	 * How it was published, if the publishing method is nonstandard
	 */
	public static final String BIB_TEX_HOWPUBLISHED = "howpublished";
	/**
	 * The institution that was involved in the publishing, but not necessarily
	 * the publisher
	 */
	public static final String BIB_TEX_INSTITUTION = "institution";
	/**
	 * The journal or magazine the work was published in
	 */
	public static final String BIB_TEX_JOURNAL = "journal";
	/**
	 * A hidden field used for specifying or overriding the alphabetical order
	 * of entries (when the "author" and "editor" fields are missing). Note that
	 * this is very different from the key (mentioned just after this list) that
	 * is used to cite or cross-reference the entry.
	 */
	public static final String BIB_TEX_KEY = "key";
	/**
	 * The month of publication (or, if unpublished, the month of creation)
	 */
	public static final String BIB_TEX_MONTH = "month";
	/**
	 * Miscellaneous extra information
	 */
	public static final String BIB_TEX_NOTE = "note";
	/**
	 * The "number" of a journal, magazine, or tech-report, if applicable. (Most
	 * publications have a "volume", but no "number" field.)
	 */
	public static final String BIB_TEX_NUMBER = "number";
	/**
	 * The conference sponsor
	 */
	public static final String BIB_TEX_ORGANIZATION = "organization";
	/**
	 * Page numbers, separated either by commas or double-hyphens. For books,
	 * the total number of pages.
	 */
	public static final String BIB_TEX_PAGES = "pages";
	/**
	 * The publisher's name
	 */
	public static final String BIB_TEX_PUBLISHER = "publisher";
	/**
	 * The school where the thesis was written
	 */
	public static final String BIB_TEX_SCHOOL = "school";
	/**
	 * The series of books the book was published in (e.g. "The Hardy Boys" or
	 * "Lecture Notes in Computer Science")
	 */
	public static final String BIB_TEX_SERIES = "series";
	/**
	 * The title of the work
	 */
	public static final String BIB_TEX_TITLE = "title";
	/**
	 * The type of tech-report, for example, "Research Note"
	 */
	public static final String BIB_TEX_TYPE = "type";
	/**
	 * The WWW address
	 */
	public static final String BIB_TEX_URL = "url";
	/**
	 * The volume of a journal or multi-volume book
	 */
	public static final String BIB_TEX_VOLUME = "volume";
	/**
	 * The year of publication (or, if unpublished, the year of creation)
	 */
	public static final String BIB_TEX_YEAR = "year";

	public static String[] BIB_TEX_PROPS = new String[] { BIB_TEX_ADDRESS,
			BIB_TEX_ANNOTE, BIB_TEX_AUTHOR, BIB_TEX_BOOKTITLE, BIB_TEX_CHAPTER,
			BIB_TEX_CROSSREF, BIB_TEX_EDITION, BIB_TEX_EDITOR, BIB_TEX_EPRINT,
			BIB_TEX_HOWPUBLISHED, BIB_TEX_INSTITUTION, BIB_TEX_JOURNAL,
			BIB_TEX_KEY, BIB_TEX_MONTH, BIB_TEX_NOTE, BIB_TEX_NUMBER,
			BIB_TEX_ORGANIZATION, BIB_TEX_PAGES, BIB_TEX_PUBLISHER,
			BIB_TEX_SCHOOL, BIB_TEX_SERIES, BIB_TEX_TITLE, BIB_TEX_TYPE,
			BIB_TEX_URL, BIB_TEX_VOLUME, BIB_TEX_YEAR };

	public static final String BIB_TEX_DOC_ARTICLE = "article";

	public static final String[] BIB_TEX_DOC_ARTICLE_REQ = new String[] {
			BIB_TEX_AUTHOR, BIB_TEX_TITLE, BIB_TEX_JOURNAL, BIB_TEX_YEAR };

	public static final String[] BIB_TEX_DOC_ARTICLE_OPT = new String[] {
			BIB_TEX_VOLUME, BIB_TEX_NUMBER, BIB_TEX_PAGES, BIB_TEX_MONTH,
			BIB_TEX_NOTE };

	public static final String BIB_TEX_DOC_BOOK = "book";
	public static final String[] BIB_TEX_DOC_BOOK_REQ = new String[] {
			BIB_TEX_AUTHOR, // or
			BIB_TEX_EDITOR, BIB_TEX_TITLE, BIB_TEX_PUBLISHER, BIB_TEX_YEAR };

	public static final String[] BIB_TEX_DOC_BOOK_OPT = new String[] {
			BIB_TEX_VOLUME, // or
			BIB_TEX_NUMBER, BIB_TEX_SERIES, BIB_TEX_ADDRESS, BIB_TEX_EDITION,
			BIB_TEX_MONTH, BIB_TEX_NOTE };

	public static final String BIB_TEX_DOC_BOOKLET = "booklet";
	public static final String[] BIB_TEX_DOC_BOOKLET_REQ = new String[] { BIB_TEX_TITLE };

	public static final String[] BIB_TEX_DOC_BOOKLET_OPT = new String[] {
			BIB_TEX_AUTHOR, // or
			BIB_TEX_HOWPUBLISHED, BIB_TEX_ADDRESS, BIB_TEX_MONTH, BIB_TEX_YEAR,
			BIB_TEX_NOTE };

	public static final String BIB_TEX_DOC_CONFERENCE = "conference";

	public static final String BIB_TEX_DOC_INBOOK = "inbook";
	public static final String[] BIB_TEX_DOC_INBOOK_REQ = new String[] {
			BIB_TEX_AUTHOR, // or
			BIB_TEX_EDITOR, BIB_TEX_TITLE, BIB_TEX_CHAPTER, // and or
			BIB_TEX_PAGES, BIB_TEX_PUBLISHER, BIB_TEX_YEAR };

	public static final String[] BIB_TEX_DOC_INBOOK_OPT = new String[] {
			BIB_TEX_VOLUME, // or
			BIB_TEX_NUMBER, BIB_TEX_SERIES, BIB_TEX_TYPE, BIB_TEX_ADDRESS,
			BIB_TEX_EDITION, BIB_TEX_MONTH, BIB_TEX_NOTE };

	public static final String BIB_TEX_DOC_INCOLLECTION = "incollection";

	public static final String[] BIB_TEX_DOC_INCOLLECTION_REQ = new String[] {
			BIB_TEX_AUTHOR, BIB_TEX_TITLE, BIB_TEX_BOOKTITLE,
			BIB_TEX_PUBLISHER, BIB_TEX_YEAR };

	public static final String[] BIB_TEX_DOC_INCOLLECTION_OPT = new String[] {
			BIB_TEX_EDITOR,
			BIB_TEX_VOLUME, // or
			BIB_TEX_NUMBER, BIB_TEX_SERIES, BIB_TEX_TYPE, BIB_TEX_CHAPTER,
			BIB_TEX_PAGES, BIB_TEX_ADDRESS, BIB_TEX_EDITION, BIB_TEX_MONTH,
			BIB_TEX_NOTE };

	public static final String BIB_TEX_DOC_INPROCEEDINGS = "inproceedings";

	public static final String[] BIB_TEX_DOC_INPROCEEDINGS_REQ = new String[] {
			BIB_TEX_AUTHOR, BIB_TEX_TITLE, BIB_TEX_BOOKTITLE, BIB_TEX_YEAR };

	public static final String[] BIB_TEX_DOC_INPROCEEDINGS_OPT = new String[] {
			BIB_TEX_EDITOR,
			BIB_TEX_VOLUME, // or
			BIB_TEX_NUMBER, BIB_TEX_SERIES, BIB_TEX_PAGES, BIB_TEX_ADDRESS,
			BIB_TEX_MONTH, BIB_TEX_ORGANIZATION, BIB_TEX_PUBLISHER,
			BIB_TEX_NOTE };

	public static final String BIB_TEX_DOC_MANUAL = "manual";

	public static final String[] BIB_TEX_DOC_MANUAL_REQ = new String[] { BIB_TEX_TITLE };

	public static final String[] BIB_TEX_DOC_MANUAL_OPT = new String[] {
			BIB_TEX_AUTHOR, BIB_TEX_ORGANIZATION, BIB_TEX_ADDRESS,
			BIB_TEX_EDITION, BIB_TEX_MONTH, BIB_TEX_YEAR, BIB_TEX_NOTE };

	public static final String BIB_TEX_DOC_MASTERTHESIS = "mastersthesis";

	public static final String[] BIB_TEX_DOC_MASTERTHESIS_REQ = new String[] {
			BIB_TEX_AUTHOR, BIB_TEX_TITLE, BIB_TEX_SCHOOL, BIB_TEX_YEAR };

	public static final String[] BIB_TEX_DOC_MASTERTHESIS_OPT = new String[] {
			BIB_TEX_TYPE, BIB_TEX_ADDRESS, BIB_TEX_MONTH, BIB_TEX_NOTE };

	public static final String BIB_TEX_DOC_MISC = "misc";
	public static final String[] BIB_TEX_DOC_MISC_REQ = new String[] {};

	public static final String[] BIB_TEX_DOC_MISC_OPT = new String[] {
			BIB_TEX_AUTHOR, BIB_TEX_TITLE, BIB_TEX_HOWPUBLISHED, BIB_TEX_MONTH,
			BIB_TEX_YEAR, BIB_TEX_NOTE };

	public static final String BIB_TEX_DOC_PHDTHESIS = "phdthesis";
	public static final String[] BIB_TEX_DOC_PHDTHESIS_REQ = new String[] {
			BIB_TEX_AUTHOR, BIB_TEX_TITLE, BIB_TEX_SCHOOL, BIB_TEX_YEAR };

	public static final String[] BIB_TEX_DOC_PHDTHESIS_OPT = new String[] {
			BIB_TEX_TYPE, BIB_TEX_ADDRESS, BIB_TEX_MONTH, BIB_TEX_NOTE };

	public static final String BIB_TEX_DOC_PROCEEDINGS = "proceedings";

	public static final String[] BIB_TEX_DOC_PROCEEDINGS_REQ = new String[] {
			BIB_TEX_TITLE, BIB_TEX_YEAR };

	public static final String[] BIB_TEX_DOC_PROCEEDINGS_OPT = new String[] {
			BIB_TEX_EDITOR,
			BIB_TEX_VOLUME, // or
			BIB_TEX_NUMBER, BIB_TEX_SERIES, BIB_TEX_ADDRESS, BIB_TEX_MONTH,
			BIB_TEX_ORGANIZATION, BIB_TEX_PUBLISHER, BIB_TEX_NOTE };

	public static final String BIB_TEX_DOC_TECHREPORT = "techreport";

	public static final String[] BIB_TEX_DOC_TECHREPORT_REQ = new String[] {
			BIB_TEX_AUTHOR, BIB_TEX_TITLE, BIB_TEX_INSTITUTION, BIB_TEX_YEAR };

	public static final String[] BIB_TEX_DOC_TECHREPORT_OPT = new String[] {
			BIB_TEX_TYPE, BIB_TEX_NUMBER, BIB_TEX_ADDRESS, BIB_TEX_MONTH,
			BIB_TEX_NOTE };

	public static final String BIB_TEX_DOC_UNPUBLISHED = "unpublished";
	public static final String[] BIB_TEX_DOC_UNPUBLISHED_REQ = new String[] {
			BIB_TEX_AUTHOR, BIB_TEX_TITLE, BIB_TEX_NOTE };

	public static final String[] BIB_TEX_DOC_UNPUBLISHED_OPT = new String[] {
			BIB_TEX_MONTH, BIB_TEX_YEAR, };

	public static final String[] BIB_TEX_DOC_TYPES = new String[] {
			BIB_TEX_DOC_ARTICLE, BIB_TEX_DOC_BOOK, BIB_TEX_DOC_BOOKLET,
			BIB_TEX_DOC_CONFERENCE, BIB_TEX_DOC_INBOOK,
			BIB_TEX_DOC_INCOLLECTION, BIB_TEX_DOC_INPROCEEDINGS,
			BIB_TEX_DOC_MANUAL, BIB_TEX_DOC_MASTERTHESIS, BIB_TEX_DOC_MISC,
			BIB_TEX_DOC_PHDTHESIS, BIB_TEX_DOC_PROCEEDINGS,
			BIB_TEX_DOC_TECHREPORT, BIB_TEX_DOC_UNPUBLISHED };

	public static final String ATTR_SHORT_NAME = "shortname";
	public static final String ATTR_FILE = "file";
	public static final String ATTR_FILE_TYPE = "filetype";
	public static final String ATTR_BIB_TEX_DOC_TYPE = "bibtexdoctype";
	public static final String FILE_TYPE_DJVU = "djvu";
	public static final String FILE_TYPE_PDF = "pdf";
	public static final String FILE_TYPE_DOC = "doc";
	public static final String FILE_TYPE_PPT = "ppt";
	public static final String FILE_TYPE_PAPER = "paper";	

	public static final String ATTR_EXC_TEXT = "text";
	public static final String ATTR_EXC_IMAGE = "image";
	public static final String ATTR_EXC_REFERENCE = "reference";

}
