package dev.hephaestus.conrad.impl.entrypoints;

import dev.hephaestus.conrad.impl.network.Packets;
import net.fabricmc.api.ModInitializer;

public class ConradModInitializer implements ModInitializer {
    @Override
    public void onInitialize() {
        Packets.registerServerListeners();
    }
}
