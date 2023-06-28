package org.wallentines.mcping;

public record PingRequest(String hostname, int port, int connectTimeout, int pingTimeout) {



}
