package dev.hephaestus.conrad.impl.data;

import net.minecraft.client.MinecraftClient;

public class ClientConfigDeterminer {
    @SuppressWarnings("MethodCallSideOnly")
    public static ConfigSerializer getInstance() {
        if (MinecraftClient.getInstance().world != null) {
            if (MinecraftClient.getInstance().isIntegratedServerRunning()) {
                return LevelConfigSerializer.INSTANCE;
            } else {
                return NetworkedConfigSerializer.INSTANCE;
            }
        } else {
            return RootConfigSerializer.INSTANCE;
        }
    }
}
