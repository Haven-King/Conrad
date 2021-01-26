package dev.monarkhes.conrad.impl.util;

public class ConradException extends RuntimeException {
    public ConradException(String msg, Object... args) {
		super(String.format(msg, args));
	}

	public ConradException(IllegalArgumentException exception) {
		super(exception);
	}
}
