package org.wallentines.mcping.modern.protocol;

import io.netty.buffer.ByteBuf;

public class ServerboundStatusPacket implements Packet<ServerboundPacketHandler> {

    public static final PacketType<ServerboundPacketHandler> TYPE = PacketType.of(0, (ver) -> new ServerboundStatusPacket());

    @Override
    public PacketType<ServerboundPacketHandler> getType() {
        return TYPE;
    }

    @Override
    public void write(ByteBuf buf) { }

    @Override
    public void handle(ServerboundPacketHandler handler) {
        handler.handle(this);
    }
}
