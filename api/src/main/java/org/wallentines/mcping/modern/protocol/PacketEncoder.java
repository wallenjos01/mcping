package org.wallentines.mcping.modern.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.EncoderException;
import io.netty.handler.codec.MessageToByteEncoder;

public class PacketEncoder<T> extends MessageToByteEncoder<Packet<T>> {


    private PacketRegistry<T> registry;

    public PacketEncoder(PacketRegistry<T> registry) {
        this.registry = registry;
    }

    public void setRegistry(PacketRegistry<T> registry) {
        this.registry = registry;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Packet<T> packet, ByteBuf byteBuf) throws Exception {

        if(registry == null) {
            throw new EncoderException("Attempt to send a packet before setting a registry!");
        }

        int id = registry.getId(packet);
        if(id == -1) {
            throw new EncoderException("Attempt to send an unregistered packet with id " + packet.getType().getId() + "!");
        }

        PacketUtil.writeVarInt(byteBuf, registry.getId(packet));

        try {
            packet.write(byteBuf);
        } catch (Exception ex) {
            throw new EncoderException("An exception occurred while sending a packet!", ex);
        }
    }
}
