package com.shanebeestudios.mcbots.api;

import com.shanebeestudios.mcbots.api.timer.GravityTimer;
import com.shanebeestudios.mcbots.api.generator.NickGenerator;
import com.shanebeestudios.mcbots.bot.Bot;
import org.jetbrains.annotations.Nullable;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

public abstract class BotManager {

    // Fields
    private final Info info;
    private final InetSocketAddress inetAddr;
    protected final ArrayList<Bot> bots = new ArrayList<>();
    protected NickGenerator nickGenerator;
    protected final GravityTimer gravityTimer;

    public BotManager(Info info) {
        this.info = info;
        this.inetAddr = createInetAddress();
        this.nickGenerator = new NickGenerator(info.getNickPath(), info.getNickPrefix(), info.isUseRealNicknames());
        this.gravityTimer = new GravityTimer(this);
    }

    // Methods
    protected InetSocketAddress createInetAddress() {
        try {
            InetAddress inetAddress = InetAddress.getByName(this.info.getAddress());
            return new InetSocketAddress(inetAddress.getHostAddress(), this.info.getPort());
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    public Info getInfoBase() {
        return this.info;
    }

    public InetSocketAddress getInetAddr() {
        return this.inetAddr;
    }

    public ArrayList<Bot> getBots() {
        return this.bots;
    }

    public NickGenerator getNickGenerator() {
        return this.nickGenerator;
    }

    public void removeBot(Bot bot) {
        this.bots.remove(bot);
    }

    public void disconnectBot(Bot bot) {
        bot.disconnect();
        this.bots.remove(bot);
    }

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
