package dev.monarkhes.conrad.impl.entrypoints;

import dev.monarkhes.conrad.api.ConfigValue;
import dev.monarkhes.conrad.api.SyncType;
import dev.monarkhes.conrad.api.serialization.NetworkSerializerRegistry;
import dev.monarkhes.conrad.impl.ConfigKey;
import dev.monarkhes.conrad.impl.Conrad;
import dev.monarkhes.conrad.impl.KeyRing;
import dev.monarkhes.conrad.impl.util.ValueContainerProvider;
import dev.monarkhes.conrad.impl.value.ValueContainer;
import dev.monarkhes.conrad.test.TestConfig;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Collection;
import java.util.UUID;

public class Networking implements ModInitializer, ClientModInitializer {
    public static final Identifier CONFIG_VALUES = Conrad.id("packet", "sync_values");

    @Override
    @Environment(EnvType.CLIENT)
    public void onInitializeClient() {
        ClientPlayConnectionEvents.INIT.register(Networking::sendConfigValues);
        ClientPlayConnectionEvents.INIT.register((handler, client) -> {
            ClientPlayNetworking.registerReceiver(CONFIG_VALUES, Networking::receiveConfigValues);
        });
    }

    @Override
    public void onInitialize() {
        ServerPlayConnectionEvents.INIT.register(((handler, server) -> {
            ServerPlayNetworking.registerReceiver(handler, CONFIG_VALUES, Networking::receiveConfigValues);
        }));
    }

    @Environment(EnvType.CLIENT)
    private static void sendConfigValues(ClientPlayNetworkHandler handler, MinecraftClient client) {
        sendConfigValues();
    }

    @Environment(EnvType.CLIENT)
    public static void sendConfigValues() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!client.isIntegratedServerRunning() && client.getCurrentServerEntry() == null) return;
        if (!ValueContainer.ROOT.modified()) return;

        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());

        buf.writeVarInt(ValueContainer.ROOT.length());

        ValueContainer.ROOT.forEach(entry -> {
            ConfigKey key = entry.getKey();
            Object value = entry.getValue();

            ConfigValue<?> configValue = KeyRing.get(key);

            if (configValue.getSyncType() != SyncType.NONE) {
                buf.writeString(key.getNamespace());
                buf.writeVarInt(key.getPath().length);

                for (String element : key.getPath()) {
                    buf.writeString(element);
                }

                buf.writeString(value.getClass().getName());
                NetworkSerializerRegistry.getSerializer(value.getClass()).write(value, buf);
            }
        });

        ClientPlayNetworking.send(CONFIG_VALUES, buf);
    }


    private static void receiveConfigValues(MinecraftServer server, ServerPlayerEntity sender, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender packetSender) {
        int valueContainerSize = buf.readVarInt();

        UUID playerUuid = sender.getUuid();

        for (int i = 0; i < valueContainerSize; ++i) {
            String namespace = buf.readString();
            String[] path = new String[buf.readVarInt()];

            for (int j = 0; j < path.length; ++j) {
                path[j] = buf.readString();
            }

            ConfigKey configKey = new ConfigKey(namespace, path);

            String valueClass = buf.readString();

            try {
                Object value = NetworkSerializerRegistry.getSerializer(valueClass).read(buf);
                ((ValueContainerProvider) server).getPlayerValueContainer(playerUuid).put(configKey, value);
            } catch (ClassNotFoundException | IOException e) {
                Conrad.LOGGER.error(e.getMessage());
            }
        }

        MutableText text = sender.getName().copy().append(new LiteralText(TestConfig.A.LIKE.get()
                ? " likes "
                : " doesn't like"
        )).append(TestConfig.A.FRUIT.get());

        sender.sendMessage(text, false);

        sendConfigValues(sender, server);
    }

    public static void sendConfigValues(@Nullable ServerPlayerEntity except, MinecraftServer server) {
        PacketByteBuf peerBuf = new PacketByteBuf(Unpooled.buffer());
        peerBuf.writeVarInt(((ValueContainerProvider) server).playerCount());

        ((ValueContainerProvider) server).forEach(entry -> {
            peerBuf.writeUuid(entry.getKey());
            peerBuf.writeVarInt(entry.getValue().length());

            entry.getValue().forEach(e -> {
                ConfigKey key = e.getKey();
                Object value = e.getValue();

                ConfigValue<?> configValue = KeyRing.get(key);

                if (configValue.getSyncType() == SyncType.PEER) {
                    peerBuf.writeString(key.getNamespace());
                    peerBuf.writeVarInt(key.getPath().length);

                    for (String element : key.getPath()) {
                        peerBuf.writeString(element);
                    }

                    peerBuf.writeString(value.getClass().getName());
                    NetworkSerializerRegistry.getSerializer(value.getClass()).write(value, peerBuf);
                }
            });
        });

        PlayerLookup.all(server).forEach(player -> {
            if (player != except) {
                ServerPlayNetworking.send(player, Networking.CONFIG_VALUES, peerBuf);
            }
        });
    }

    @Environment(EnvType.CLIENT)
    private static void receiveConfigValues(MinecraftClient client, ClientPlayNetworkHandler clientPlayNetworkHandler, PacketByteBuf buf, PacketSender packetSender) {
        int playerCount = buf.readVarInt();

        for (int i = 0; i < playerCount; ++i) {
            UUID playerId = buf.readUuid();
            int keyCount = buf.readVarInt();

            for (int j = 0; j < keyCount; ++j) {
                String namespace = buf.readString();
                String[] path = new String[buf.readVarInt()];

                for (int k = 0; k < path.length; ++k) {
                    path[k] = buf.readString();
                }

                ConfigKey configKey = new ConfigKey(namespace, path);

                String valueClass = buf.readString();

                try {
                    Object value = NetworkSerializerRegistry.getSerializer(valueClass).read(buf);

                    if (playerId != client.getSession().getProfile().getId()) {
                        ValueContainer.getInstance(playerId).put(configKey, value);
                    }
                } catch (ClassNotFoundException | IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
