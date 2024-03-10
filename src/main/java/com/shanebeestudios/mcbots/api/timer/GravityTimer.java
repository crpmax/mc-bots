package com.shanebeestudios.mcbots.api.timer;

import com.shanebeestudios.mcbots.bot.Bot;
import com.shanebeestudios.mcbots.api.Loader;

import java.util.Timer;
import java.util.TimerTask;

public class GravityTimer {

    private final Timer timer = new Timer();
    private final Loader loader;

    public GravityTimer(Loader loader) {
        this.loader = loader;
    }

    public void startTimer() {
        this.timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                GravityTimer.this.loader.getBots().forEach(Bot::fallDown);
            }
        }, 1000L, 50L);
    }

}
