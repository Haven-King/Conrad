package dev.hephaestus.conrad.impl.common.config;

import dev.hephaestus.conrad.api.Config;
import dev.hephaestus.conrad.api.Conrad;
import dev.hephaestus.conrad.api.SaveCallback;
import dev.hephaestus.conrad.api.serialization.ConfigSerializer;
import dev.hephaestus.conrad.impl.client.util.ClientUtil;
import dev.hephaestus.conrad.impl.common.ConradPreLaunchEntrypoint;
import dev.hephaestus.conrad.impl.common.networking.packets.ConradPacket;
import dev.hephaestus.conrad.impl.common.networking.packets.all.ConfigValuePacket;
import dev.hephaestus.conrad.impl.common.util.ConradException;
import dev.hephaestus.conrad.impl.common.util.ConradUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.io.FileOutputStream;
import java.io.IOException;
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
	private boolean done = false;

	public ValueContainer(Path saveDirectory) {
		this.saveDirectory = saveDirectory;

		if (ValueContainer.ROOT != null) {
			for (Map.Entry<ValueKey, Object> entry : ValueContainer.ROOT) {
				this.put(entry.getKey(), entry.getValue(), false);
			}

			this.done = true;
		}
	}

	public boolean containsDefault(ValueKey key) {
		return DEFAULT_VALUES.containsKey(key);
	}

	public void put(ValueKey key, Object value, boolean sync) {
		DEFAULT_VALUES.putIfAbsent(key, value);

		Object old = this.get(key);
		boolean modified = value != old;

		if (modified) {
			this.values.put(key, value);

			if (this.done) {
				for (Identifier callback : KeyRing.get(key.getConfigKey()).getValueDefinition(key).getCallbacks()) {
					Conrad.getCallback(callback).forEach(saveCallback -> saveCallback.onSave(key, old, value));
				}

				try {
					this.save(key, value, sync &&
						KeyRing.get(key.getConfigKey()).getValueDefinition(key).isSynced()
					);
				} catch (IOException e) {
					ConradUtil.LOG.warn("Error saving value {} for key {}: {}", value, key, e.getMessage());
				}
			}
		}
	}

	protected void save(ValueKey key, Object value, boolean sync) throws IOException {
		ConfigDefinition configDefinition = KeyRing.getRootDefinition(key.getConfigKey());

		if (sync) {
			this.sync(key, value);
		}

		ConfigSerializer<?, ?> serializer = configDefinition.getSerializer();

		Files.createDirectories(this.saveDirectory.resolve(configDefinition.getSavePath()));

		serializer.writeValue(
				serializer.serialize(this, configDefinition),
				new FileOutputStream(
						this.saveDirectory.resolve(configDefinition.getSavePath().resolve(configDefinition.getKey().getName() + "." + serializer.fileExtension())).toFile()
				)
		);
	}

	protected void sync(ValueKey key, Object value) {
		if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
			ClientUtil.sendValue(key, value);
		} else {
			MinecraftServer server = (MinecraftServer) FabricLoader.getInstance().getGameInstance();
			ConradPacket packet = new ConfigValuePacket(ConfigValuePacket.INFO, key, value);
			server.getPlayerManager().getPlayerList().forEach(player -> {
				if (player.hasPermissionLevel(4)) {
					packet.send(player);
				}
			});
		}
	}

	@SuppressWarnings("unchecked")
	public final <T> T get(ValueKey key) {
		return (T) this.values.get(key);
	}

	@Override
	public Iterator<Map.Entry<ValueKey, Object>> iterator() {
		return this.values.entrySet().iterator();
	}

	public void done() {
		this.done = true;
	}

	public static class Remote extends ValueContainer {
		public Remote() {
			super(null);
		}

		@Override
		protected void save(ValueKey key, Object value, boolean sync){
			this.sync(key, value);
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
