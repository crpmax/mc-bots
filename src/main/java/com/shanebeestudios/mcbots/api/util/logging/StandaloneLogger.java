package com.shanebeestudios.mcbots.api.util.logging;

import java.text.SimpleDateFormat;
import java.util.Date;

public class StandaloneLogger extends BaseLogger {

    private static final SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");

    public void log(String prefix, String message) {
        System.out.println("\r[" + formatter.format(new Date()) + " " + prefix + "] " + message);
        //System.out.print(StandaloneLoader.getInstance().getPrompt() + "> ");
    }

    public void error(String error) {
        log("ERROR", error);
    }

    @SuppressWarnings("CallToPrintStackTrace")
    public void error(Exception e) {
        log("ERROR", "");
        e.printStackTrace();
    }

    @Override
    public void info(String format, Object... objects) {
        log("INFO", String.format(format, objects));
    }

    public void info(String... message) {
        log("INFO", String.join(" ", message));
    }

    public void info() {
        log("INFO", "");
    }

    public void warn(String... warning) {
        log("WARN", String.join(" ", warning));
    }

    public void warn() {
        log("WARN", "");
    }

    public void chat(String... chat) {
        log("CHAT", String.join(" ", chat));
    }

    public void critical(Exception e) {
        error(e);
        System.exit(1);
    }

}
