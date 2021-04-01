package dev.inkwell.conrad.api.gui.util;

import dev.inkwell.conrad.api.value.util.Table;

import java.util.List;

@FunctionalInterface
public interface KeySuggestionProvider<T> {
    List<String> getSuggestions(Table<T> currentValue, String currentKey);
}
