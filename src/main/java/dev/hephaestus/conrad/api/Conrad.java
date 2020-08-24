package dev.hephaestus.conrad.api;

import dev.hephaestus.conrad.impl.common.config.PlayerValueContainers;
import dev.hephaestus.conrad.impl.common.config.ValueContainer;
import dev.hephaestus.conrad.impl.common.config.ValueContainerProvider;
import net.minecraft.server.network.ServerPlayerEntity;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.UUID;

@SuppressWarnings("unchecked")
public class Conrad {
	public static <T extends Config> T getConfig(Class<T> configClass) {
		return (T) Proxy.newProxyInstance(configClass.getClassLoader(), new Class[] {configClass}, ValueContainer.ROOT);
	}

	public static <T extends Config> T getConfig(Class<T> configClass, ServerPlayerEntity playerEntity) {
		PlayerValueContainers playerValueContainers = ValueContainerProvider.getInstance(configClass.getAnnotation(Config.SaveType.class).value()).getPlayerValueContainers();

		if (playerValueContainers == null || playerValueContainers.get(playerEntity.getUuid()) == null) return null;

		return (T) Proxy.newProxyInstance(configClass.getClassLoader(), new Class[] {configClass}, playerValueContainers.get(playerEntity.getUuid()));
	}
}
