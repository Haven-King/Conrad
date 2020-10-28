package dev.hephaestus.conrad.api;

import dev.hephaestus.conrad.impl.common.config.ValueKey;

public interface SaveCallback<T> {
	void onSave(ValueKey valueKey, Object oldValue, Object newValue);
}
