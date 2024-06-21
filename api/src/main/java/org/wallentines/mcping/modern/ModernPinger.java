package org.wallentines.mcping.modern;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.haproxy.*;
import org.wallentines.mcping.PingRequest;
import org.wallentines.mcping.Pinger;
import org.wallentines.mcping.StatusMessage;
import org.wallentines.mcping.modern.protocol.*;

import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

public class ModernPinger implements Pinger {


    @Override
    public CompletableFuture<StatusMessage> pingServer(PingRequest request) {

        CompletableFuture<StatusMessage> out = new CompletableFuture<>();

        EventLoopGroup group = new NioEventLoopGroup();
        PingInstance instance = new PingInstance(out);

        PacketEncoder<ServerboundPacketHandler> encoder = new PacketEncoder<>(PacketRegistry.HANDSHAKE);

        Bootstrap bootstrap = new Bootstrap()
                .group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, request.connectTimeout())
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.pipeline()
                                .addLast(new FrameDecoder())
                                .addLast(new PacketDecoder<>(PacketRegistry.STATUS_CLIENTBOUND))
                                .addLast(new FrameEncoder())
                                .addLast(encoder)
                                .addLast(new PacketHandler<>(new PingHandler(ch, instance)));

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

                    Channel channel = future.channel();

                    // Send HAProxy
                    if(request.haproxy()) {

                        InetSocketAddress source = (InetSocketAddress) channel.localAddress();
                        InetSocketAddress dest = (InetSocketAddress) channel.remoteAddress();


                        HAProxyProxiedProtocol proto = source.getAddress() instanceof Inet4Address ?
                                HAProxyProxiedProtocol.TCP4 :
                                HAProxyProxiedProtocol.TCP6;

                        channel.write(new HAProxyMessage(
                                HAProxyProtocolVersion.V2,
                                HAProxyCommand.PROXY,
                                proto,
                                source.getAddress().getHostAddress(),
                                dest.getAddress().getHostAddress(),
                                source.getPort(),
                                dest.getPort()
                        ));
                    }

                    // Send ping
                    channel.write(new ServerboundHandshakePacket(767, request.hostname(), request.port(), ServerboundHandshakePacket.Intent.STATUS));
                    encoder.setRegistry(PacketRegistry.STATUS_SERVERBOUND);
                    channel.writeAndFlush(new ServerboundStatusPacket());

                })
                .channel()
                .closeFuture()
                        .addListener(future -> group.shutdownGracefully());




        return out;
    }

    private static class PingHandler implements ClientboundPacketHandler {

        private final PingInstance instance;
        private final Channel channel;

        public PingHandler(Channel channel, PingInstance instance) {
            this.channel = channel;
            this.instance = instance;
        }

        @Override
        public void handle(ClientboundPingPacket ping) {

        }

        @Override
        public void handle(ClientboundStatusPacket status) {
            channel.close();
            instance.complete(StatusMessage.deserialize(status.data()));
        }
    }

    private static class PingInstance {

        private final CompletableFuture<StatusMessage> response;
        private final Thread timeoutThread;

        public PingInstance(CompletableFuture<StatusMessage> response) {
            this.response = response;
            this.timeoutThread = new Thread(() -> {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ex) {
                    // Ignore
                }

                if(!this.response.isDone()) {
                    this.response.complete(null);
                }
            });
        }

        public void complete(StatusMessage response) {
            if(!this.response.isDone()) {
                this.response.complete(response);
            }
            this.timeoutThread.interrupt();
        }
    }
}
