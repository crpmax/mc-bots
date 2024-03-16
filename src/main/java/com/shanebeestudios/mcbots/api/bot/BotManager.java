package com.shanebeestudios.mcbots.api.bot;

import com.shanebeestudios.mcbots.api.generator.NickGenerator;
import com.shanebeestudios.mcbots.api.timer.GravityTimer;
import com.shanebeestudios.mcbots.api.util.Logger;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Nullable;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

/**
 * Bot manager for server plugin
 */
@SuppressWarnings("unused")
public class BotManager {

    private final int autoRespawnDelay;
    private final String nickPath = null;
    private final String nickPrefix = "";
    private final boolean hasGravity;
    private final ArrayList<String> joinMessages = new ArrayList<>();
    private final InetSocketAddress inetAddr;
    private final ArrayList<Bot> bots = new ArrayList<>();
    private final NickGenerator nickGenerator;
    private final GravityTimer gravityTimer;

    public BotManager() {
        this.autoRespawnDelay = 3000;
        this.inetAddr = createInetAddress(getServerAddress(), Bukkit.getPort());
        this.nickGenerator = new NickGenerator("plugins/McBots/nicks.txt", this.nickPrefix, true);
        this.hasGravity = true;
        this.gravityTimer = new GravityTimer(this);
        this.gravityTimer.startTimer();
    }

    // Methods
    private InetSocketAddress createInetAddress(String address, int port) {
        try {
            InetAddress inetAddress = InetAddress.getByName(address);
            return new InetSocketAddress(inetAddress.getHostAddress(), port);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    public int getAutoRespawnDelay() {
        return this.autoRespawnDelay;
    }

    public String getNickPath() {
        return this.nickPath;
    }

    public String getNickPrefix() {
        return this.nickPrefix;
    }

    public boolean hasGravity() {
        return this.hasGravity;
    }

    public ArrayList<String> getJoinMessages() {
        return this.joinMessages;
    }

    public InetSocketAddress getInetAddr() {
        return this.inetAddr;
    }

    public GravityTimer getGravityTimer() {
        return this.gravityTimer;
    }

    /**
     * Get all loaded bots
     *
     * @return All loaded bots
     */
    public ArrayList<Bot> getBots() {
        return this.bots;
    }

    /**
     * Get instance of nick generator
     *
     * @return Nick generator
     */
    public NickGenerator getNickGenerator() {
        return this.nickGenerator;
    }

    /**
     * Remove a bot
     *
     * @param bot Bot to remove
     */
    public void removeBot(Bot bot) {
        this.bots.remove(bot);
    }

    /**
     * Disconnect and remove a bot
     *
     * @param bot Bot to disconnect and remove
     */
    public void disconnectBot(Bot bot) {
        bot.disconnect();
        this.bots.remove(bot);
    }

    /**
     * Find a bot by name
     *
     * @param text Name of player to get bot from
     * @return Bot from name
     */
    @Nullable
    public Bot findBotByName(String text) {
        for (Bot bot : this.getBots()) {
            // Starts with and ignore case
            // https://stackoverflow.com/a/38947571/11787611
            if (bot.getNickname().regionMatches(true, 0, text, 0, text.length())) {
                return bot;
            }
        }
        return null;
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

    public void logBotCreated(String name) {
        Logger.info("Bot '&b" + name + "&7' created");
    }

    public void logBotDisconnected(String name) {
        Logger.info("Bot '&b" + name + "&7' disconnected");
    }

}
