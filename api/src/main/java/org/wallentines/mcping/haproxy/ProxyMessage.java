package org.wallentines.mcping.haproxy;

import java.io.IOException;
import java.io.OutputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;

public record ProxyMessage(Protocol protocol, InetAddress localAddress, InetAddress remoteAddress, int localPort, int remotePort) {

    private static InetAddress toAddress(SocketAddress addr) {

        if(addr instanceof InetSocketAddress insa) {
            return insa.getAddress();
        }

        try {
            return Inet4Address.getByAddress(new byte[]{0, 0, 0, 0});
        } catch (UnknownHostException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static int getPort(SocketAddress addr) {
        if(addr instanceof InetSocketAddress insa) {
            return insa.getPort();
        }

        return 0;
    }

    public void writeV1(OutputStream os) throws IOException {

        String payload = "PROXY " +
                protocol.name() + " " +
                localAddress.getHostAddress() + " " +
                remoteAddress.getHostAddress() + " " +
                localPort + " " +
                remotePort +
                "\r\n";

        os.write(payload.getBytes(StandardCharsets.US_ASCII));
    }

    public static ProxyMessage fromSockets(SocketAddress local, SocketAddress remote) {

        Protocol proto = Protocol.UNKNOWN;
        if(local instanceof InetSocketAddress insa) {
            InetAddress addr = insa.getAddress();
            if(addr instanceof Inet4Address) {
                proto = Protocol.TCP4;
            } else if(addr instanceof Inet6Address) {
                proto = Protocol.TCP6;
            }
        }

        return new ProxyMessage(proto, toAddress(local), toAddress(remote), getPort(local), getPort(remote));
    }

    public enum Protocol {
        UNKNOWN,
        TCP4,
        TCP6
    }


}
