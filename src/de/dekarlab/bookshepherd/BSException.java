package de.dekarlab.bookshepherd;

public class BSException extends Exception {

	/**
	 * Serial Id.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 * 
	 * @param e
	 *            exception
	 */
	public BSException(Exception e) {
		super(e);
	}

}
