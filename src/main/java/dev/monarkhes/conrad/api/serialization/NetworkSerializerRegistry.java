package dev.monarkhes.conrad.api.serialization;

import dev.monarkhes.conrad.api.util.Color;
import dev.monarkhes.vivid.util.Array;
import dev.monarkhes.vivid.util.Table;
import net.minecraft.network.PacketByteBuf;

import java.util.HashMap;
import java.util.Map;

import static dev.monarkhes.conrad.api.serialization.NetworkSerializer.of;

public class NetworkSerializerRegistry {
    private static final Map<Class<?>, NetworkSerializer<?>> SERIALIZERS = new HashMap<>();

    public static <T> void register(Class<T> valueClass, NetworkSerializer<T> serializer) {
        SERIALIZERS.putIfAbsent(valueClass, serializer);
    }

    @SuppressWarnings("unchecked")
    public static <T> NetworkSerializer<T> getSerializer(Class<T> valueClass) {
        return (NetworkSerializer<T>) SERIALIZERS.get(valueClass);
    }

    @SuppressWarnings("unchecked")
    public static <T> NetworkSerializer<T> getSerializer(String valueClass) throws ClassNotFoundException {
        return (NetworkSerializer<T>) SERIALIZERS.get(Class.forName(valueClass));
    }

    static {
        register(Boolean.class, of((bl, buf) -> buf.writeBoolean(bl), PacketByteBuf::readBoolean));
        register(Integer.class, of((i, buf) -> buf.writeVarInt(i), PacketByteBuf::readVarInt));
        register(Long.class, of((l, buf) -> buf.writeVarLong(l), PacketByteBuf::readVarLong));
        register(String.class, of((s, buf) -> buf.writeString(s), PacketByteBuf::readString));
        register(Float.class, of((f, buf) -> buf.writeFloat(f), PacketByteBuf::readFloat));
        register(Double.class, of((d, buf) -> buf.writeDouble(d), PacketByteBuf::readDouble));

        register(Color.class, of(
                (color, buf) -> buf.writeVarInt(color.getColor()),
                buf -> Color.ofTransparent(buf.readVarInt()))
        );

        register(Array.class, of((array, buf) -> {
            buf.writeString(array.getValueClass().getName());
            buf.writeVarInt(array.size());

            NetworkSerializer<?> serializer = getSerializer(array.getValueClass());
            for (Object element : array) {
                serializer.write(element, buf);
            }
        }, buf -> {
            try {
                Class valueClass = Class.forName(buf.readString());
                Array array = new Array(valueClass, () -> null);

                NetworkSerializer serializer = getSerializer(valueClass);

                for (int i = 0; i < buf.readVarInt(); ++i) {
                    array.put(i, serializer.read(buf));
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            return null;
        }));

        register(Table.class, of((table, buf) -> {
            buf.writeString(table.getValueClass().getName());
            buf.writeVarInt(table.size());

            NetworkSerializer<?> serializer = getSerializer(table.getValueClass());
            for (Object element : table) {
                Table.Entry<String, ?> entry = (Table.Entry<String, ?>) element;

                buf.writeString(entry.getKey());
                serializer.write(entry.getValue(), buf);
            }
        }, buf -> {
            try {
                Class valueClass = Class.forName(buf.readString());
                Table table = new Table(valueClass, () -> null);

                NetworkSerializer serializer = getSerializer(valueClass);

                for (int i = 0; i < buf.readVarInt(); ++i) {
                    table.addEntry();
                    table.setKey(i, buf.readString());
                    table.put(i, serializer.read(buf));
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            return null;
        }));
    }
}
