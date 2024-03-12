package com.shanebeestudios.mcbots.api.listener;

import com.github.steveice10.packetlib.Session;
import com.github.steveice10.packetlib.event.session.SessionAdapter;
import com.shanebeestudios.mcbots.api.BotManager;
import com.shanebeestudios.mcbots.api.bot.Bot;

import java.util.ArrayList;

public class BaseSessionListener extends SessionAdapter {

    protected Bot bot;
    protected Session client;
    protected BotManager botManager;
    protected ArrayList<String> joinMessages;
    protected int autoRespawnDelay;

    public BaseSessionListener init(Bot bot) {
        this.bot = bot;
        this.client = bot.getClient();
        this.botManager = bot.getBotManager();
        this.joinMessages = this.botManager.getJoinMessages();
        this.autoRespawnDelay = this.botManager.getAutoRespawnDelay();
        return this;
    }
    
}
