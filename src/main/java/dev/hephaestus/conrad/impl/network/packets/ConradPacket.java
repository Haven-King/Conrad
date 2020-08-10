package dev.hephaestus.conrad.impl.network.packets;

import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class ConradPacket extends PacketByteBuf {
    protected static Logger LOGGER = LogManager.getLogger("ConradNetworking");

    public final Identifier id;
    public final Type type;

    public ConradPacket(Identifier id, Type type) {
        super(Unpooled.buffer());
        this.id = id;
        this.type = type;
    }

    public final void send() {
        if (this.type != Type.S2C) {
            if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
                ClientSidePacketRegistry.INSTANCE.sendToServer(this.id, this);
            } else {
                LOGGER.error("Cannot send C2S packet from server");
            }
        } else {
            LOGGER.error("Cannot send C2S packet without target player");
        }
    }

    public final void send(ServerPlayerEntity player) {
        if (this.type != Type.C2S) {
            ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, this.id, this);
        } else {
            LOGGER.error("Cannot send S2C packet to a player");
        }
    }

    protected enum Type {
        ALL,
        C2S,
        S2C
    }
}
