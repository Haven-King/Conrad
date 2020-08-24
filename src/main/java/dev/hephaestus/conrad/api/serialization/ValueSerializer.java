package dev.hephaestus.conrad.api.serialization;

import com.fasterxml.jackson.databind.JsonNode;

public interface ValueSerializer<E, R extends E, V> {
	R serialize(V value);

	@SuppressWarnings("unchecked")
	default R serializeValue(Object value) {
		return this.serialize((V) value);
	}

	V deserialize(E representation);

	default boolean isCompound() {
		return false;
	}
}
