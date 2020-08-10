package dev.hephaestus.conrad.impl.entrypoints;

import net.fabricmc.api.ModInitializer;

import dev.hephaestus.conrad.impl.network.Packets;

public class ConradModInitializer implements ModInitializer {
	@Override
	public void onInitialize() {
		Packets.registerServerListeners();
	}
}
