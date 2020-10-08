package dev.hephaestus.conrad.api.networking;

import dev.hephaestus.conrad.impl.common.config.ConfigKey;
import dev.hephaestus.conrad.impl.common.config.ValueKey;
import dev.hephaestus.conrad.impl.common.util.ReflectionUtil;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.HashMap;

public class NetworkSerializerRegistry {
	private static final HashMap<Class<?>, NetworkedObjectReader<?>> READERS = new HashMap<>();
	private static final HashMap<Class<?>, NetworkedObjectWriter<?>> WRITERS = new HashMap<>();

	public static <T> void register(Class<T> clazz, NetworkedObjectReader<T> reader, NetworkedObjectWriter<T> writer) {
		for (Class<?> otherClazz : ReflectionUtil.getClasses(clazz)) {
			READERS.put(otherClazz, reader);
			WRITERS.put(otherClazz, writer);
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> T read(PacketByteBuf buf, Class<T> clazz) {
		return (T) READERS.get(clazz).read(buf);
	}

	public static void write(PacketByteBuf buf, Object object) {
		WRITERS.get(object.getClass()).write(buf, object);
	}

	public static void init() {
		NetworkSerializerRegistry.register(ConfigKey.class, ConfigKey.READER, ConfigKey.WRITER);
		NetworkSerializerRegistry.register(ValueKey.class, ValueKey.READER, ValueKey.WRITER);
		NetworkSerializerRegistry.register(Boolean.class, (PacketByteBuf::readBoolean), ((buf, value) -> buf.writeBoolean((Boolean) value)));
		NetworkSerializerRegistry.register(Integer.class, (PacketByteBuf::readVarInt), ((buf, value) -> buf.writeVarInt((Integer) value)));
		NetworkSerializerRegistry.register(Long.class, (PacketByteBuf::readVarLong), ((buf, value) -> buf.writeVarLong((Long) value)));
		NetworkSerializerRegistry.register(Float.class, (PacketByteBuf::readFloat), ((buf, value) -> buf.writeFloat((Float) value)));
		NetworkSerializerRegistry.register(Double.class, (PacketByteBuf::readDouble), ((buf, value) -> buf.writeDouble((Double) value)));
		NetworkSerializerRegistry.register(String.class, (buf -> buf.readString(32767)), ((buf, value) -> buf.writeString((String) value)));
		NetworkSerializerRegistry.register(Identifier.class, (PacketByteBuf::readIdentifier), ((buf, value) -> buf.writeIdentifier((Identifier) value)));
	}

	public static boolean contains(Class<?> aClass) {
		return READERS.containsKey(aClass) && WRITERS.containsKey(aClass);
	}
}
