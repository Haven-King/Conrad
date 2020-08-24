package dev.hephaestus.conrad.impl.client.util;

import dev.hephaestus.conrad.impl.common.config.ValueContainerProvider;
import net.minecraft.client.MinecraftClient;

public class ClientUtil {
	@SuppressWarnings("MethodCallSideOnly")
	public static ValueContainerProvider getLevelValueContainerProvider() {
		if (MinecraftClient.getInstance() == null) {
			return ValueContainerProvider.ROOT;
		} else if (MinecraftClient.getInstance().isIntegratedServerRunning() && MinecraftClient.getInstance().getServer() != null) {
			return ((ValueContainerProvider) MinecraftClient.getInstance().getServer());
		} else if (MinecraftClient.getInstance().getServer() != null && MinecraftClient.getInstance().getCurrentServerEntry() != null) {
			return ((ValueContainerProvider) MinecraftClient.getInstance().getCurrentServerEntry());
		} else {
			return ValueContainerProvider.ROOT;
		}
	}
}
