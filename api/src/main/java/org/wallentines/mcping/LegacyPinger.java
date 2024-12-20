package org.wallentines.mcping;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wallentines.mcping.haproxy.ProxyMessage;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

public class LegacyPinger implements Pinger {

    private static final Logger log = LoggerFactory.getLogger(LegacyPinger.class);

    @Override
    public CompletableFuture<StatusMessage> pingServer(PingRequest request) {

        CompletableFuture<StatusMessage> res = new CompletableFuture<>();

        new Thread(() -> {
            try(Socket socket = new Socket()) {

                socket.connect(new InetSocketAddress(request.hostname(), request.port()), 0);
                InputStream is = socket.getInputStream();
                OutputStream os = socket.getOutputStream();

                if(request.haproxy()) {
                    SocketAddress local = socket.getLocalSocketAddress();
                    SocketAddress remote = socket.getRemoteSocketAddress();
                    ProxyMessage.fromSockets(local, remote).writeV1(os);
                }

                ByteBuf out = Unpooled.buffer();
                encode(request, out);

                os.write(out.array());

                ByteBuf response = Unpooled.buffer();
                byte[] buffer = new byte[1024];
                int bytesRead;
                while((bytesRead = is.read(buffer)) != -1) {
                    response.writeBytes(buffer, 0, bytesRead);
                }

                if(response.writerIndex() == 0) {
                    res.complete(null);
                    return;
                }

                res.complete(decode(response));

            } catch (SocketTimeoutException ex) {

                res.complete(null);

            } catch (IOException ex) {
                log.error("An exception occurred while handling a legacy ping!", ex);
                res.complete(null);
            }
        }).start();

        return res;
    }

    private static void encode(PingRequest request, ByteBuf buffer) {

        // Write payload
        buffer.writeByte(0xFE); // Ping packet ID
        buffer.writeByte(0x01); // Ping payload
        buffer.writeByte(0xFA); // Plugin message packet ID
        buffer.writeShort(11); // Length of plugin message channel
        buffer.writeBytes("MC|PingHost".getBytes(StandardCharsets.UTF_16BE)); // Plugin message channel

        byte[] hostnameData = request.hostname().getBytes(StandardCharsets.UTF_16BE);

        buffer.writeShort(7 + hostnameData.length); // Remaining data length
        buffer.writeByte(78); // Protocol Version (1.6.4)
        buffer.writeShort(request.hostname().length()); // Hostname length
        buffer.writeBytes(hostnameData); // Hostname
        buffer.writeInt(request.port()); // Port

    }

    private static StatusMessage decode(ByteBuf buffer) {

        byte packetId = buffer.readByte();
        if(packetId != (byte) 0xFF) {
            throw new IllegalArgumentException("Unable to parse legacy ping response! Expected kick packet identifier!");
        }

        try {
            short messageLength = buffer.readShort();
            byte[] messageData = new byte[messageLength * 2];

            buffer.readBytes(messageData);
            String message = new String(messageData, StandardCharsets.UTF_16BE);

            String[] fields = message.split("\u0000");
            if (fields.length != 6) {
                throw new IllegalArgumentException("Unable to parse legacy ping response! Expected 6 null-delineated fields, found " + fields.length + "!");
            }

            int protocolVersion = Integer.parseInt(fields[1]);

            GameVersion version = new GameVersion(fields[2], protocolVersion);
            String motd = fields[3];

            int onlinePlayers = Integer.parseInt(fields[4]);
            int maxPlayers = Integer.parseInt(fields[5]);

            return StatusMessage.fromLegacy(version, motd, onlinePlayers, maxPlayers);

        } catch (NumberFormatException ex) {

            throw new IllegalArgumentException("Unable to parse legacy ping response! An error occurred while parsing a number!");

        } catch (IndexOutOfBoundsException ex) {

            throw new IllegalArgumentException("Unable to parse legacy ping response! Attempt to read past the end of the buffer!");
        }
    }
}
