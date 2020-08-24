package dev.hephaestus.clothy.api;

import dev.hephaestus.conrad.annotations.NotNull;

public interface ReferenceProvider<T> {
    @NotNull
	AbstractConfigEntry<T> provideReferenceEntry();
}
