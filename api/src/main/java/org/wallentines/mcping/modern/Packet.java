package org.wallentines.mcping.modern;

import io.netty.buffer.ByteBuf;

public record Packet(int packetId, ByteBuf data) {

}
