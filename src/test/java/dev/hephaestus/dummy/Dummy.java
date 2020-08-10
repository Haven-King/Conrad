package dev.hephaestus.dummy;

import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import dev.hephaestus.conrad.api.Conrad;

public class Dummy implements ModInitializer {
	@Override
	public void onInitialize() {
		ServerTickEvents.START_SERVER_TICK.register(minecraftServer -> {
			minecraftServer.getPlayerManager().getPlayerList().forEach(playerEntity -> {
				Conrad.getConfig(DummyClientConfig.class, playerEntity).ifPresent(playerConfig -> {
					DummyServerConfig config = Conrad.getConfig(DummyServerConfig.class);
					Text msg = new LiteralText(config.message + playerConfig.powerLevel).styled(
							style -> style.withColor(TextColor.fromRgb(config.color))
					);

					playerEntity.sendMessage(msg, true);
				});
			});
		});
	}
}
