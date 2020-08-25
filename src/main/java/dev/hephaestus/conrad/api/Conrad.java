package dev.hephaestus.conrad.api;

import dev.hephaestus.conrad.impl.common.ConradInvocationHandler;
import dev.hephaestus.conrad.impl.common.config.PlayerValueContainers;
import dev.hephaestus.conrad.impl.common.config.ValueContainer;
import dev.hephaestus.conrad.impl.common.config.ValueContainerProvider;
import net.minecraft.server.network.ServerPlayerEntity;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.UUID;

@SuppressWarnings("unchecked")
public class Conrad {
	private static final HashMap<Class<? extends Config>, Object> CONFIG_PROXIES = new HashMap<>();
	private static final HashMap<UUID, HashMap<Class<? extends Config>, Object>> PLAYER_PROXIES = new HashMap<>();

	public static <T extends Config> T getConfig(Class<T> configClass) {
		return (T) CONFIG_PROXIES.computeIfAbsent(configClass, key -> Proxy.newProxyInstance(
				configClass.getClassLoader(),
				new Class[] {configClass},
				ConradInvocationHandler.INSTANCE
		));	}

	public static <T extends Config> T getConfig(Class<T> configClass, ServerPlayerEntity playerEntity) {
		return (T) PLAYER_PROXIES.computeIfAbsent(playerEntity.getUuid(), id -> new HashMap<>()).computeIfAbsent(configClass, key -> Proxy.newProxyInstance(
				configClass.getClassLoader(),
				new Class[] {configClass},
				new ConradInvocationHandler(ValueContainerProvider.getInstance(
						configClass.getAnnotation(Config.SaveType.class).value()
				).getPlayerValueContainers().get(playerEntity.getUuid()))
		));
	}
}
