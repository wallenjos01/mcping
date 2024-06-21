package org.wallentines.mcping.modern.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.DecoderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PacketDecoder<T> extends ChannelInboundHandlerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger("PacketDecoder");
    private PacketRegistry<T> registry;

    public PacketDecoder(PacketRegistry<T> registry) {
        this.registry = registry;
    }

    public void setRegistry(PacketRegistry<T> registry) {
        this.registry = registry;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if(msg instanceof ByteBuf buf) {
            decode(ctx, buf);
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    protected void decode(ChannelHandlerContext ctx, ByteBuf bytes) {

        if (!ctx.channel().isActive() || !bytes.isReadable()) {
            bytes.release();
            return;
        }

        int id = PacketUtil.readVarInt(bytes);
        if(registry.getPacketType(id) == null) {
            bytes.clear();
            return;
        }

        Packet<T> p;
        try {
            p = registry.read(id, bytes);
            if(bytes.isReadable()) {
                int extra = bytes.readableBytes();
                throw new DecoderException("Found " + extra + " extra bytes after the end of a packet!");
            }
            ctx.fireChannelRead(p);

        } catch (Exception ex) {
            throw new DecoderException("An error occurred while parsing a packet with id " + id, ex);
        }

        if(bytes.refCnt() == 0) {
            LOGGER.warn("Packet with id {} was already released!", id);
        } else {
            bytes.release();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.error("An error occurred while decoding a packet!", cause);
        ctx.channel().close();
    }
}
