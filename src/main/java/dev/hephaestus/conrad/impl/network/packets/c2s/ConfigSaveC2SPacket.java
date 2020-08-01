package dev.hephaestus.conrad.impl.network.packets.c2s;

import dev.hephaestus.conrad.api.Config;
import dev.hephaestus.conrad.impl.ConradUtils;
import dev.hephaestus.conrad.impl.duck.ConfigManagerProvider;
import net.fabricmc.fabric.api.network.PacketContext;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.io.IOException;

public class ConfigSaveC2SPacket {
	public static final Identifier ID = ConradUtils.id("packet", "c2s", "save");

	public static void accept(PacketContext context, PacketByteBuf buf) {
		try {
			Class<? extends Config> configClass = (Class<? extends Config>) Class.forName(buf.readString(32767));
			Config config = ConradUtils.MAPPER.readValue(buf.readString(32767), configClass);

			context.getTaskQueue().execute(() -> {
				ConfigManagerProvider provider = ConfigManagerProvider.of(context.getPlayer().getServer());

				if (provider != null) {
					provider.getConfigManager().putConfig(config);
				}
			});
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
	}
}
