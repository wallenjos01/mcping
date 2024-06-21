package org.wallentines.mcping.modern.protocol;

import io.netty.buffer.ByteBuf;
import org.wallentines.mdcfg.Functions;

public interface PacketType<T> {

    int getId();

    Packet<T> read(ByteBuf buf);

    static <T> PacketType<T> of(int id, Functions.F1<ByteBuf, Packet<T>> reader) {
        return new PacketType<>() {
            @Override
            public int getId() {
                return id;
            }

            @Override
            public Packet<T> read(ByteBuf buf) {
                return reader.apply(buf);
            }
        };
    }

}
