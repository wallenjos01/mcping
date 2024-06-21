package org.wallentines.mcping;


import org.wallentines.mcping.modern.ModernPinger;
import org.wallentines.mdcfg.ConfigSection;

public class Main {

    public static void main(String[] args) {

        ArgumentParser parser = new ArgumentParser()
                .addOption("address", 'a', "localhost")
                .addOption("port", 'p', "25565")
                .addOption("connectTimeout", 'c', "10000")
                .addOption("pingTimeout", 't', "2500")
                .addFlag("legacy", 'l')
                .addFlag("haproxy", 'h');


        ArgumentParser.ParseResult result = parser.parse(args);
        if(result.isError()) {
            System.out.println(result.getError());
            return;
        }

        ConfigSection sec = result.getOutput().toConfigSection();

        String address = sec.getString("address");
        int port = Integer.parseInt(sec.getString("port"));
        int connectTimeout = Integer.parseInt(sec.getString("connectTimeout"));
        int pingTimeout = Integer.parseInt(sec.getString("pingTimeout"));
        boolean legacy = sec.getBoolean("legacy");

        Pinger pinger = legacy ? new LegacyPinger() : new ModernPinger();

        System.out.println("Attempting to ping " + address + ":" + port);
        pinger.pingServer(new PingRequest(address, port, connectTimeout, pingTimeout, sec.getBoolean("haproxy"))).thenAccept(res -> {

            System.out.println("Response received");
            if(res == null) {
                System.out.println("Received null response.");
                return;
            }

            System.out.printf("Players: %d/%d%n", res.playersOnline(), res.maxPlayers());
            System.out.printf("Description: %s%n", res.motd());
        });

    }

}
