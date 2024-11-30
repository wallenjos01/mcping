package org.wallentines.mcping;

public record PingRequest(String hostname, int port, boolean haproxy) {

    public PingRequest(String hostname, int port) {
        this(hostname, port,false);
    }

}
