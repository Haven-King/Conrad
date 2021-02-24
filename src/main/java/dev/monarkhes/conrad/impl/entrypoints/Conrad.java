package dev.monarkhes.conrad.impl.entrypoints;

import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.config.v1.SyncConfigValues;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.config.ConfigDefinition;
import net.fabricmc.loader.api.config.ConfigManager;
import net.fabricmc.loader.api.config.entrypoint.ConfigPostInitializer;
import net.fabricmc.loader.api.config.value.ValueContainer;
import net.fabricmc.loader.config.ValueContainerProviders;

public class Conrad implements ConfigPostInitializer {
    public static void syncAndSave(ConfigDefinition<?> config) {
        ValueContainer valueContainer = container(config);
        SyncConfigValues.sendConfigValues(config, valueContainer);
        ConfigManager.save(config, valueContainer);
    }

    public static ValueContainer container(ConfigDefinition<?> config) {
        ValueContainer container = ValueContainerProviders.getInstance(config.getSaveType()).getValueContainer();

        if (container.getSaveDirectory() == null) {
            // We were given a remote container
            container = ValueContainer.ROOT;
        }

        return container;
    }

    @Override
    public void onConfigsLoaded() {
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            for (ConfigDefinition<?> configDefinition : ConfigManager.getConfigKeys()) {
                ConfigScreenProvider.register(ConfigManager.getValues(configDefinition));
            }
        }
    }
}
