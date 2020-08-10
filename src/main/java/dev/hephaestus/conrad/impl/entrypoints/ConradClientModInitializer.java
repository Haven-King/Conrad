package dev.hephaestus.conrad.impl.entrypoints;

import net.fabricmc.api.ClientModInitializer;

import dev.hephaestus.conrad.impl.network.Packets;

public class ConradClientModInitializer implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		Packets.registerClientListeners();
	}
}
