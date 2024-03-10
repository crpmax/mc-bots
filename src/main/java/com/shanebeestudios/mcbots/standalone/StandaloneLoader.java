package com.shanebeestudios.mcbots.standalone;

import com.github.steveice10.mc.auth.service.AuthenticationService;
import com.github.steveice10.mc.protocol.data.status.ServerStatusInfo;
import com.shanebeestudios.mcbots.api.ServerInfo;
import com.shanebeestudios.mcbots.api.Loader;
import com.shanebeestudios.mcbots.bot.Bot;
import com.shanebeestudios.mcbots.bot.BotLoader;
import com.shanebeestudios.mcbots.api.util.Logger;

import java.net.InetSocketAddress;
import java.util.HashSet;

public class StandaloneLoader extends Loader {

    private final StandaloneInfo standaloneInfo;
    private final HashSet<Bot> controlledBots = new HashSet<>();


    private final AuthenticationService authenticationService;
    private final BotLoader botLoader;

    private boolean isMainListenerMissing = true;

    public StandaloneLoader(StandaloneInfo standaloneInfo) {
        super(standaloneInfo);
        this.standaloneInfo = standaloneInfo;

        getAndPrintServerInfo(getInetAddr());

        if (standaloneInfo.isOnline()) {
            OnlineService onlineService = new OnlineService(standaloneInfo);
            this.authenticationService = onlineService.getOnlineAuthenticationService();
        } else {
            this.authenticationService = null;
        }

        // Load bots
        this.botLoader = new BotLoader(this);
        this.botLoader.spin();
        if (standaloneInfo.hasGravity()) {
            this.gravityTimer.startTimer();
        }

        // Load console reader
        ConsoleReader consoleReader = new ConsoleReader(this);
        consoleReader.start();
    }

    private void getAndPrintServerInfo(InetSocketAddress inetAddr) {
        //print info
        Logger.info("Server Address:", inetAddr.getHostString());
        Logger.info("Server Port: " + inetAddr.getPort());
        Logger.info("Bot count: " + standaloneInfo.getBotCount());

        //get and print server info
        ServerInfo serverInfo = new ServerInfo(inetAddr);
        serverInfo.requestInfo();
        ServerStatusInfo statusInfo = serverInfo.getStatusInfo();
        if (statusInfo != null) {
            Logger.info(
                "Server version: "
                    + statusInfo.getVersionInfo().getVersionName()
                    + " (" + statusInfo.getVersionInfo().getProtocolVersion()
                    + ")"
            );
            Logger.info("Player Count: " + statusInfo.getPlayerInfo().getOnlinePlayers()
                + " / " + statusInfo.getPlayerInfo().getMaxPlayers());
            Logger.info();
        } else {
            Logger.warn("There was an error retrieving server status information. The server may be offline or running on a different version.");
        }
    }

    public void renewMainListener() {
        this.bots.get(0).registerMainListener();
    }

    public void removeBot(Bot bot) {
        this.bots.remove(bot);
        controlledBots.remove(bot);
        if (bot.hasMainListener()) {
            Logger.info("Bot with MainListener removed");
            this.isMainListenerMissing = true;
        }
        if (!this.bots.isEmpty()) {
            if (this.isMainListenerMissing && !this.standaloneInfo.isMinimal()) {
                Logger.info("Renewing MainListener");
                renewMainListener();
                this.isMainListenerMissing = false;
            }
        } else {
            if (this.botLoader.getTriedToConnect() == this.standaloneInfo.getBotCount()) {
                Logger.error("All bots disconnected, exiting");
                System.exit(0);
            }
        }
    }

    public String getPrompt() {
        int count = this.getControlledBots().size();
        if (count == 0) {
            return "ALL";
        } else if (count == 1) {
            //If controlling only one bot, return its nickname
            return getControlledBots().iterator().next().getNickname();
        } else {
            return count + " BOTS";
        }
    }

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

    public StandaloneInfo getMainInfo() {
        return this.standaloneInfo;
    }

    public HashSet<Bot> getControlledBots() {
        return this.controlledBots;
    }

    public AuthenticationService getAuthenticationService() {
        return this.authenticationService;
    }

    public boolean isMainListenerMissing() {
        return this.isMainListenerMissing;
    }

    public void setMainListenerMissing(boolean missing) {
        this.isMainListenerMissing = missing;
    }

}
