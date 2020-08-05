package dev.hephaestus.conrad.impl.network;

import dev.hephaestus.conrad.impl.network.packets.c2s.ConfigInfoC2SPacket;
import dev.hephaestus.conrad.impl.network.packets.c2s.ConfigSaveC2SPacket;
import dev.hephaestus.conrad.impl.network.packets.c2s.PingC2SPacket;
import dev.hephaestus.conrad.impl.network.packets.s2c.ConfigInfoS2CPacket;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;

public class Packets {
	public static void registerServerListeners() {
		ServerSidePacketRegistry.INSTANCE.register(ConfigInfoC2SPacket.ID, ConfigInfoC2SPacket::accept);
		ServerSidePacketRegistry.INSTANCE.register(ConfigSaveC2SPacket.ID, ConfigSaveC2SPacket::accept);
		ServerSidePacketRegistry.INSTANCE.register(PingC2SPacket.ID, PingC2SPacket::accept);
	}

	public static void registerClientListeners() {
		ClientSidePacketRegistry.INSTANCE.register(ConfigInfoS2CPacket.ID, ConfigInfoS2CPacket::accept);
	}
}
