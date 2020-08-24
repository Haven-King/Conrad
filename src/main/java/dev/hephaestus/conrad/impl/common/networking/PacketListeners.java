package dev.hephaestus.conrad.impl.common.networking;

import dev.hephaestus.conrad.impl.common.networking.packets.all.ConfigValuePacket;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;

public class PacketListeners implements ModInitializer {
	@Override
	public void onInitialize() {
		ServerSidePacketRegistry.INSTANCE.register(ConfigValuePacket.INFO, ConfigValuePacket::saveUserInfo);
		ServerSidePacketRegistry.INSTANCE.register(ConfigValuePacket.SAVE, ConfigValuePacket::saveConfigValue);
	}
}
