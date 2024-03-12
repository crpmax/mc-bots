package com.shanebeestudios.mcbots.plugin;

import com.shanebeestudios.mcbots.api.util.logging.Logger;
import com.shanebeestudios.mcbots.plugin.bot.PluginBotManager;
import com.shanebeestudios.mcbots.plugin.command.Command;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import dev.jorel.commandapi.exceptions.UnsupportedVersionException;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

@SuppressWarnings("unused")
public class McBotPlugin extends JavaPlugin {

    private static McBotPlugin instance;
    private boolean commandApiCanLoad;
    private PluginBotManager pluginBotManager;

    @Override
    public void onLoad() {
        try {
            CommandAPI.onLoad(new CommandAPIBukkitConfig(this).verboseOutput(false));
            this.commandApiCanLoad = true;
        } catch (UnsupportedVersionException ignore) {
            this.commandApiCanLoad = false;
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onEnable() {
        long start = System.currentTimeMillis();
        instance = this;
        Logger.setupBukkitLogging(this);

        PluginManager pluginManager = Bukkit.getPluginManager();
        if (!this.commandApiCanLoad) {
            Logger.error("CommandAPI could not be loaded, plugin disabling!");
            pluginManager.disablePlugin(this);
            return;
        }

        if (Bukkit.getOnlineMode()) {
            Logger.error("This plugin will only work in offline mode!");
            pluginManager.disablePlugin(this);
            return;
        }

        loadNicknameFile();
        setupBotLogic();
        setupCommand();

        long finish = System.currentTimeMillis() - start;
        String version = getDescription().getVersion();
        Logger.info("&aSuccessfully enabled v%s&7 in &b%s ms", version, finish);
    }

    @Override
    public void onDisable() {
        CommandAPI.onDisable();
    }

    private void loadNicknameFile() {
        this.saveResource("nicks.txt", false);
    }

    private void setupBotLogic() {
        this.pluginBotManager = new PluginBotManager();
    }

    private void setupCommand() {
        new Command(this);
        Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);
    }

    // Getters
    public static McBotPlugin getInstance() {
        return instance;
    }

    public PluginBotManager getPluginBotManager() {
        return this.pluginBotManager;
    }

}