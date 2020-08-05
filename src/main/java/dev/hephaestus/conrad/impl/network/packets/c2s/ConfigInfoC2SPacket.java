package dev.hephaestus.conrad.impl.network.packets.c2s;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.hephaestus.conrad.api.Config;
import dev.hephaestus.conrad.impl.ConradUtils;
import dev.hephaestus.conrad.impl.duck.ConfigManagerProvider;
import dev.hephaestus.conrad.impl.network.packets.ConradPacket;
import net.fabricmc.fabric.api.network.PacketContext;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class ConfigInfoC2SPacket extends ConradPacket {
	public static final Identifier ID = ConradUtils.id("packet", "c2s", "info");

	public ConfigInfoC2SPacket(Config config) {
		super(ID, Type.C2S);
		this.writeString(config.getClass().getName());

		try {
			this.writeString(ConradUtils.MAPPER.writeValueAsString(config));
		} catch (JsonProcessingException e) {
			LOGGER.error(e.getMessage());
		}
	}

	@SuppressWarnings("unchecked")
	public static void accept(PacketContext context, PacketByteBuf buf) {
		try {
			Class<? extends Config> configClass = (Class<? extends Config>) Class.forName(buf.readString(32767));
			Config config = ConradUtils.MAPPER.readValue(buf.readString(32767), configClass);

			context.getTaskQueue().execute(() -> {
				ConfigManagerProvider provider = ConfigManagerProvider.of(context.getPlayer().getServer());

				if (provider != null) {
					provider.getPlayerConfigManager().putConfig(config, (ServerPlayerEntity) context.getPlayer());
				}
			});
		} catch (ClassNotFoundException | JsonProcessingException e) {
			e.printStackTrace();
		}
	}
}
