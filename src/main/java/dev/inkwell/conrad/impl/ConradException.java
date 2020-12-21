package dev.inkwell.conrad.impl;

public class ConradException extends RuntimeException {
	@SuppressWarnings("ConfusingArgumentToVarargsMethod")
	public ConradException(String msg, String... args) {
		super(String.format(msg, args));
	}
}
