package dev.monarkhes.conrad.impl;

import net.fabricmc.fabric.api.config.v1.SyncConfigValues;
import net.fabricmc.loader.api.config.ConfigDefinition;
import net.fabricmc.loader.api.config.ConfigManager;
import net.fabricmc.loader.api.config.value.ValueContainer;
import net.fabricmc.loader.config.ValueContainerProviders;

public class Conrad {
    public static void syncAndSave(ConfigDefinition config) {
        SyncConfigValues.sendConfigValues(config);
        ConfigManager.save(config, container(config));
    }

    public static ValueContainer container(ConfigDefinition config) {
        ValueContainer container = ValueContainerProviders.getInstance(config.getSaveType()).getValueContainer();

        if (container.getSaveDirectory() == null) {
            // We were given a remote container
            container = ValueContainer.ROOT;
        }

        return container;
    }
}
