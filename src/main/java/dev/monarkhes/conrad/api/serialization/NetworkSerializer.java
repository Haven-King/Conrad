package dev.monarkhes.conrad.api.serialization;

import net.minecraft.network.PacketByteBuf;

import java.util.function.BiConsumer;
import java.util.function.Function;

public interface NetworkSerializer<T> {
    void write(Object value, PacketByteBuf buf);
    T read(PacketByteBuf buf);

    static <T> NetworkSerializer<T> of(BiConsumer<T, PacketByteBuf> writer, Function<PacketByteBuf, T> reader) {
        return new NetworkSerializer<T>() {
            @Override
            @SuppressWarnings("unchecked")
            public void write(Object value, PacketByteBuf buf) {
                writer.accept((T) value, buf);
            }

            @Override
            public T read(PacketByteBuf buf) {
                return reader.apply(buf);
            }
        };
    }
}
