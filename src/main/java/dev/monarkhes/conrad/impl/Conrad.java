package dev.monarkhes.conrad.impl;

import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Conrad {
	public static final Logger LOGGER = LogManager.getLogger("conrad");

	public static final Identifier id(String... path) {
		return new Identifier("conrad", String.join(".", path));
	}
}
