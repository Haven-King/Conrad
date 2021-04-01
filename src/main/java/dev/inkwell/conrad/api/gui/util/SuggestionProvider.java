package dev.inkwell.conrad.api.gui.util;

import java.util.List;

@FunctionalInterface
public interface SuggestionProvider {
    List<String> getSuggestions(String currentValue);
}
