package dev.hephaestus.conrad.impl.data;

import net.minecraft.client.MinecraftClient;

/**
 * Determines which serializer is appropriate for saving.
 * Needs to be in its own class because we can't import {@link MinecraftClient} in
 * {@link ConfigSerializer} since that class exists on both the client and server
 * environments.
 */
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
