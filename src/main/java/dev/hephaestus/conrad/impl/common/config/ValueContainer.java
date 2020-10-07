package dev.hephaestus.conrad.impl.common.config;

import dev.hephaestus.conrad.api.Config;
import dev.hephaestus.conrad.api.Conrad;
import dev.hephaestus.conrad.api.serialization.ConfigSerializer;
import dev.hephaestus.conrad.impl.client.util.ClientUtil;
import dev.hephaestus.conrad.impl.common.ConradPreLaunchEntrypoint;
import dev.hephaestus.conrad.impl.common.keys.KeyRing;
import dev.hephaestus.conrad.impl.common.keys.ValueKey;
import dev.hephaestus.conrad.impl.common.networking.packets.all.ConfigValuePacket;
import dev.hephaestus.conrad.impl.common.util.ConradException;
import dev.hephaestus.conrad.impl.common.util.ConradUtil;
import dev.hephaestus.conrad.impl.common.util.ReflectionUtil;
import dev.hephaestus.conrad.impl.common.util.SerializationUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.network.ServerPlayerEntity;

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ValueContainer implements Iterable<Map.Entry<ValueKey, Object>> {
	public static final ValueContainer ROOT = new ValueContainer(FabricLoader.getInstance().getConfigDir().normalize());

	private static final HashMap<ValueKey, Object> DEFAULT_VALUES = new HashMap<>();

	protected final HashMap<ValueKey, Object> values = new HashMap<>();

	private final Path saveDirectory;

	public ValueContainer(Path saveDirectory) {
		this.saveDirectory = saveDirectory;

		if (ValueContainer.ROOT != null) {
			for (Map.Entry<ValueKey, Object> entry : ValueContainer.ROOT) {
				try {
					this.put(entry.getKey(), entry.getValue(), true);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public boolean containsDefault(ValueKey key) {
		return DEFAULT_VALUES.containsKey(key);
	}

	public void put(ValueKey key, Object value, boolean sync) throws IOException {
		DEFAULT_VALUES.putIfAbsent(key, value);

		Object old = this.get(key);
		boolean modified = value != old;

		if (modified) {
			this.values.put(key, value);

			if (ConradPreLaunchEntrypoint.isDone()) {
				Conrad.fireCallbacks(key, old, value);
				this.save(key, value, sync && key.isSynced());
			}
		}
	}

	protected void save(ValueKey key, Object value, boolean sync) throws IOException {
		Class<? extends Config> configClass = KeyRing.get(key.getConfig().root());
		Config config = Conrad.getConfig(configClass);

		if (sync) {
			if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
				ClientUtil.sendValue(key, value);
			} else {
				// TODO
			}
		}

		ConfigSerializer<?, ?> serializer = config.serializer();
		Path path = SerializationUtil.saveFolder(this.saveDirectory, configClass);

		Files.createDirectories(path);

		serializer.writeValue(
				serializer.serialize(config),
				new FileOutputStream(
						path.resolve(SerializationUtil.saveName(configClass) + "." + serializer.fileExtension()).toFile()
				)
		);
	}

	@SuppressWarnings("unchecked")
	public final <T> T get(ValueKey key) {
		return (T) this.values.get(key);
	}

	@Override
	public Iterator<Map.Entry<ValueKey, Object>> iterator() {
		return this.values.entrySet().iterator();
	}

	public static class Remote extends ValueContainer {
		public Remote() {
			super(null);
		}

		@Override
		public void put(ValueKey key, Object value, boolean synced) throws IOException {
			super.put(key, value, false);
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> T getDefault(ValueKey key) {
		return (T) DEFAULT_VALUES.get(key);
	}

	public static void init() {}


	/**
	 * Gets level-attached config value container for the current level.
	 * @return a ValueContainer holding the config values
	 */
	public static ValueContainer getInstance() {
		EnvType envType = FabricLoader.getInstance().getEnvironmentType();

		if (envType == EnvType.SERVER) {
			return ROOT;
		} else {
			return ClientUtil.getValueContainer();
		}
	}

	/**
	 * Gets the user-attached config value container for the specified player.
	 * @param playerEntity player to get container for
	 * @return ValueContainer with their config values
	 */
	public static ValueContainer getInstance(ServerPlayerEntity playerEntity) {
		EnvType envType = FabricLoader.getInstance().getEnvironmentType();

		if (envType == EnvType.SERVER && playerEntity.getServer() != null) {
			return ((ValueContainerProvider) playerEntity.getServer()).getPlayerValueContainers().get(playerEntity.getUuid());
		} else if (envType == EnvType.CLIENT) {
			return ClientUtil.getValueContainer(playerEntity);
		}

		throw new ConradException("Server is null");
	}
}
