package dev.hephaestus.conrad.impl.network.packets.c2s;

import dev.hephaestus.conrad.api.Config;
import dev.hephaestus.conrad.impl.ConradUtils;
import dev.hephaestus.conrad.impl.duck.ConfigManagerProvider;
import dev.hephaestus.conrad.impl.network.packets.ConradPacket;
import net.fabricmc.fabric.api.network.PacketContext;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class ConfigSaveC2SPacket extends ConradPacket {
	public static final Identifier ID = ConradUtils.id("packet", "c2s", "save");

	public ConfigSaveC2SPacket(Config config) {
		super(ID, Type.C2S);
		ConradUtils.write(this, config);
	}

	public static void accept(PacketContext context, PacketByteBuf buf) {
		if (context.getPlayer().hasPermissionLevel(4)) {
			Config config = ConradUtils.read(buf);

			context.getTaskQueue().execute(() -> {
				ConfigManagerProvider provider = ConfigManagerProvider.of(context.getPlayer().getServer());

				if (provider != null) {
					provider.getConfigManager().putConfig(config);
				}
			});
		}
	}
}
