package org.wallentines.mcping;

import java.util.concurrent.CompletableFuture;

public interface Pinger {

    CompletableFuture<PingResponse> pingServer(PingRequest request);

}
