package dev.inkwell.conrad.api;

import dev.inkwell.conrad.impl.JsonSerializer;
import org.jetbrains.annotations.NotNull;

public interface Config {
	@NotNull String name();

	@NotNull SaveType saveType();

	@NotNull default ConfigSerializer<?, ?> serializer() {
		return JsonSerializer.INSTANCE;
	}
}
