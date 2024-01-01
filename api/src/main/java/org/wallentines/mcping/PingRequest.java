package org.wallentines.mcping;

public record PingRequest(String hostname, int port, int connectTimeout, int pingTimeout, boolean haproxy) {

    public PingRequest(String hostname, int port, int connectTimeout, int pingTimeout) {
        this(hostname, port, connectTimeout, pingTimeout, false);
    }

}
