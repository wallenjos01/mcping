package org.wallentines.mcping.modern.protocol;

import io.netty.buffer.ByteBuf;

public record ServerboundPingPacket(long value) implements Packet<ServerboundPacketHandler> {

    public static final PacketType<ServerboundPacketHandler> TYPE =  PacketType.of(1, (buf) -> new ServerboundPingPacket(buf.readLong()));

    @Override
    public PacketType<ServerboundPacketHandler> getType() {
        return TYPE;
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeLong(value);
    }

    @Override
    public void handle(ServerboundPacketHandler handler) {
        handler.handle(this);
    }
}
