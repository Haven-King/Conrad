package dev.hephaestus.conrad.impl.network.packets.c2s;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.network.PacketContext;

import dev.hephaestus.conrad.api.Config;
import dev.hephaestus.conrad.impl.ConradUtils;
import dev.hephaestus.conrad.impl.duck.ConfigManagerProvider;
import dev.hephaestus.conrad.impl.network.packets.ConradPacket;
import dev.hephaestus.conrad.impl.network.packets.all.ConfigDataPacket;

public class PingC2SPacket extends ConradPacket {
	public static final Identifier ID = ConradUtils.id("packet", "c2s", "ping");

	public PingC2SPacket() {
		super(ID, Type.C2S);
	}

	@SuppressWarnings("unused")
	public static void accept(PacketContext context, PacketByteBuf buf) {
		ServerPlayerEntity playerEntity = (ServerPlayerEntity) context.getPlayer();

		context.getTaskQueue().execute(() -> {
			if (playerEntity.hasPermissionLevel(4)) {
				for (Config config : ConfigManagerProvider.of(playerEntity.server).getConfigManager()) {
					new ConfigDataPacket(config).send(playerEntity);
				}
			}
		});
	}
}
