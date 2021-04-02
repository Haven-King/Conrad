package dev.inkwell.conrad.impl.compat;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.inkwell.conrad.impl.ConfigScreenProviderImpl;

import java.util.HashMap;
import java.util.Map;

public class ModMenuCompat implements ModMenuApi {
    private final Map<String, ConfigScreenFactory<?>> factories = new HashMap<>();

    public ModMenuCompat() {
        ConfigScreenProviderImpl.forEach((string, screenFunction) -> {
            factories.put(string, screenFunction::apply);
        });
    }

    @Override
    public Map<String, ConfigScreenFactory<?>> getProvidedConfigScreenFactories() {
        return factories;
    }

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return factories.get("conrad");
    }
}
