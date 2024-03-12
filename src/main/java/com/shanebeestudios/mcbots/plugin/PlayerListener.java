package com.shanebeestudios.mcbots.plugin;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.shanebeestudios.mcbots.api.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings({"FieldCanBeLocal", "unused"})
public class PlayerListener implements Listener {

    private final McBotPlugin plugin;
    private final Map<UUID, PlayerProfile> profileMap = new HashMap<>();

    public PlayerListener(McBotPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    private void onPreJoin(AsyncPlayerPreLoginEvent event) {
        String name = event.getName();
        String uuidString = Utils.nameToUUID(name);
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

}
