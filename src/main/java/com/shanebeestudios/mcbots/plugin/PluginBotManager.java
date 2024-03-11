package com.shanebeestudios.mcbots.plugin;

import com.shanebeestudios.mcbots.api.BotManager;
import com.shanebeestudios.mcbots.api.Info;
import com.shanebeestudios.mcbots.bot.Bot;
import org.jetbrains.annotations.Nullable;

public class PluginBotManager extends BotManager {

    public PluginBotManager(Info info) {
        super(info);
    }

    public void createBot(@Nullable String name) {
        if (name != null && name.length() > 16) {
            return;
        }

        String botname = name != null ? name : getNickGenerator().nextNick();
        Bot bot = new Bot(this, botname, getInetAddr(), null);
        this.bots.add(bot);
        bot.connect();
    }

}
