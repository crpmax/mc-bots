package com.shanebeestudios.mcbots.api.util;

import org.jetbrains.annotations.Nullable;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class Utils {

    /**
     * Get a UUID from a player name
     *
     * @param playerName Player name to fetch UUID from
     * @return UUID from player name if available
     */
    @Nullable
    public static String nameToUUID(String playerName) {
        try {
            URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + playerName);
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(new InputStreamReader(url.openStream()));
            return (String) json.get("id");
        } catch (ParseException | IOException ignore) {
        }
        return null;
    }

}
