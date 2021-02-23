package org.github.msx80.annocommand;

public class NoMatchingMethodException extends AnnoCommandException {


	private static final long serialVersionUID = -937989977705688411L;

	public final String baseCommand;
	
	public NoMatchingMethodException(String baseCommand) {
		super();
		this.baseCommand = baseCommand;
	}

	public NoMatchingMethodException(String baseCommand,Exception cause) {
		super(cause);
		this.baseCommand = baseCommand;
	}

	public NoMatchingMethodException(String baseCommand, String message, Exception cause) {
		super(message, cause);
		this.baseCommand = baseCommand;
	}

	public NoMatchingMethodException(String baseCommand, String message) {
		super(message);
		this.baseCommand = baseCommand;
	}

}
