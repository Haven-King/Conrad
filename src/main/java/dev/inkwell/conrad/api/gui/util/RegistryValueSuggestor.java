package dev.inkwell.conrad.api.gui.util;

import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

public class RegistryValueSuggestor<T> implements SuggestionProvider {
    private final Registry<T> registry;
    private final BiPredicate<Registry<T>, Identifier> predicate;

    public RegistryValueSuggestor(Registry<T> registry, BiPredicate<Registry<T>, Identifier> predicate) {
        this.registry = registry;
        this.predicate = predicate;
    }

    public RegistryValueSuggestor(Registry<T> registry) {
        this(registry, (r, id) -> true);
    }

    @Override
    public List<String> getSuggestions(String currentValue) {
        List<String> suggestions = new ArrayList<>();

        for (Identifier id : this.registry.getIds()) {
            String string = id.toString();

            if (predicate.test(this.registry, id) && (!string.equals(currentValue) && string.startsWith(currentValue) || id.getPath().startsWith(currentValue))) {
                suggestions.add(string);
            }
        }

        return suggestions;
    }
}
