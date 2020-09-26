package dev.hephaestus.conrad.api;

import dev.hephaestus.conrad.impl.common.keys.ValueKey;

public interface SaveCallback<T> {
	void onSave(ValueKey valueKey, T oldValue, T newValue);
}
