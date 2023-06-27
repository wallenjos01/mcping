package org.wallentines.mcping;

import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.codec.JSONCodec;
import org.wallentines.mdcfg.serializer.ConfigContext;

import java.util.Collection;
import java.util.List;

public record PingResponse(GameVersion version, int maxPlayers, int onlinePlayers, Collection<PlayerInfo> samplePlayers, String description, String faviconB64, boolean enforcesSecureChat, boolean previewsChat) {



    public static PingResponse parseModern(ConfigSection section) {

        GameVersion version = section.hasSection("version") ? GameVersion.parse(section.getSection("version")) : null;
        ConfigSection players = section.getSection("players");

        int maxPlayers = players.getInt("max");
        int onlinePlayers = players.getInt("online");

        Collection<PlayerInfo> samplePlayers = players.hasList("sample") ? players.getListFiltered("sample", PlayerInfo.SERIALIZER) : List.of();

        String description = "";
        if(section.has("description")) {
            if(section.hasSection("description")) {
                description = JSONCodec.minified().encodeToString(ConfigContext.INSTANCE, section.getSection("description"));
            } else {
                description = section.getString("description");
            }
        }
        String faviconB64 = section.getOrDefault("favicon", (String) null);

        boolean enforcesSecureChat = section.getOrDefault("enforcesSecureChat", false);
        boolean previewsChat = section.getOrDefault("previewsChat", false);

        return new PingResponse(version, maxPlayers, onlinePlayers, samplePlayers, description, faviconB64, enforcesSecureChat, previewsChat);
    }


    public static PingResponse fromLegacy(GameVersion version, String motd, int onlinePlayers, int maxPlayers) {

        return new PingResponse(version, maxPlayers, onlinePlayers, List.of(), motd, null, false, false);
    }

}
