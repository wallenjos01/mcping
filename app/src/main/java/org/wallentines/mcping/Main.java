package org.wallentines.mcping;


import org.wallentines.mcping.modern.ModernPinger;
import org.wallentines.mdcfg.ConfigSection;

import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String[] args) {

        ArgumentParser parser = new ArgumentParser()
                .addOption("address", 'a', "localhost")
                .addOption("port", 'p', "25565")
                .addOption("timeout", 't', "10000")
                .addFlag("legacy", 'l')
                .addFlag("haproxy", 'h')
                .addFlag("quiet", 'q');


        ArgumentParser.ParseResult result = parser.parse(args);
        if(result.isError()) {
            System.out.println(result.getError());
            return;
        }

        ConfigSection sec = result.getOutput().toConfigSection();
        System.exit(new Main(sec).ping());
    }

    private final String address;
    private final int port;
    private final long timeout;
    private final boolean haproxy;
    private final boolean legacy;
    private final boolean quiet;

    private Main(ConfigSection config) {

        address = config.getString("address");
        port = Integer.parseInt(config.getString("port"));
        timeout = Long.parseLong(config.getString("timeout"));
        haproxy = config.getBoolean("haproxy");
        legacy = config.getBoolean("legacy");
        quiet = config.getBoolean("quiet");

    }

    private void log(String message) {
        if(!quiet) {
            System.out.println(message);
        }
    }

    private int ping() {

        Pinger pinger = legacy ? new LegacyPinger() : new ModernPinger();

        log("Attempting to ping " + address + ":" + port);
        StatusMessage message = pinger.pingServer(new PingRequest(address, port, haproxy))
                .orTimeout(timeout, TimeUnit.MILLISECONDS)
                .exceptionally(th -> null)
                .join();

        if(message == null) return 1;

        log("Players: " + message.playerSample() + "/" + message.maxPlayers());
        log("Motd: " + message.motd());

        return 0;
    }


}
