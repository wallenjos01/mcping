package org.wallentines.mcping.modern.protocol;

import io.netty.buffer.ByteBuf;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PacketRegistry<T> {

    private final Map<Integer, PacketType<T>> packetTypes;

    public PacketRegistry(Collection<PacketType<T>> packetTypes) {

        Map<Integer, PacketType<T>> tp = new HashMap<>();
        for(PacketType<T> pt : packetTypes) {
            tp.put(pt.getId(), pt);
        }

        this.packetTypes = Map.copyOf(tp);
    }

    public PacketType<T> getPacketType(int id) {
        return packetTypes.get(id);
    }

    public Packet<T> read(int id, ByteBuf buf) {
        PacketType<T> pck = getPacketType(id);
        if(pck == null) return null;

        return pck.read(buf);
    }

    public int getId(Packet<T> packet) {

        int id = packet.getType().getId();
        if(!packetTypes.containsKey(id) || packetTypes.get(id) != packet.getType()) {
            return -1;
        }

        return id;
    }

    public static final PacketRegistry<ServerboundPacketHandler> HANDSHAKE = new PacketRegistry<>(List.of(ServerboundHandshakePacket.TYPE));

    public static final PacketRegistry<ClientboundPacketHandler> STATUS_CLIENTBOUND =  new PacketRegistry<>(List.of(ClientboundStatusPacket.TYPE, ClientboundPingPacket.TYPE));
    public static final PacketRegistry<ServerboundPacketHandler> STATUS_SERVERBOUND =  new PacketRegistry<>(List.of(ServerboundStatusPacket.TYPE, ServerboundPingPacket.TYPE));

}
