package dev.hephaestus.conrad.impl.data;

import dev.hephaestus.conrad.api.Config;

public class VoidConfigSerializer implements ConfigSerializer {
	public static final VoidConfigSerializer INSTANCE = new VoidConfigSerializer();

	@Override
	public <T extends Config> void serialize(T config) {

	}

	@Override
	public <T extends Config> T deserialize(Class<T> configClass) {
		return null;
	}
}
