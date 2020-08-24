package dev.hephaestus.conrad.api.networking;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketByteBuf;

public interface NetworkedObjectWriter<T> {
	ByteBuf write(PacketByteBuf buf, Object value);
}
