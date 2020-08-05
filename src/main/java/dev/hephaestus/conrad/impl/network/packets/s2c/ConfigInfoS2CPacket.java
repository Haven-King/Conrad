package dev.hephaestus.conrad.impl.network.packets.s2c;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.hephaestus.conrad.api.Config;
import dev.hephaestus.conrad.impl.ConradUtils;
import dev.hephaestus.conrad.impl.duck.ConfigManagerProvider;
import dev.hephaestus.conrad.impl.network.packets.ConradPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.network.PacketContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class ConfigInfoS2CPacket extends ConradPacket {
	public static final Identifier ID = ConradUtils.id("packet", "s2c", "info");

	public ConfigInfoS2CPacket(Config config) {
		super(ID, Type.S2C);
		this.writeString(config.getClass().getName());

		try {
			this.writeString(ConradUtils.MAPPER.writeValueAsString(config));
		} catch (JsonProcessingException e) {
			LOGGER.error(e.getMessage());
		}
	}

	@SuppressWarnings("unchecked")
	@Environment(EnvType.CLIENT)
	public static void accept(PacketContext context, PacketByteBuf buf) {
		try {
			Class<? extends Config> configClass = (Class<? extends Config>) Class.forName(buf.readString(32767));
			Config config = ConradUtils.MAPPER.readValue(buf.readString(32767), configClass);

			context.getTaskQueue().execute(() -> {
				ConfigManagerProvider provider = ConfigManagerProvider.of(MinecraftClient.getInstance().getCurrentServerEntry());

				if (provider != null) {
					provider.getConfigManager().putConfig(config);
				}
			});
		} catch (ClassNotFoundException | JsonProcessingException e) {
			e.printStackTrace();
		}
	}
}