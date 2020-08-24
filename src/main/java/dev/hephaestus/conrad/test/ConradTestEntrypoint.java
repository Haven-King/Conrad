package dev.hephaestus.conrad.test;

import dev.hephaestus.conrad.api.Conrad;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.LiteralText;

public class ConradTestEntrypoint implements ClientModInitializer, ModInitializer {
	@Override
	@Environment(EnvType.CLIENT)
	public void onInitializeClient() {
		HudRenderCallback.EVENT.register(((matrixStack, v) -> {
			UserTestConfig config = Conrad.getConfig(UserTestConfig.class);
			MinecraftClient.getInstance().textRenderer.draw(matrixStack, config.getMyName() + " - " + config.getMyFavoriteNumber(), 3, 3, 0xFFFF8888);
			MinecraftClient.getInstance().textRenderer.draw(matrixStack, String.valueOf(config.getInnerTestConfig().getMyBAC()), 3, 13, 0xFF8888FF);
		}));

		ClientTickEvents.START_WORLD_TICK.register(clientWorld -> {
			UserTestConfig config = Conrad.getConfig(UserTestConfig.class);
//			config.setMyFavoriteNumber(config.getMyFavoriteNumber() + 1);
//			config.getInnerTestConfig().setMyBAC((float) Math.random());
		});
	}

	@Override
	public void onInitialize() {
		ServerTickEvents.START_SERVER_TICK.register((minecraftServer -> {
			minecraftServer.getPlayerManager().getPlayerList().forEach(playerEntity -> {
				UserTestConfig config = Conrad.getConfig(UserTestConfig.class, playerEntity);

				if (config != null) {
					playerEntity.sendMessage(new LiteralText("Power Level: " + config.getInnerTestConfig().getInner().getLeet()), true);
//					config.setNumber(config.getNumber() - 1);
				}
			});
		}));
	}
}
