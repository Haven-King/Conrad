/*
 * Copyright 2021 Haven King
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.inkwell.optionionated.impl.networking;

import dev.inkwell.optionionated.api.ConfigDefinition;
import dev.inkwell.optionionated.api.ConfigManager;
import dev.inkwell.optionionated.api.data.DataType;
import dev.inkwell.optionionated.api.data.SaveType;
import dev.inkwell.optionionated.api.data.SyncType;
import dev.inkwell.optionionated.api.serialization.ConfigSerializer;
import dev.inkwell.optionionated.api.util.Version;
import dev.inkwell.optionionated.api.value.ValueContainer;
import dev.inkwell.optionionated.api.value.ValueKey;
import dev.inkwell.optionionated.impl.ConfigManagerImpl;
import dev.inkwell.optionionated.impl.networking.channels.*;
import dev.inkwell.optionionated.impl.networking.util.Disconnector;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.VersionParsingException;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.function.Function;
import java.util.function.Predicate;

public class ConfigNetworking implements ModInitializer, ClientModInitializer {
    public static <R> Result read(PacketByteBuf buf, Function<SaveType, ValueContainer> provider, Disconnector disconnector) {
        String configDefinitionString = buf.readString(32767);
        ConfigDefinition<R> configDefinition = ConfigManager.getDefinition(configDefinitionString);
        String userVersionString = buf.readString(32767);
        boolean forward = buf.readBoolean();

        if (configDefinition != null) {
            try {
                Version userVersion = Version.parse(userVersionString);
                Version localVersion = configDefinition.getVersion();

                if (userVersion.getVersionComponent(0) != localVersion.getVersionComponent(0)) {
                    // We require that the sides have matching config versions at least in the major component
                    disconnector.config_disconnect(new TranslatableText("fabric.config.invalid_version",
                            configDefinition.toString(),
                            userVersionString,
                            localVersion.toString()));

                    // We'll also abort here to avoid sending useless info to other connected clients
                    return new Result(forward, configDefinitionString, null);
                }
            } catch (VersionParsingException e) {
                disconnector.config_disconnect(new TranslatableText("fabric.config.version_parse", configDefinition.toString(), userVersionString));
                // We'll also abort here to avoid sending useless info to other connected clients
                // Ideally we'd be able to do this during the login phase so that clients don't finish connecting at all
                return new Result(forward, configDefinitionString, null);
            }

            SaveType saveType = configDefinition.getSaveType();

            if (saveType == SaveType.USER || saveType == SaveType.LEVEL) {
                InputStream inputStream = new ByteArrayInputStream(buf.readByteArray());
                ValueContainer valueContainer = provider.apply(saveType);

                try {
                    configDefinition.getSerializer().deserialize(configDefinition, inputStream, valueContainer);

                    if (saveType == SaveType.LEVEL) {
                        valueContainer.save(configDefinition);
                    }

                    return new Result(forward, configDefinitionString, valueContainer);
                } catch (IOException e) {
                    ConfigManagerImpl.LOGGER.error("Failed to sync config '{}': {}", configDefinition, e.getMessage());
                }
            }
        }

        return new Result(forward, configDefinitionString, null);
    }

    public static <R> @Nullable PacketByteBuf toPacket(ConfigDefinition<R> configDefinition, ValueContainer valueContainer) {
        SaveType saveType = configDefinition.getSaveType();

        Predicate<ValueKey<?>> predicate = key -> true;
        boolean forward = false;

        // We only want to construct and send the packet if it's actually gonna contain values
        if (saveType == SaveType.USER) {
            Collection<SyncType> syncTypes = new HashSet<>();

            // Checks each config key
            for (ValueKey<?> value : configDefinition) {
                for (SyncType syncType : value.getData(DataType.SYNC_TYPE)) {
                    syncTypes.add(syncType);
                }
            }

            // Checks the config definition itself
            for (SyncType syncType : configDefinition.getData(DataType.SYNC_TYPE)) {
                syncTypes.add(syncType);
            }

            if (syncTypes.isEmpty()) return null;

            forward = true;
            predicate = key -> !key.getData(DataType.SYNC_TYPE).isEmpty();
        }

        ConfigSerializer<R> serializer = configDefinition.getSerializer();
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());

        buf.writeString(configDefinition.toString());
        buf.writeString(configDefinition.getVersion().toString());
        buf.writeBoolean(forward);

        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            serializer.serialize(configDefinition, stream, valueContainer, predicate, true);
            byte[] bytes = stream.toByteArray();

            // If we didn't actually serialize anything, don't bother sending
            if (bytes.length > 0) {
                buf.writeByteArray(bytes);
                return buf;
            }
        } catch (IOException e) {
            ConfigManagerImpl.LOGGER.error("Failed to sync config '{}': {}", configDefinition, e.getMessage());
        }

        return null;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void onInitializeClient() {
        for (EntrypointContainer<Channel> channel : FabricLoader.getInstance().getEntrypointContainers("channel", Channel.class)) {
            channel.getEntrypoint().onInitializeClient();
        }
    }

    @Override
    public void onInitialize() {
        for (EntrypointContainer<Channel> channel : FabricLoader.getInstance().getEntrypointContainers("channel", Channel.class)) {
            channel.getEntrypoint().onInitialize();
        }
    }

    public static boolean isSynced(ConfigDefinition<?> configDefinition) {
        if (!configDefinition.getData(DataType.SYNC_TYPE).isEmpty()) {
            return true;
        }

        for (ValueKey<?> valueKey : configDefinition) {
            if (!valueKey.getData(DataType.SYNC_TYPE).isEmpty()) {
                return true;
            }
        }

        return false;
    }

    public static final class Result {
        public final boolean forward;
        public final String configDefinitionString;
        public final @Nullable ValueContainer valueContainer;

        public Result(boolean forward, String configDefinitionString, @Nullable ValueContainer valueContainer) {
            this.forward = forward;
            this.configDefinitionString = configDefinitionString;
            this.valueContainer = valueContainer;
        }
    }
}
