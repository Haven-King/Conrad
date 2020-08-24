package dev.hephaestus.conrad.impl.client;

import dev.hephaestus.conrad.impl.common.networking.packets.all.ConfigValuePacket;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;

public class PacketListeners implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientSidePacketRegistry.INSTANCE.register(ConfigValuePacket.INFO, ConfigValuePacket::saveConfigValue);
	}
}
