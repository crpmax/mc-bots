package com.shanebeestudios.mcbots.api.util;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("unused")
public class Logger {

    private static final String PREFIX = "&7[&bMc&3Bot&7]";
    private static final String PREFIX_ERROR = "&7[&bMc&3Bot &cERROR&7]";
    private static final String PREFIX_WARN = "&7[&bMc&3Bot &eWARN&7]";
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
        sender.sendMessage(getColString(PREFIX + " &7" + String.format(format, objects)));
    }

    public static void log(String prefix, String message) {
        String string = getColString(prefix + " " + message);
        Bukkit.getConsoleSender().sendMessage(string);
    }

    public static void error(String error) {
        log(PREFIX_ERROR, "&c" + error);
    }

    @SuppressWarnings("CallToPrintStackTrace")
    public static void error(Exception e) {
        e.printStackTrace();
    }

    public static void info(String format, Object... objects) {
        log(PREFIX, "&7" + String.format(format, objects));
    }

    public static void info(String... message) {
        log(PREFIX, "&7" + String.join(" ", message));
    }

    public static void info() {
        log(PREFIX, "");
    }

    public static void warn(String... warning) {
        log(PREFIX_WARN, String.join(" ", warning));
    }

    public static void critical(Exception e) {
        error(e);
    }

}
