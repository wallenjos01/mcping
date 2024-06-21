package org.wallentines.mcping.modern.protocol;


public interface ClientboundPacketHandler {

    void handle(ClientboundPingPacket ping);

    void handle(ClientboundStatusPacket status);

}
