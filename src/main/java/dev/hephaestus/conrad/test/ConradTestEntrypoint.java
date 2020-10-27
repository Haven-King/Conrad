package dev.hephaestus.conrad.test;

import dev.hephaestus.conrad.api.Conrad;
import dev.hephaestus.conrad.api.SaveCallback;
import dev.hephaestus.conrad.impl.common.util.ConradUtil;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;

public class ConradTestEntrypoint implements ClientModInitializer, ModInitializer {
	@Override
	@Environment(EnvType.CLIENT)
	public void onInitializeClient() {
		HudRenderCallback.EVENT.register(((matrixStack, v) -> {
			UserTestConfig config = Conrad.getConfig(UserTestConfig.class);
			MinecraftClient.getInstance().textRenderer.draw(matrixStack, config.as123asdasd123asdasd1() + " - " + config.myFavoriteNumber(), 3, 3, 0xFFFF8888);
			MinecraftClient.getInstance().textRenderer.draw(matrixStack, String.valueOf(config.innerTestConfig().myBAC()), 3, 13, 0xFF8888FF);
		}));
	}

	@Override
	public void onInitialize() {
		Conrad.registerCallback(new Identifier("meth", "head"), (SaveCallback<Integer>) (valueKey, oldValue, newValue) ->
				ConradUtil.LOG.info("Value changed from {} to {}", oldValue, newValue));

		ServerTickEvents.START_SERVER_TICK.register((minecraftServer -> {
			for (ServerPlayerEntity player : minecraftServer.getPlayerManager().getPlayerList()) {
				UserTestConfig config = Conrad.getConfig(UserTestConfig.class, player);

				if (config != null) {
					player.sendMessage(new LiteralText("Power Level: " + config.innerTestConfig().innerTestConfig().leet()).styled(style -> style.withColor(config.color())), true);
				}
			}
		}));
	}
}
