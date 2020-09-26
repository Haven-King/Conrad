package dev.hephaestus.conrad.impl.common.config;

import dev.hephaestus.conrad.impl.common.keys.ValueKey;

import java.io.IOException;
import java.nio.file.Path;

public class PlayerValueContainer extends ValueContainer {
	public PlayerValueContainer() {
		super(null);
	}

	@Override
	protected void save(ValueKey key, Object value, boolean sync) {

	}
}
