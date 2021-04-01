package dev.inkwell.conrad.api.gui.util;

import dev.inkwell.conrad.api.value.util.Table;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.List;

public final class RegistryKeySuggestor<T> implements KeySuggestionProvider<T> {
    private final Registry<?> registry;

    public RegistryKeySuggestor(Registry<?> registry) {
        this.registry = registry;
    }

    @Override
    public List<String> getSuggestions(Table<T> currentValue, String currentKey) {
        List<String> suggestions = new ArrayList<>();

        for (Identifier id : this.registry.getIds()) {
            String string = id.toString();

            if (!currentValue.containsKey(string) && (!string.equals(currentKey) && string.startsWith(currentKey) || id.getPath().startsWith(currentKey))) {
                suggestions.add(string);
            }
        }

        return suggestions;
    }
}
