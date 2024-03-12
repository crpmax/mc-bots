package com.shanebeestudios.mcbots.standalone;

import com.shanebeestudios.mcbots.standalone.bot.StandaloneBotManager;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class Main {

    public static void main(String[] args) {
        Options options = new Options();

        options.addOption("c", "count", true, "bot count");

        Option addressOption = new Option("s", "server", true, "server IP[:port]");
        addressOption.setRequired(true);
        options.addOption(addressOption);

        Option delayOption = new Option("d", "delay", true, "connection delay (ms) <min> <max>");
        delayOption.setArgs(2);
        options.addOption(delayOption);

        options.addOption("r", "real", false, "generate real looking nicknames");
        options.addOption("n", "nocolor", false, "dont color & format incoming chat messages");
        options.addOption("p", "prefix", true, "bot nick prefix");
        options.addOption("m", "minimal", false, "minimal run without any listeners");
        options.addOption("x", "most-minimal", false, "minimal run without any control, just connect the bots");
        options.addOption("j", "join-msg", true, "join messages / commands, separated by &&");

        options.addOption("l", "proxy-list", true, "Path or URL to proxy list file with proxy:port on every line");
        options.addOption("t", "proxy-type", true, "Proxy type: SOCKS4 or SOCKS5");

        options.addOption(null, "nicks", true, "Path to nicks file with nick on every line");

        options.addOption("g", "gravity", false, "Try to simulate gravity by falling down");

        options.addOption("o", "online", false, "Use online mode (premium) account");

        options.addOption("ar", "auto-respawn", true, "Set autorespawn delay (-1 to disable)");

        new StandaloneBotManager(options, args);
    }

}
