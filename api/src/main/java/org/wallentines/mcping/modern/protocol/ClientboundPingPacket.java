package org.wallentines.mcping.modern.protocol;

import io.netty.buffer.ByteBuf;

public record ClientboundPingPacket(long value) implements Packet<ClientboundPacketHandler> {

    public static final PacketType<ClientboundPacketHandler> TYPE =  PacketType.of(1, (buf) -> new ClientboundPingPacket(buf.readLong()));

    @Override
    public PacketType<ClientboundPacketHandler> getType() {
        return TYPE;
    }

    @Override
    public void handle(ClientboundPacketHandler handler) {
        handler.handle(this);
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeLong(value);
    }
}
