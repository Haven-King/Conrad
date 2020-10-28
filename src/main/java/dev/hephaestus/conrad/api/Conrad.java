package dev.hephaestus.conrad.api;

import com.google.common.collect.HashMultimap;
import dev.hephaestus.conrad.impl.common.ConradInvocationHandler;
import dev.hephaestus.conrad.impl.common.config.ValueContainer;
import dev.hephaestus.conrad.impl.common.config.KeyRing;
import dev.hephaestus.conrad.impl.common.config.ValueKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

@SuppressWarnings("unchecked")
public class Conrad {
	private static final HashMap<Class<? extends Config>, Object> CONFIG_PROXIES = new HashMap<>();
	private static final HashMap<UUID, HashMap<Class<? extends Config>, Object>> PLAYER_PROXIES = new HashMap<>();
	private static final HashMultimap<Identifier, SaveCallback<?>> SAVE_CALLBACKS = HashMultimap.create();

	public static <T extends Config> T getConfig(Class<T> configClass) {
		return (T) CONFIG_PROXIES.computeIfAbsent(configClass, key -> Proxy.newProxyInstance(
				configClass.getClassLoader(),
				new Class[] {configClass},
				ConradInvocationHandler.INSTANCE
		));
	}

	public static <T extends Config> T getConfig(Class<T> configClass, ServerPlayerEntity playerEntity) {
		return (T) PLAYER_PROXIES.computeIfAbsent(playerEntity.getUuid(), id -> new HashMap<>()).computeIfAbsent(configClass, key -> Proxy.newProxyInstance(
				configClass.getClassLoader(),
				new Class[] {configClass},
				new ConradInvocationHandler(ValueContainer.getInstance(playerEntity))
		));
	}

	public static <T> void registerCallback(Identifier id, SaveCallback<T> callback) {
		SAVE_CALLBACKS.put(id, callback);
	}

	@ApiStatus.Internal
	public static Set<SaveCallback<?>> getCallback(Identifier id) {
		return SAVE_CALLBACKS.get(id);
	}
}
