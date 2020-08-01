package dev.hephaestus.conrad.impl.network.packets.c2s;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.hephaestus.conrad.api.Config;
import dev.hephaestus.conrad.impl.ConradUtils;
import dev.hephaestus.conrad.impl.duck.ConfigManagerProvider;
import dev.hephaestus.conrad.impl.network.packets.s2c.ConfigInfoS2CPacket;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.network.PacketContext;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class PingC2SPacket {
	public static final Identifier ID = ConradUtils.id("packet", "c2s", "ping");

	public static void send() {
		ClientSidePacketRegistry.INSTANCE.sendToServer(ID, new PacketByteBuf(Unpooled.buffer()));
	}

	public static void accept(PacketContext context, PacketByteBuf buf) {
		ServerPlayerEntity playerEntity = (ServerPlayerEntity) context.getPlayer();

		context.getTaskQueue().execute(() -> {
			if (playerEntity.hasPermissionLevel(4)) {
				for (Config config : ConfigManagerProvider.of(playerEntity.server).getConfigManager()) {
					try {
						PacketByteBuf outBuf = new PacketByteBuf(Unpooled.buffer());
						buf.writeString(config.getClass().getName());
						buf.writeString(ConradUtils.MAPPER.writeValueAsString(config));
						ServerSidePacketRegistry.INSTANCE.sendToPlayer(playerEntity, ConfigInfoS2CPacket.ID, outBuf);
					} catch (JsonProcessingException e) {
						e.printStackTrace();
					}
				}
			}
		});
	}
}
