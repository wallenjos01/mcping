package org.wallentines.mcping.modern.protocol;

import io.netty.buffer.ByteBuf;
import org.wallentines.mcping.StatusMessage;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.codec.JSONCodec;
import org.wallentines.mdcfg.serializer.ConfigContext;

public record ClientboundStatusPacket(ConfigSection data) implements Packet<ClientboundPacketHandler> {


    public ClientboundStatusPacket(StatusMessage message) {
        this(message.serialize());
    }

    public static final PacketType<ClientboundPacketHandler> TYPE = PacketType.of(0, ClientboundStatusPacket::read);

    @Override
    public PacketType<ClientboundPacketHandler> getType() {
        return TYPE;
    }

    @Override
    public void write(ByteBuf buf) {

        PacketUtil.writeUtf(buf, JSONCodec.minified().encodeToString(ConfigContext.INSTANCE, data));
    }

    @Override
    public void handle(ClientboundPacketHandler handler) {
        handler.handle(this);
    }

    public static ClientboundStatusPacket read(ByteBuf buf) {

        return new ClientboundStatusPacket(JSONCodec.loadConfig(PacketUtil.readUtf(buf)).asSection());
    }
}
