package dev.monarkhes.conrad.api;

import dev.monarkhes.conrad.impl.entrypoints.ConfigScreenProvider;
import dev.monarkhes.vivid.screen.ScreenStyle;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;

public abstract class Config extends net.fabricmc.fabric.api.config.v1.Config {
    protected ScreenStyle getStyle() {
        return ScreenStyle.DEFAULT;
    }

    @Override
    public void onConfigsLoaded() {
        super.onConfigsLoaded();

        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT && !this.getValues().isEmpty()) {
                ConfigScreenProvider.register(this.getValues());
        }
    }
}
