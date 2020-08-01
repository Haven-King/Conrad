package dev.hephaestus.conrad.impl.network.packets.c2s;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.hephaestus.conrad.api.Config;
import dev.hephaestus.conrad.impl.ConradUtils;
import dev.hephaestus.conrad.impl.duck.ConfigManagerProvider;
import net.fabricmc.fabric.api.network.PacketContext;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class ConfigInfoC2SPacket {
	public static final Identifier ID = ConradUtils.id("packet", "c2s", "info");

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
