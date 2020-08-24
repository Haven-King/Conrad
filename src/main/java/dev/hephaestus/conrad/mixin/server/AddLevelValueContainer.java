package dev.hephaestus.conrad.mixin.server;

import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.datafixers.DataFixer;
import dev.hephaestus.conrad.impl.common.config.PlayerValueContainers;
import dev.hephaestus.conrad.impl.common.config.ValueContainer;
import dev.hephaestus.conrad.impl.common.config.ValueContainerProvider;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.ServerResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListenerFactory;
import net.minecraft.util.UserCache;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.level.storage.LevelStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.Proxy;
import java.nio.file.Path;

@Mixin(MinecraftServer.class)
public abstract class AddLevelValueContainer implements ValueContainerProvider {
	@Shadow public abstract Path getSavePath(WorldSavePath worldSavePath);

	@Unique private ValueContainer valueContainer;
	@Unique private PlayerValueContainers playerValueContainers;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void initializeValueContainers(Thread thread, DynamicRegistryManager.Impl impl, LevelStorage.Session session, SaveProperties saveProperties, ResourcePackManager resourcePackManager, Proxy proxy, DataFixer dataFixer, ServerResourceManager serverResourceManager, MinecraftSessionService minecraftSessionService, GameProfileRepository gameProfileRepository, UserCache userCache, WorldGenerationProgressListenerFactory worldGenerationProgressListenerFactory, CallbackInfo ci) {
		if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
			this.valueContainer = new ValueContainer(this.getSavePath(WorldSavePath.ROOT).normalize().resolve("config"));
		} else {
			this.valueContainer = ValueContainer.ROOT;
		}

		this.playerValueContainers = new PlayerValueContainers();
	}

	@Override
	public ValueContainer getValueContainer() {
		return this.valueContainer;
	}

	@Override
	public PlayerValueContainers getPlayerValueContainers() {
		return this.playerValueContainers;
	}
}
