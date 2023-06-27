package org.wallentines.mcping;

import org.wallentines.mdcfg.serializer.ObjectSerializer;
import org.wallentines.mdcfg.serializer.Serializer;

import java.util.UUID;

public record PlayerInfo(String username, UUID uuid) {

    public static final Serializer<PlayerInfo> SERIALIZER = ObjectSerializer.create(
            Serializer.STRING.entry("name", PlayerInfo::username),
            Serializer.UUID.entry("id", PlayerInfo::uuid),
            PlayerInfo::new
    );

}
