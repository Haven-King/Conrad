package dev.hephaestus.conrad.impl.network.packets.s2c;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.hephaestus.conrad.api.Config;
import dev.hephaestus.conrad.impl.ConradUtils;
import dev.hephaestus.conrad.impl.duck.ConfigManagerProvider;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.network.PacketContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class ConfigInfoS2CPacket {
	public static final Identifier ID = ConradUtils.id("packet", "s2c", "info");

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