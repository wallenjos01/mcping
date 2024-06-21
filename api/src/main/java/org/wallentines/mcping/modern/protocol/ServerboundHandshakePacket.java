package org.wallentines.mcping.modern.protocol;

import io.netty.buffer.ByteBuf;

import java.util.Locale;

public record ServerboundHandshakePacket(int protocolVersion, String address, int port, Intent intent) implements Packet<ServerboundPacketHandler> {

    public static final PacketType<ServerboundPacketHandler> TYPE = PacketType.of(0, ServerboundHandshakePacket::read);

    @Override
    public PacketType<ServerboundPacketHandler> getType() {
        return TYPE;
    }

    @Override
    public void write(ByteBuf buf) {
        PacketUtil.writeVarInt(buf, protocolVersion);
        PacketUtil.writeUtf(buf, address);
        buf.writeShort(port);
        PacketUtil.writeVarInt(buf, intent.getId());
    }

    @Override
    public void handle(ServerboundPacketHandler handler) {
        handler.handle(this);
    }

    public static ServerboundHandshakePacket read(ByteBuf buffer) {

        int proto = PacketUtil.readVarInt(buffer);
        String addr = PacketUtil.readUtf(buffer, 255).toLowerCase(Locale.ROOT);
        int port = buffer.readUnsignedShort();

        int intentId = PacketUtil.readVarInt(buffer);
        Intent intent = Intent.byId(intentId);
        if(intent == null) {
            throw new IllegalStateException("Client sent handshake with invalid intent! (" + intentId + ")");
        }

        return new ServerboundHandshakePacket(proto, addr, port, intent);
    }


    public enum Intent {
        STATUS(1),
        LOGIN(2),
        TRANSFER(3);

        private final int id;

        Intent(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

        public static Intent byId(int id) {
            if(id < 1 || id > 3) return null;
            return values()[id - 1];
        }
    }
}
