package com.shanebeestudios.mcbots.api.timer;

import com.shanebeestudios.mcbots.api.bot.Bot;
import com.shanebeestudios.mcbots.api.BotManager;

import java.util.Timer;
import java.util.TimerTask;

public class GravityTimer {

    private final Timer timer = new Timer();
    private final BotManager botManager;

    public GravityTimer(BotManager botManager) {
        this.botManager = botManager;
    }

    public void startTimer() {
        this.timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                GravityTimer.this.botManager.getBots().forEach(Bot::fallDown);
            }
        }, 1000L, 50L);
    }

}
