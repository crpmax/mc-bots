package com.shanebeestudios.mcbots.api.util.logging;

import com.shanebeestudios.mcbots.plugin.McBotPlugin;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PluginLogger extends BaseLogger {

    private static final String PREFIX = "&7[&bMc&3Bot&7]";
    private static final String PREFIX_ERROR = "&7[&bMc&3Bot &cERROR&7]";
    private static final Pattern HEX_PATTERN = Pattern.compile("<#([A-Fa-f\\d]){6}>");

    @SuppressWarnings("deprecation") // Paper deprecation
    public static String getColString(String string) {
        Matcher matcher = HEX_PATTERN.matcher(string);
        while (matcher.find()) {
            final ChatColor hexColor = ChatColor.of(matcher.group().substring(1, matcher.group().length() - 1));
            final String before = string.substring(0, matcher.start());
            final String after = string.substring(matcher.end());
            string = before + hexColor + after;
            matcher = HEX_PATTERN.matcher(string);
        }
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    public static void logToSender(CommandSender sender, String format, Object... objects) {
        sender.sendMessage(getColString(PREFIX  + " &7" + String.format(format, objects)));
    }

    private final Logger bukkitLogger;

    public PluginLogger(McBotPlugin plugin) {
        this.bukkitLogger = plugin.getLogger();
    }

    @Override
    public void log(String prefix, String message) {
        String string = getColString(prefix + " " + message);
        Bukkit.getConsoleSender().sendMessage(string);
    }

    @Override
    public void error(String error) {
        log(PREFIX_ERROR, "&c" + error);
    }

    @SuppressWarnings("CallToPrintStackTrace")
    @Override
    public void error(Exception e) {
        e.printStackTrace();
    }

    @Override
    public void info(String format, Object... objects) {
        log(PREFIX, "&7" + String.format(format, objects));
    }

    @Override
    public void info(String... message) {
        log(PREFIX, "&7" + String.join(" ", message));
    }

    @Override
    public void info() {
        log(PREFIX, "");
    }

    @Override
    public void warn(String... warning) {
        this.bukkitLogger.warning(String.join(" ", warning));
    }

    @Override
    public void warn() {
        this.bukkitLogger.warning("");
    }

    @Override
    public void chat(String... chat) {
        log("[CHAT]", String.join(" ", chat));
    }

    @Override
    public void critical(Exception e) {
        error(e);
    }

}
