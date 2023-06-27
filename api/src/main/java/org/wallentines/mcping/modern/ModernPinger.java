package org.wallentines.mcping.modern;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.wallentines.mcping.PingRequest;
import org.wallentines.mcping.PingResponse;
import org.wallentines.mcping.Pinger;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.codec.JSONCodec;

import java.util.concurrent.CompletableFuture;

public class ModernPinger implements Pinger {


    @Override
    public CompletableFuture<PingResponse> pingServer(PingRequest request) {

        CompletableFuture<PingResponse> out = new CompletableFuture<>();

        EventLoopGroup group = new NioEventLoopGroup();
        PingInstance instance = new PingInstance(out, group);

        Bootstrap bootstrap = new Bootstrap()
                .group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline()
                                .addLast(new PacketSplitter())
                                .addLast(new PacketDecoder())
                                .addLast(new LengthPrepender())
                                .addLast(new PacketEncoder())
                                .addLast(new PingHandler(request, instance));
                    }
                });

        bootstrap.connect(request.hostname(), request.port())
                .addListener((ChannelFutureListener) future -> {
                    if(!future.isSuccess()) {
                        System.out.println("Unable to connect to server");
                        instance.complete(null);
                    }
                });

        return out;
    }

    private static class PingHandler extends SimpleChannelInboundHandler<Packet> {

        private final PingInstance instance;
        private final PingRequest request;
        private Thread timeoutThread;

        public PingHandler(PingRequest request, PingInstance instance) {
            this.request = request;
            this.instance = instance;
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            super.channelActive(ctx);

            timeoutThread = new Thread(() -> {

                try {
                    Thread.sleep(10000L);
                    instance.complete(null);
                } catch (InterruptedException ex) {
                    // Ignore
                }
            });
            timeoutThread.start();

            ByteBuf handshakeData = Unpooled.buffer();
            PacketUtil.writeVarInt(handshakeData, -1);
            PacketUtil.writeUtf(handshakeData, request.hostname());
            handshakeData.writeShort(25565);
            PacketUtil.writeVarInt(handshakeData, 1);

            ctx.write(new Packet(0, handshakeData)); // Handshake
            ctx.writeAndFlush(new Packet(0, Unpooled.buffer())); // Status
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Packet msg) throws Exception {

            if(msg.packetId() != 0) {
                instance.complete(null);
                return;
            }

            String json = PacketUtil.readUtf(msg.data());
            ConfigSection section = JSONCodec.loadConfig(json).asSection();

            instance.complete(PingResponse.parseModern(section));
            if(timeoutThread != null) {
                timeoutThread.interrupt();
            }
        }
    }

    private static class PingInstance {

        private final CompletableFuture<PingResponse> response;
        private final EventLoopGroup group;

        public PingInstance(CompletableFuture<PingResponse> response, EventLoopGroup group) {
            this.response = response;
            this.group = group;
        }

        public void complete(PingResponse response) {
            if(!this.response.isDone()) {
                this.response.complete(response);
            }
            group.shutdownGracefully();
        }
    }
}
