package org.wallentines.mcping.modern.protocol;

public interface ServerboundPacketHandler {

    void handle(ServerboundHandshakePacket handshake);

    void handle(ServerboundStatusPacket ping);

    void handle(ServerboundPingPacket ping);


}
