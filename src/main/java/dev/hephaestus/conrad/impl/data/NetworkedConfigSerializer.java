package dev.hephaestus.conrad.impl.data;

import dev.hephaestus.conrad.api.Config;
import dev.hephaestus.conrad.impl.network.packets.c2s.ConfigSaveC2SPacket;

public class NetworkedConfigSerializer implements ConfigSerializer {
    public static final NetworkedConfigSerializer INSTANCE = new NetworkedConfigSerializer();

    @Override
    public <T extends Config> void serialize(T config) {
        new ConfigSaveC2SPacket(config).send();
    }

    @Override
    public <T extends Config> T deserialize(Class<T> configClass) {
        return null;
    }
}
