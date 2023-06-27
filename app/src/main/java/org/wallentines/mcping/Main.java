package org.wallentines.mcping;


import org.wallentines.mcping.modern.ModernPinger;
import org.wallentines.mdcfg.ConfigSection;

public class Main {

    public static void main(String[] args) {

        ArgumentParser parser = new ArgumentParser()
                .addOption("address", 'a', "localhost")
                .addOption("port", 'p', "25565")
                .addFlag("legacy", 'l');


        ArgumentParser.ParseResult result = parser.parse(args);
        if(result.isError()) {
            System.out.println(result.getError());
            return;
        }

        ConfigSection sec = result.getOutput().toConfigSection();

        String address = sec.getString("address");
        int port = Integer.parseInt(sec.getString("port"));
        boolean legacy = sec.getBoolean("legacy");

        Pinger pinger = legacy ? new LegacyPinger() : new ModernPinger();

        System.out.println("Attempting to ping " + address + ":" + port);
        pinger.pingServer(new PingRequest(address, port)).thenAccept(res -> {

            System.out.println("Response received");
            if(res == null) {
                System.out.println("Received null response.");
                System.exit(1);
            }

            System.out.printf("Players: %d/%d%n", res.onlinePlayers(), res.maxPlayers());
            System.out.printf("Description: %s%n", res.description());
            System.exit(0);
        });

    }

}
