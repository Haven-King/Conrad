package dev.hephaestus.conrad.impl.network.packets.all;

import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.network.PacketContext;

import dev.hephaestus.conrad.api.Config;
import dev.hephaestus.conrad.impl.ConradUtils;
import dev.hephaestus.conrad.impl.duck.ConfigManagerProvider;
import dev.hephaestus.conrad.impl.network.packets.ConradPacket;

public class ConfigDataPacket extends ConradPacket {
	public static final Identifier ID = ConradUtils.id("packet", "all", "data");

	public ConfigDataPacket(Config config) {
		super(ID, Type.ALL);
		ConradUtils.write(this, config);
	}

	@Environment(EnvType.CLIENT)
	public static void acceptOnClient(PacketContext context, PacketByteBuf buf) {
		Config config = ConradUtils.read(buf);

		context.getTaskQueue().execute(() -> {
			ConfigManagerProvider provider = ConfigManagerProvider.of(MinecraftClient.getInstance().getCurrentServerEntry());

			if (provider != null) {
				provider.getConfigManager().putConfig(config);
			}
		});
	}

	public static void acceptOnServer(PacketContext context, PacketByteBuf buf) {
		Config config = ConradUtils.read(buf);

		context.getTaskQueue().execute(() -> {
			ConfigManagerProvider provider = ConfigManagerProvider.of(context.getPlayer().getServer());

			if (provider != null) {
				provider.getPlayerConfigManager().putConfig(config, (ServerPlayerEntity) context.getPlayer());
			}
		});
	}
}
