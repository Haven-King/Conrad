package dev.hephaestus.conrad.api.networking;

import dev.hephaestus.conrad.impl.common.keys.ConfigKey;
import dev.hephaestus.conrad.impl.common.keys.ValueKey;
import dev.hephaestus.conrad.impl.common.util.ReflectionUtil;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Date;
import java.util.HashMap;

public class NetworkSerializerRegistry {
	private static final HashMap<Class<?>, NetworkedObjectReader<?>> READERS = new HashMap<>();
	private static final HashMap<Class<?>, NetworkedObjectWriter<?>> WRITERS = new HashMap<>();

	public static <T> void put(Class<T> clazz, NetworkedObjectReader<T> reader, NetworkedObjectWriter<T> writer) {
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
		NetworkSerializerRegistry.put(ConfigKey.class, ConfigKey.READER, ConfigKey.WRITER);
		NetworkSerializerRegistry.put(ValueKey.class, ValueKey.READER, ValueKey.WRITER);
		NetworkSerializerRegistry.put(Integer.class, (PacketByteBuf::readVarInt), ((buf, value) -> buf.writeVarInt((Integer) value)));
		NetworkSerializerRegistry.put(Long.class, (PacketByteBuf::readVarLong), ((buf, value) -> buf.writeVarLong((Long) value)));
		NetworkSerializerRegistry.put(Float.class, (PacketByteBuf::readFloat), ((buf, value) -> buf.writeFloat((Float) value)));
		NetworkSerializerRegistry.put(Double.class, (PacketByteBuf::readDouble), ((buf, value) -> buf.writeDouble((Double) value)));
		NetworkSerializerRegistry.put(String.class, (buf -> buf.readString(32767)), ((buf, value) -> buf.writeString((String) value)));
		NetworkSerializerRegistry.put(Text.class, (PacketByteBuf::readText), ((buf, value) -> buf.writeText((Text) value)));
		NetworkSerializerRegistry.put(Identifier.class, (PacketByteBuf::readIdentifier), ((buf, value) -> buf.writeIdentifier((Identifier) value)));
		NetworkSerializerRegistry.put(Date.class, (PacketByteBuf::readDate), ((buf, value) -> buf.writeDate((Date) value)));
	}

	public static NetworkedObjectReader<?> getReader(Class<?> clazz) {
		return READERS.get(clazz);
	}

	public static NetworkedObjectWriter<?> getWriter(Class<?> clazz) {
		return WRITERS.get(clazz);
	}
}
