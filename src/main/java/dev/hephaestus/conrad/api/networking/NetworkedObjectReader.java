package dev.hephaestus.conrad.api.networking;

import net.minecraft.network.PacketByteBuf;

public interface NetworkedObjectReader<T> {
	T read(PacketByteBuf buf);
}
