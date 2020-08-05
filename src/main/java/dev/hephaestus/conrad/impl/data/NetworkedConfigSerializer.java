package dev.hephaestus.conrad.impl.data;

import dev.hephaestus.conrad.api.Config;
import dev.hephaestus.conrad.impl.network.packets.ConradPacket;
import dev.hephaestus.conrad.impl.network.packets.c2s.ConfigInfoC2SPacket;
import dev.hephaestus.conrad.impl.network.packets.c2s.ConfigSaveC2SPacket;

public class NetworkedConfigSerializer implements ConfigSerializer {
    public static final NetworkedConfigSerializer INSTANCE = new NetworkedConfigSerializer();

    @Override
    public <T extends Config> void serialize(T config) {
        ConradPacket packet;
        if (config.getClass().getAnnotation(Config.SaveType.class).value() == Config.SaveType.Type.CLIENT) {
            packet = new ConfigInfoC2SPacket(config);
        } else {
            packet = new ConfigSaveC2SPacket(config);
        }

        packet.send();
    }

    @Override
    public <T extends Config> T deserialize(Class<T> configClass) {
        return null;
    }
}
