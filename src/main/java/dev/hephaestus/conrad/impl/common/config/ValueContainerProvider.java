package dev.hephaestus.conrad.impl.common.config;

import dev.hephaestus.conrad.api.Config;
import dev.hephaestus.conrad.impl.client.util.ClientUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;

public interface ValueContainerProvider {
	ValueContainerProvider ROOT = new ValueContainerProvider() {
		@Override
		public ValueContainer getValueContainer() {
			return ValueContainer.ROOT;
		}

		@Override
		public PlayerValueContainers getPlayerValueContainers() {
			return null;
		}
	};

	ValueContainer getValueContainer();
	PlayerValueContainers getPlayerValueContainers();

	static ValueContainerProvider getInstance(Config.SaveType.Type saveType) {
		EnvType envType = FabricLoader.getInstance().getEnvironmentType();

		if (saveType == Config.SaveType.Type.LEVEL && envType == EnvType.SERVER) {
			return ROOT;
		} else if (envType == EnvType.CLIENT) {
			return ClientUtil.getLevelValueContainerProvider();
		} else if (saveType == Config.SaveType.Type.USER && envType == EnvType.SERVER) {
			return (ValueContainerProvider) FabricLoader.getInstance().getGameInstance();
		}

		throw new IllegalStateException();
	}
}
