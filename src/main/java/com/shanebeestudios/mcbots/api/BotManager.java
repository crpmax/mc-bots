package com.shanebeestudios.mcbots.api;

import com.shanebeestudios.mcbots.api.generator.NickGenerator;
import com.shanebeestudios.mcbots.api.timer.GravityTimer;
import com.shanebeestudios.mcbots.api.util.logging.Logger;
import com.shanebeestudios.mcbots.api.bot.Bot;
import org.jetbrains.annotations.Nullable;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

/**
 * Base bot manager
 */
public abstract class BotManager {

    protected int autoRespawnDelay;
    protected boolean minimal = false;
    protected boolean mostMinimal = false;
    protected String nickPath = null;
    protected String nickPrefix = "";
    protected boolean hasGravity;
    protected ArrayList<String> joinMessages = new ArrayList<>();
    protected InetSocketAddress inetAddr;
    protected final ArrayList<Bot> bots = new ArrayList<>();
    protected NickGenerator nickGenerator;
    protected final GravityTimer gravityTimer;

    public BotManager() {
        this.gravityTimer = new GravityTimer(this);
    }

    // Methods
    protected InetSocketAddress createInetAddress(String address, int port) {
        try {
            InetAddress inetAddress = InetAddress.getByName(address);
            return new InetSocketAddress(inetAddress.getHostAddress(), port);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isMinimal() {
        return this.minimal;
    }

    public boolean isMostMinimal() {
        return this.mostMinimal;
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

    public void logBotCreated(String name) {
        Logger.info("Bot '" + name + "' created");
    }

    public void logBotDisconnected(String name) {
        Logger.info("Bot '" + name + "' disconnected");
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

}
