package de.dekarlab.bookshepherd.gui;

import java.text.Collator;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class ColumnSorter implements Listener {
	public static final String TABLE_COLUMN_ATTR_ITEM = "item";

	@SuppressWarnings("unchecked")
	private Comparator currentComparator = null;

	private Collator col = Collator.getInstance(Locale.getDefault());

	public static final int INT_COMPARATOR = 0;
	public static final int STRING_COMPARATOR = 1;
	public static final int DATE_COMPARATOR = 2;
	public static final int DOUBLE_COMPARATOR = 3;
	public static final int HOUR_COMPARATOR = 4;

	private ColumnSorter(int _comp) {
		switch (_comp) {
		case INT_COMPARATOR:
			currentComparator = intComparator;
			break;

		case STRING_COMPARATOR:
			currentComparator = strComparator;
			break;

		case DATE_COMPARATOR:
			currentComparator = dateComparator;
			break;

		case DOUBLE_COMPARATOR:
			currentComparator = doubleComparator;
			break;

		case HOUR_COMPARATOR:
			currentComparator = hourComparator;
			break;

		default:
			currentComparator = strComparator;
		}
	}

	public static Listener getListener(int _comp) {
		return new ColumnSorter(_comp);
	}

	private int colIndex = 0;
	private int updown = 1;

	// Integer Comparator
	@SuppressWarnings("unchecked")
	private Comparator intComparator = new Comparator() {
		public int compare(Object arg0, Object arg1) {

			TableItem t1 = (TableItem) arg0;
			TableItem t2 = (TableItem) arg1;

			int v1 = Integer.parseInt(t1.getText(colIndex));
			int v2 = Integer.parseInt(t2.getText(colIndex));

			if (v1 < v2)
				return 1 * updown;
			if (v1 > v2)
				return -1 * updown;

			return 0;
		}
	};

	// String Comparator
	@SuppressWarnings("unchecked")
	private Comparator strComparator = new Comparator() {
		public int compare(Object arg0, Object arg1) {

			TableItem t1 = (TableItem) arg0;
			TableItem t2 = (TableItem) arg1;

			String v1 = (t1.getText(colIndex));
			String v2 = (t2.getText(colIndex));

			return (col.compare(v1, v2)) * updown;
		}
	};

	// Double Comparator
	@SuppressWarnings("unchecked")
	private Comparator doubleComparator = new Comparator() {
		public int compare(Object arg0, Object arg1) {

			TableItem t1 = (TableItem) arg0;
			TableItem t2 = (TableItem) arg1;

			double v1 = Double.parseDouble(t1.getText(colIndex));
			double v2 = Double.parseDouble(t2.getText(colIndex));

			if (v1 < v2)
				return 1 * updown;
			if (v1 > v2)
				return -1 * updown;

			return 0;
		}
	};

	// Hour Comparator (hh:mm:ss)
	@SuppressWarnings("unchecked")
	private Comparator hourComparator = new Comparator() {
		public int compare(Object arg0, Object arg1) {

			TableItem t1 = (TableItem) arg0;
			TableItem t2 = (TableItem) arg1;

			String v1 = (t1.getText(colIndex)).trim();
			String v2 = (t2.getText(colIndex)).trim();

			DateFormat df = new SimpleDateFormat("hh:mm:ss");

			Date d1 = null;
			Date d2 = null;

			try {
				d1 = df.parse(v1);
			} catch (ParseException e) {
				Logger.getLogger("bookshepherd").log(Level.WARNING, "v1 " + v1);
				try {
					d1 = df.parse("01:01:01");
				} catch (ParseException e1) {
				}
			}

			try {
				d2 = df.parse(v2);
			} catch (ParseException e) {
				Logger.getLogger("bookshepherd").log(Level.WARNING, "v2 " + v2);
				try {
					d2 = df.parse("01:01:01");
				} catch (ParseException e1) {
				}
			}

			if (d1.equals(d2))
				return 0;

			return updown * (d1.before(d2) ? 1 : -1);
		}
	};
	@SuppressWarnings("unchecked")
	private Comparator dateComparator = new Comparator() {
		public int compare(Object arg0, Object arg1) {
			TableItem t1 = (TableItem) arg0;
			TableItem t2 = (TableItem) arg1;

			String v1 = (t1.getText(colIndex)).trim();
			String v2 = (t2.getText(colIndex)).trim();

			v1.replaceAll("-", "/");
			v2.replaceAll("-", "/");

			DateFormat df = new SimpleDateFormat("dd/MM/yyyy");

			Date d1 = null;
			Date d2 = null;

			try {
				d1 = df.parse(v1);
			} catch (ParseException e) {
				try {
					d1 = df.parse("01/01/1900");
				} catch (ParseException e1) {
				}
			}

			try {
				d2 = df.parse(v2);
			} catch (ParseException e) {
				try {
					d2 = df.parse("01/01/1900");
				} catch (ParseException e1) {
				}
			}

			if (d1.equals(d2))
				return 0;

			return updown * (d1.before(d2) ? 1 : -1);
		}
	};

	@SuppressWarnings("unchecked")
	public void handleEvent(Event e) {
		// sort column
		TableColumn currentColumn = (TableColumn) e.widget;
		Table table = currentColumn.getParent();
		table.setSortColumn(currentColumn);
		if (table.getSortDirection() == SWT.UP
				&& table.getSortColumn().getText().equals(
						currentColumn.getText())) {
			table.setSortDirection(SWT.DOWN);
		} else {
			table.setSortDirection(SWT.UP);
		}

		updown = (updown == 1 ? -1 : 1);

		colIndex = searchColumnIndex(currentColumn);

		table.setRedraw(false);

		TableItem[] items = table.getItems();

		Arrays.sort(items, currentComparator);

		table.setItemCount(items.length);

		for (int i = 0; i < items.length; i++) {
			TableItem item = new TableItem(table, SWT.NONE, i);
			item.setText(getData(items[i]));
			// update data
			item.setData(TABLE_COLUMN_ATTR_ITEM, items[i]
					.getData(TABLE_COLUMN_ATTR_ITEM));
			items[i].dispose();
		}

		table.setRedraw(true);
	}

	private String[] getData(TableItem t) {
		Table table = t.getParent();

		int colCount = table.getColumnCount();
		String[] s = new String[colCount];

		for (int i = 0; i < colCount; i++)
			s[i] = t.getText(i);

		return s;

	}

	private int searchColumnIndex(TableColumn currentColumn) {
		Table table = currentColumn.getParent();

		int in = 0;

		for (int i = 0; i < table.getColumnCount(); i++)
			if (table.getColumn(i) == currentColumn)
				in = i;

		return in;
	}

}
