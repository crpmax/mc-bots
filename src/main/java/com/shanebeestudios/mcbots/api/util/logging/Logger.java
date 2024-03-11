package com.shanebeestudios.mcbots.api.util.logging;

import com.shanebeestudios.mcbots.plugin.McBotPlugin;

@SuppressWarnings("unused")
public class Logger {

    private static BaseLogger INSTANCE = new StandaloneLogger();

    public static void setupBukkitLogging(McBotPlugin plugin) {
        INSTANCE = new PluginLogger(plugin);
    }

    public static void error(String error) {
        INSTANCE.error(error);
    }

    public static void error(Exception e) {
        INSTANCE.error(e);
    }

    public static void info(String format, Object... objects) {
        INSTANCE.info(format, objects);
    }

    public static void info(String... message) {
        INSTANCE.info(message);
    }

    public static void info() {
        INSTANCE.info();
    }

    public static void warn(String... warning) {
        INSTANCE.warn(warning);
    }

    public static void warn() {
        INSTANCE.warn();
    }

    public static void chat(String... chat) {
        INSTANCE.chat(chat);
    }

    public static void critical(Exception e) {
        INSTANCE.critical(e);
    }

}
