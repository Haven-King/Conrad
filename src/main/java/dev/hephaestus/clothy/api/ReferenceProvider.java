package dev.hephaestus.clothy.api;

import org.jetbrains.annotations.NotNull;

public interface ReferenceProvider<T> {
    @NotNull
	AbstractConfigEntry<T> provideReferenceEntry();
}
