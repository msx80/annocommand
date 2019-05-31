package org.github.msx80.annocommand;

public class NoMatchingMethodException extends AnnoCommandException {


	private static final long serialVersionUID = -937989977705688411L;

	public NoMatchingMethodException() {
		super();
	}

	public NoMatchingMethodException(Exception cause) {
		super(cause);
	}

	public NoMatchingMethodException(String message, Exception cause) {
		super(message, cause);
	}

	public NoMatchingMethodException(String message) {
		super(message);
	}

}
