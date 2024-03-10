package com.shanebeestudios.mcbots.api.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {

    private static final SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");

    public static void log(String prefix, String message) {
        System.out.println("\r[" + formatter.format(new Date()) + " " + prefix + "] " + message);
        //System.out.print(StandaloneLoader.getInstance().getPrompt() + "> ");
    }

    public static void error(String error) {
        log("ERROR", error);
    }

    @SuppressWarnings("CallToPrintStackTrace")
    public static void error(Exception e) {
        log("ERROR", "");
        e.printStackTrace();
    }

    public static void info(String... message) {
        log("INFO", String.join(" ", message));
    }

    public static void info() {
        log("INFO", "");
    }

    public static void warn(String... warning) {
        log("WARN", String.join(" ", warning));
    }

    public static void warn() {
        log("WARN", "");
    }

    public static void chat(String... chat) {
        log("CHAT", String.join(" ", chat));
    }

    public static void critical(Exception e) {
        error(e);
        System.exit(1);
    }

}
