package org.wallentines.mcping.modern;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.haproxy.*;
import org.wallentines.mcping.PingRequest;
import org.wallentines.mcping.PingResponse;
import org.wallentines.mcping.Pinger;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.codec.JSONCodec;

import java.net.Inet4Address;
import java.net.InetSocketAddress;
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
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, request.connectTimeout())
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.pipeline()
                                .addLast(new PacketSplitter())
                                .addLast(new PacketDecoder())
                                .addLast(new LengthPrepender())
                                .addLast(new PacketEncoder())
                                .addLast(new PingHandler(request, instance));

                        if(request.haproxy()) {
                            ch.pipeline().addFirst(HAProxyMessageEncoder.INSTANCE);
                        }
                    }
                });


        bootstrap.connect(request.hostname(), request.port())
                .addListener((ChannelFutureListener) future -> {
                    if(!future.isSuccess()) {
                        instance.complete(null);
                    }
                }).channel();




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
                    Thread.sleep(request.pingTimeout());
                    instance.complete(null);
                } catch (InterruptedException ex) {
                    // Ignore
                }
            });
            timeoutThread.start();

            if(request.haproxy()) {

                InetSocketAddress source = (InetSocketAddress) ctx.channel().localAddress();
                InetSocketAddress dest = (InetSocketAddress) ctx.channel().remoteAddress();


                HAProxyProxiedProtocol proto = source.getAddress() instanceof Inet4Address ?
                        HAProxyProxiedProtocol.TCP4 :
                        HAProxyProxiedProtocol.TCP6;

                ctx.write(new HAProxyMessage(
                        HAProxyProtocolVersion.V2,
                        HAProxyCommand.PROXY,
                        proto,
                        source.getAddress().getHostAddress(),
                        dest.getAddress().getHostAddress(),
                        source.getPort(),
                        dest.getPort()
                ));
            }

            ByteBuf handshakeData = Unpooled.buffer();
            PacketUtil.writeVarInt(handshakeData, -1);
            PacketUtil.writeUtf(handshakeData, request.hostname());
            handshakeData.writeShort(25565);
            PacketUtil.writeVarInt(handshakeData, 1);

            ctx.write(new Packet(0, handshakeData)); // Handshake
            ctx.writeAndFlush(new Packet(0, Unpooled.EMPTY_BUFFER)); // Status
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Packet msg) {

            if(timeoutThread != null) {
                timeoutThread.interrupt();
            }
            if(msg.packetId() != 0) {
                instance.complete(null);
                return;
            }

            String json = PacketUtil.readUtf(msg.data());
            ConfigSection section = JSONCodec.loadConfig(json).asSection();

            instance.complete(PingResponse.parseModern(section));
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
