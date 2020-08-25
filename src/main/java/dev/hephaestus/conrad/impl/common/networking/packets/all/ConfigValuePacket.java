package dev.hephaestus.conrad.impl.common.networking.packets.all;

import dev.hephaestus.conrad.api.Config;
import dev.hephaestus.conrad.api.networking.NetworkSerializerRegistry;
import dev.hephaestus.conrad.impl.common.config.ValueContainerProvider;
import dev.hephaestus.conrad.impl.common.keys.ValueKey;
import dev.hephaestus.conrad.impl.common.networking.packets.ConradPacket;
import dev.hephaestus.conrad.impl.common.util.ConradUtil;
import net.fabricmc.fabric.api.network.PacketContext;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.io.*;

public class ConfigValuePacket extends ConradPacket {
	public static final Identifier INFO = ConradUtil.id("packet", "all", "value", "info");
	public static final Identifier SAVE = ConradUtil.id("packet", "c2s", "value", "save");

	public ConfigValuePacket(Identifier id, ValueKey valueKey, Object value) {
		super(id, Type.ALL);

		ValueKey.WRITER.write(this, valueKey);

		this.writeString(value.getClass().getName());
		NetworkSerializerRegistry.write(this, value);
	}

	private static Object read(PacketByteBuf buf) {
		Class<?> clazz;

		try {
			String className = buf.readString(32767);
			clazz = Class.forName(className);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}

		return NetworkSerializerRegistry.read(buf, clazz);
	}

	public static void saveUserInfo(PacketContext context, PacketByteBuf buf) {
		ValueKey valueKey = ValueKey.READER.read(buf);
		Object value = read(buf);

		context.getTaskQueue().execute(() -> {
			try {
				ValueContainerProvider.getInstance(
						Config.SaveType.Type.USER
				).getPlayerValueContainers().put(
						(ServerPlayerEntity) context.getPlayer(),
						valueKey,
						value
				);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}

	public static void saveConfigValue(PacketContext context, PacketByteBuf buf) {
		ValueKey valueKey = ValueKey.READER.read(buf);
		Object value = read(buf);

		context.getTaskQueue().execute(() -> {
			try {
				ValueContainerProvider.getInstance(
						Config.SaveType.Type.LEVEL
				).getValueContainer().put(
						valueKey,
						value
				);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}
}