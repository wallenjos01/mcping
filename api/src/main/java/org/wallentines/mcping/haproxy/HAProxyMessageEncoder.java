package org.wallentines.mcping.haproxy;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class HAProxyMessageEncoder extends MessageToByteEncoder<ProxyMessage> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, ProxyMessage packet, ByteBuf byteBuf) throws Exception {
        packet.writeV1(new ByteBufOutputStream(byteBuf));
    }
}
