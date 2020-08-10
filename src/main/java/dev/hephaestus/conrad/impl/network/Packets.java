package dev.hephaestus.conrad.impl.network;

import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;

import dev.hephaestus.conrad.impl.network.packets.all.ConfigDataPacket;
import dev.hephaestus.conrad.impl.network.packets.c2s.ConfigSaveC2SPacket;
import dev.hephaestus.conrad.impl.network.packets.c2s.PingC2SPacket;

public class Packets {
	public static void registerServerListeners() {
		ServerSidePacketRegistry.INSTANCE.register(ConfigDataPacket.ID, ConfigDataPacket::acceptOnServer);
		ServerSidePacketRegistry.INSTANCE.register(ConfigSaveC2SPacket.ID, ConfigSaveC2SPacket::accept);
		ServerSidePacketRegistry.INSTANCE.register(PingC2SPacket.ID, PingC2SPacket::accept);
	}

	public static void registerClientListeners() {
		ClientSidePacketRegistry.INSTANCE.register(ConfigDataPacket.ID, ConfigDataPacket::acceptOnClient);
	}
}
