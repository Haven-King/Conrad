package dev.inkwell.conrad.api.gui;

import dev.inkwell.conrad.impl.ConfigScreenProviderImpl;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;

import java.util.function.BiConsumer;
import java.util.function.Function;

@Environment(EnvType.CLIENT)
public final class ConfigScreenProvider {
    private ConfigScreenProvider() {

    }

    public static Screen getScreen(String modId) {
        return getScreen(modId, MinecraftClient.getInstance().currentScreen);
    }

    public static Screen getScreen(String modId, Screen parent) {
        return ConfigScreenProviderImpl.get(modId, parent);
    }

    public static void open(String modId) {
        Screen screen = ConfigScreenProviderImpl.get(modId, MinecraftClient.getInstance().currentScreen);

        if (screen != null) {
            MinecraftClient.getInstance().openScreen(screen);
        }
    }

    public static void forEach(BiConsumer<String, Function<Screen, ? extends Screen>> consumer) {
        ConfigScreenProviderImpl.forEach(consumer);
    }
}
