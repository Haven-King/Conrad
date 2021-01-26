package dev.monarkhes.conrad.impl.value;

import dev.monarkhes.conrad.api.Config;
import dev.monarkhes.conrad.api.SaveType;
import dev.monarkhes.conrad.api.serialization.ConfigSerializer;
import dev.monarkhes.conrad.impl.ConfigKey;
import dev.monarkhes.conrad.impl.KeyRing;
import dev.monarkhes.conrad.impl.entrypoints.RegisterConfigs;
import dev.monarkhes.conrad.impl.util.ConradException;
import dev.monarkhes.conrad.impl.util.ValueContainerProvider;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class ValueContainer implements Iterable<Map.Entry<ConfigKey, Object>> {
	public static ValueContainer ROOT = new ValueContainer(FabricLoader.getInstance().getConfigDir().normalize(), null);

	private final Map<ConfigKey, Object> values = new ConcurrentHashMap<>();
	private final Path saveDirectory;

	private boolean modified = false;

	private ValueContainer(Path saveDirectory, Void dummy) {
		this.saveDirectory = saveDirectory;
	}

	public ValueContainer(Path saveDirectory) {
		this.saveDirectory = saveDirectory;
		ROOT.values.forEach(this.values::put);
	}

	public boolean modified() {
		return this.modified;
	}

	public void put(ConfigKey key, Object value) throws IOException {
		Object old = this.get(key);
		boolean modified = value != old;

		if (modified) {
			this.modified = true;
			this.putWithDefault(key, value);
			this.save(key, value);
		}
	}

	public void putWithDefault(ConfigKey key, Object value) {
		ROOT.values.putIfAbsent(key, value);
		this.values.put(key, value);
	}

	@SuppressWarnings("unchecked")
	public <T> T get(ConfigKey key) {
		return (T) this.values.get(key);
	}

	public boolean contains(ConfigKey key) {
		return this.values.containsKey(key);
	}

	@SuppressWarnings("unchecked")
	protected  <T, O extends T> void save(ConfigKey key, Object value) throws IOException {
		ConfigKey rootKey = new ConfigKey(key.getNamespace(), key.getPath()[0]);
		Config root = KeyRing.getRootConfig(rootKey);
		ConfigSerializer<T, O> serializer = (ConfigSerializer<T, O>) root.serializer();
		Path path = this.saveDirectory.resolve(rootKey.getNamespace()).resolve(root.name()).getParent();

		Files.createDirectories(path);

		serializer.writeValue(
			serializer.serialize(rootKey),
			Files.newOutputStream(
				path.resolve(rootKey.getPath()[rootKey.getPath().length - 1] + "." + serializer.fileExtension())
			)
		);
	}

	public static @NotNull ValueContainer getInstance(@NotNull SaveType saveType) {
		if (!RegisterConfigs.isFinished()) return ROOT;

		EnvType envType = FabricLoader.getInstance().getEnvironmentType();

		if (saveType == SaveType.USER && envType == EnvType.CLIENT || saveType == SaveType.LEVEL && envType == EnvType.SERVER) {
			return ROOT;
		} else if (saveType == SaveType.LEVEL && envType == EnvType.CLIENT) {
			MinecraftClient client = MinecraftClient.getInstance();

			if (client == null) {
				return ROOT;
			} else if (client.isIntegratedServerRunning() && client.getServer() != null) {
				return ((ValueContainerProvider) client.getServer()).getValueContainer();
			} else if (client.getCurrentServerEntry() != null) {
				return ((ValueContainerProvider) client.getCurrentServerEntry()).getValueContainer();
			}
		} else if (saveType == SaveType.USER && envType == EnvType.SERVER) {
			throw new ConradException("Can't get user config on server without a player.");
		}

		return ROOT;
	}

	public static @NotNull ValueContainer getInstance(@NotNull PlayerEntity user) {
		EnvType envType = FabricLoader.getInstance().getEnvironmentType();

		if (envType == EnvType.CLIENT) {
			MinecraftClient client = MinecraftClient.getInstance();

			if (client == null) {
				throw new ConradException("Cannot get user config outside of world.");
			}else if (client.isIntegratedServerRunning() && client.getServer() != null) {
				return ((ValueContainerProvider) client.getServer()).getPlayerValueContainer(user);
			} else if (client.getCurrentServerEntry() != null) {
				return ((ValueContainerProvider) client.getCurrentServerEntry()).getPlayerValueContainer(user);
			}
		} else if (user.getServer() != null) {
			return ((ValueContainerProvider) user.getServer()).getPlayerValueContainer(user);
		}

		throw new ConradException("Error getting ValueContainer instance.");
	}

	public static @NotNull ValueContainer getInstance(@NotNull UUID userId) {
		EnvType envType = FabricLoader.getInstance().getEnvironmentType();

		if (envType == EnvType.CLIENT) {
			MinecraftClient client = MinecraftClient.getInstance();

			if (client == null) {
				throw new ConradException("Cannot get user config outside of world.");
			}else if (client.isIntegratedServerRunning() && client.getServer() != null) {
				return ((ValueContainerProvider) client.getServer()).getPlayerValueContainer(userId);
			} else if (client.getCurrentServerEntry() != null) {
				return ((ValueContainerProvider) client.getCurrentServerEntry()).getPlayerValueContainer(userId);
			}
		} else if (FabricLoader.getInstance().getGameInstance() != null) {
			return ((ValueContainerProvider) FabricLoader.getInstance().getGameInstance()).getPlayerValueContainer(userId);
		}

		throw new ConradException("Error getting ValueContainer instance.");

	}

	@NotNull
	@Override
	public Iterator<Map.Entry<ConfigKey, Object>> iterator() {
		return this.values.entrySet().iterator();
	}

	@Override
	public void forEach(Consumer<? super Map.Entry<ConfigKey, Object>> action) {
		this.values.entrySet().forEach(action);
	}

	public int length() {
		return this.values.size();
	}
}
