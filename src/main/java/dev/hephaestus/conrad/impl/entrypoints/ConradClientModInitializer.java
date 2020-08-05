package dev.hephaestus.conrad.impl.entrypoints;

import dev.hephaestus.conrad.impl.network.Packets;
import net.fabricmc.api.ClientModInitializer;

public class ConradClientModInitializer implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Packets.registerClientListeners();
    }
}
