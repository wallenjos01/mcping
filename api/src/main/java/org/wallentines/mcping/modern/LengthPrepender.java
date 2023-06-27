package org.wallentines.mcping.modern;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class LengthPrepender extends MessageToByteEncoder<ByteBuf> {

    protected void encode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, ByteBuf byteBuf2) {

        int length = byteBuf.readableBytes();
        PacketUtil.writeVarInt(byteBuf2, length);
        byteBuf2.writeBytes(byteBuf, byteBuf.readerIndex(), length);
    }
}
