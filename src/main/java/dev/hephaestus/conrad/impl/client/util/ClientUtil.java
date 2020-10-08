package dev.hephaestus.conrad.impl.client.util;

import dev.hephaestus.conrad.impl.common.config.ValueContainer;
import dev.hephaestus.conrad.impl.common.config.ValueContainerProvider;
import dev.hephaestus.conrad.impl.common.config.ValueKey;
import dev.hephaestus.conrad.impl.common.networking.packets.all.ConfigValuePacket;
import dev.hephaestus.conrad.impl.common.util.ConradException;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;

@SuppressWarnings("MethodCallSideOnly")
public class ClientUtil {
	public static ValueContainerProvider getValueContainerProvider() {
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

	public static ValueContainer getValueContainer() {
		if (MinecraftClient.getInstance() == null || MinecraftClient.getInstance().getServer() == null) {
			return ValueContainer.ROOT;
		} else if (MinecraftClient.getInstance().getServer() != null && MinecraftClient.getInstance().getCurrentServerEntry() != null) {
			return ((ValueContainerProvider) MinecraftClient.getInstance().getCurrentServerEntry()).getValueContainer();
		} else if (MinecraftClient.getInstance().getServer() != null) {
			return ((ValueContainerProvider) MinecraftClient.getInstance().getServer()).getValueContainer();
		}

		throw new ConradException("No ValueContainerProvider available");
	}

	public static ValueContainer getValueContainer(ServerPlayerEntity playerEntity) {
		if (MinecraftClient.getInstance().player == null || playerEntity.getUuid() == MinecraftClient.getInstance().player.getUuid()) {
			return ValueContainer.ROOT;
		} else if (playerEntity.getServer() != null) {
			return ((ValueContainerProvider) playerEntity.getServer()).getPlayerValueContainers().get(playerEntity.getUuid());
		}

		throw new ConradException("Server is null");
	}

    public static void sendValue(ValueKey key, Object value) {
		if (MinecraftClient.getInstance() != null && MinecraftClient.getInstance().getNetworkHandler() != null) {
			new ConfigValuePacket(ConfigValuePacket.INFO, key, value).send();
		}
    }

	public static String translate(TranslatableText text) {
		return I18n.translate(text.getKey());
	}
}
