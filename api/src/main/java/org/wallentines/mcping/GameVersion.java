package org.wallentines.mcping;

import org.wallentines.mdcfg.ConfigSection;

public record GameVersion(String name, int protocol) {

    public static GameVersion parse(ConfigSection section) {

        if(!section.has("name") || !section.has("protocol")) {
            return null;
        }

        return new GameVersion(section.getString("name"), section.getInt("protocol"));

    }

}
