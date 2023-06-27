package org.wallentines.mcping;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

public class LegacyPinger implements Pinger {

    @Override
    public CompletableFuture<PingResponse> pingServer(PingRequest request) {

        CompletableFuture<PingResponse> res = new CompletableFuture<>();

        new Thread(() -> {
            try(Socket socket = new Socket()) {

                socket.connect(new InetSocketAddress(request.hostname(), request.port()), 10000);
                InputStream is = socket.getInputStream();
                OutputStream os = socket.getOutputStream();

                ByteBuf out = Unpooled.buffer();
                encode(request, out);

                os.write(out.array());

                Thread timeoutThread = new Thread(() -> {
                    try {
                        Thread.sleep(10000L);
                        res.complete(null);
                    } catch (InterruptedException ex) {
                        // Ignore.
                    }
                });
                timeoutThread.start();

                ByteBuf response = Unpooled.buffer();
                byte[] buffer = new byte[1024];
                int bytesRead;
                while((bytesRead = is.read(buffer)) != -1) {
                    response.writeBytes(buffer, 0, bytesRead);
                }

                timeoutThread.interrupt();
                res.complete(decode(response));

            } catch (SocketTimeoutException ex) {

                System.out.println("Server connection timed out.");
                res.complete(null);

            } catch (IOException ex) {
                ex.printStackTrace();
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
        buffer.writeShort(11); // Length of plugin message type
        buffer.writeBytes("MC|PingHost".getBytes(StandardCharsets.UTF_16BE)); // Plugin message type

        byte[] hostnameData = request.hostname().getBytes(StandardCharsets.UTF_16BE);

        buffer.writeShort(7 + hostnameData.length); // Remaining data length
        buffer.writeByte(0x49); // Protocol Version (0x4A == the last protocol version using this ping request style)
        buffer.writeShort(request.hostname().length()); // Hostname length
        buffer.writeBytes(hostnameData); // Hostname
        buffer.writeInt(request.port()); // Port

    }

    private static PingResponse decode(ByteBuf buffer) {

        byte packetId = buffer.readByte();
        if(packetId != (byte) 0xFF) {
            System.out.println("Unable to parse legacy ping response! Expected kick packet identifier!");
            return null;
        }

        try {
            short messageLength = buffer.readShort();
            byte[] messageData = new byte[messageLength * 2];

            buffer.readBytes(messageData);
            String message = new String(messageData, StandardCharsets.UTF_16BE);

            String[] fields = message.split("\u0000");
            if (fields.length != 6) {
                System.out.println("Unable to parse legacy ping response! Expected 6 null-delineated fields, found " + fields.length + "!");
                return null;
            }

            int protocolVersion = Integer.parseInt(fields[1]);

            GameVersion version = new GameVersion(fields[2], protocolVersion);
            String motd = fields[3];

            int onlinePlayers = Integer.parseInt(fields[4]);
            int maxPlayers = Integer.parseInt(fields[5]);

            return PingResponse.fromLegacy(version, motd, onlinePlayers, maxPlayers);

        } catch (NumberFormatException ex) {

            System.out.println("Unable to parse legacy ping response! An error occurred while parsing a number!");
            return null;

        } catch (IndexOutOfBoundsException ex) {

            System.out.println("Unable to parse legacy ping response! Attempt to read past the end of the buffer!");
            return null;
        }
    }
}
