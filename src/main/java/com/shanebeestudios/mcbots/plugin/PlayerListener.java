package com.shanebeestudios.mcbots.plugin;

import com.destroystokyo.paper.profile.PlayerProfile;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.Nullable;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerListener implements Listener {

    private final McBotPlugin plugin;
    private final BukkitScheduler scheduler = Bukkit.getScheduler();

    private final Map<UUID,PlayerProfile> profileMap = new HashMap<>();

    public PlayerListener(McBotPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    private void onPreJoin(AsyncPlayerPreLoginEvent event) {
        String name = event.getName();

        String uuidString = nameToUUID(name);
        if (uuidString != null) {
            UUID uuid = UUID.fromString(uuidString.replaceFirst("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5"));
            PlayerProfile profile = Bukkit.createProfile(uuid, name);
            profile.complete(true, true);
            this.profileMap.put(event.getUniqueId(), profile);
        }
    }

    @EventHandler
    private void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (this.profileMap.containsKey(uuid)) {
            player.setPlayerProfile(this.profileMap.get(uuid));
            this.profileMap.remove(uuid);
        }
    }

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
