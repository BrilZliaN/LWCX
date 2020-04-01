/*
 * Copyright evilmidget38. Obtained from: https://gist.github.com/evilmidget38/a5c971d2f2b2c3b3fb37
 * I (Hidendra) have made minor changes (mainly removing unused code)
 */

package com.griefcraft.util;

import com.google.common.collect.ImmutableList;
import com.griefcraft.lwc.LWC;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;

public class NameFetcher implements Callable<Map<UUID, String>> {
    private static String profileUrl = LWC.getInstance().getConfiguration().getString("yggdrasil.profileSessionServer",
        "https://sessionserver.mojang.com/session/minecraft/profile/");
    private final JSONParser jsonParser = new JSONParser();
    private final List<UUID> uuids;

    public NameFetcher(List<UUID> uuids) {
        this.uuids = ImmutableList.copyOf(uuids);
    }

    public Map<UUID, String> call() throws Exception {
        Map<UUID, String> uuidStringMap = new HashMap<>();
        for (UUID uuid : uuids) {
            HttpURLConnection connection = (HttpURLConnection) new URL(profileUrl + uuid.toString().replace("-", "")).openConnection();
            connection.setConnectTimeout(10000);
            JSONObject response = (JSONObject) jsonParser.parse(new InputStreamReader(connection.getInputStream()));
            String name = (String) response.get("name");
            if (name == null) {
                continue;
            }
            String cause = (String) response.get("cause");
            String errorMessage = (String) response.get("errorMessage");
            if (cause != null && cause.length() > 0) {
                throw new IllegalStateException(errorMessage);
            }
            uuidStringMap.put(uuid, name);
        }
        return uuidStringMap;
    }
}
