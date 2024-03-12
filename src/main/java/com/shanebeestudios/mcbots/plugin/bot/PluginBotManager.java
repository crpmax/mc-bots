package com.shanebeestudios.mcbots.plugin.bot;

import com.shanebeestudios.mcbots.api.BotManager;
import com.shanebeestudios.mcbots.api.bot.Bot;
import com.shanebeestudios.mcbots.api.generator.NickGenerator;
import com.shanebeestudios.mcbots.api.util.logging.Logger;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Nullable;

import java.net.Inet4Address;
import java.net.UnknownHostException;

/**
 * Bot manager for server plugin
 */
public class PluginBotManager extends BotManager {

    public PluginBotManager() {
        this.autoRespawnDelay = 3000;
        this.inetAddr = createInetAddress(getServerAddress(), Bukkit.getPort());
        this.nickGenerator = new NickGenerator("plugins/McBots/nicks.txt", this.nickPrefix, true);
        this.hasGravity = true;
        this.gravityTimer.startTimer();
    }

    /**
     * Create a bot
     *
     * @param name Name of bot or null to create random named bot
     *             Name must be between 1 and 16 characters
     * @return Bot if was created, else null
     */
    @Nullable
    public Bot createBot(@Nullable String name) {
        if (name != null && name.length() > 16) return null;

        String botname = name != null ? name : getNickGenerator().nextNick();
        Bot bot = new Bot(this, botname, getInetAddr(), null);
        this.bots.add(bot);
        bot.setupSessionListener(new PluginSessionListener());
        bot.connect();
        return bot;
    }

    private String getServerAddress() {
        try {
            return Inet4Address.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void logBotCreated(String name) {
        Logger.info("Bot '&b" + name + "&7' created");
    }

    @Override
    public void logBotDisconnected(String name) {
        Logger.info("Bot '&b" + name + "&7' disconnected");
    }

}
