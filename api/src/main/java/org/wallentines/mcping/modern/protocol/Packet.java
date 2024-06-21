package org.wallentines.mcping.modern.protocol;

import io.netty.buffer.ByteBuf;

public interface Packet<T> {

    PacketType<T> getType();

    void write(ByteBuf buf);

    void handle(T handler);

}
