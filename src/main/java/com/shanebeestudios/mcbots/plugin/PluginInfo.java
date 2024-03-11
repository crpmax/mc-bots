package com.shanebeestudios.mcbots.plugin;

import com.shanebeestudios.mcbots.api.Info;
import org.bukkit.Bukkit;

import java.net.Inet4Address;
import java.net.UnknownHostException;

public class PluginInfo extends Info {

    public PluginInfo() {
        super();
        this.autoRespawnDelay = 3000;
        this.useRealNicknames = true;
        this.address = getServerAddress();
        this.port = Bukkit.getPort();
        this.hasGravity = true;
    }

    private String getServerAddress() {
        try {
            return Inet4Address.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

}
