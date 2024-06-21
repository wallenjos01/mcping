package org.wallentines.mcping;

import org.jetbrains.annotations.Nullable;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.codec.JSONCodec;
import org.wallentines.mdcfg.serializer.ConfigContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;


public record StatusMessage(GameVersion version, int playersOnline, int maxPlayers, Collection<PlayerInfo> playerSample,
                            String motd, @Nullable String favicon, boolean secureChat, boolean previewChat) {

    public static StatusMessage fromLegacy(GameVersion version, String motd, int onlinePlayers, int maxPlayers) {
        return new StatusMessage(version, onlinePlayers, maxPlayers, List.of(), motd, null, false, false);
    }


    public static StatusMessage deserialize(ConfigSection section) {

        String motd = JSONCodec.minified().encodeToString(ConfigContext.INSTANCE, section.getOptional("description", ConfigSection.SERIALIZER).orElse(new ConfigSection()));
        String icon = section.getOrDefault("icon", (String) null);
        boolean secureChat = section.getOrDefault("enforcesSecureChat", false);
        boolean previewsChat = section.getOrDefault("previewsChat", false);

        int maxPlayers = 0;
        int onlinePlayers = 0;
        Collection<PlayerInfo> info = new ArrayList<>();

        Optional<ConfigSection> players = section.getOptional("players", ConfigSection.SERIALIZER);
        if(players.isPresent()) {
            maxPlayers = players.get().getOrDefault("max", 0).intValue();
            onlinePlayers = players.get().getOrDefault("online", 0).intValue();
            info = players.get().getOptional("sample", PlayerInfo.SERIALIZER.filteredListOf()).orElse(info);
        }

        int protocol = 0;
        String name = "Unknown";

        Optional<ConfigSection> version = section.getOptional("version", ConfigSection.SERIALIZER);
        if(version.isPresent()) {
            protocol = version.get().getOrDefault("protocol", 0).intValue();
            name = version.get().getOrDefault("name", "Unknown");
        }

        return new StatusMessage(new GameVersion(name, protocol), onlinePlayers, maxPlayers, info, motd, icon, secureChat, previewsChat);
    }

    public ConfigSection serialize() {

        return new ConfigSection()
                .with("version", new ConfigSection()
                        .with("name", version.name())
                        .with("protocol", version.protocol())
                )
                .with("players", new ConfigSection()
                        .with("max", maxPlayers)
                        .with("online", playersOnline)
                        .with("sample", playerSample, PlayerInfo.SERIALIZER.filteredListOf())
                )
                .with("description", motd)
                .with("favicon", favicon)
                .with("enforcesSecureChat", secureChat)
                .with("previewsChat", previewChat);
    }


}
