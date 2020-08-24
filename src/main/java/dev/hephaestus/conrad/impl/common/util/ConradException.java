package dev.hephaestus.conrad.impl.common.util;

public class ConradException extends RuntimeException {
	@SuppressWarnings("ConfusingArgumentToVarargsMethod")
	public ConradException(String msg, String... args) {
		super(String.format(msg, args));
	}
}
