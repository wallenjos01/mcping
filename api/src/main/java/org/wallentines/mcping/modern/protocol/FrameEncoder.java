package org.wallentines.mcping.modern.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class FrameEncoder extends MessageToByteEncoder<ByteBuf> {

    protected void encode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, ByteBuf out) {

        int length = byteBuf.readableBytes();
        PacketUtil.writeVarInt(out, length);
        out.writeBytes(byteBuf);
    }
}
