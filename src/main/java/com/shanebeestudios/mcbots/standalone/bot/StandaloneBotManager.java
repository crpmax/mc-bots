package com.shanebeestudios.mcbots.standalone.bot;

import com.github.steveice10.mc.auth.service.AuthenticationService;
import com.github.steveice10.mc.protocol.data.status.ServerStatusInfo;
import com.shanebeestudios.mcbots.api.BotManager;
import com.shanebeestudios.mcbots.api.ServerInfo;
import com.shanebeestudios.mcbots.api.generator.NickGenerator;
import com.shanebeestudios.mcbots.api.generator.ProxyGenerator;
import com.shanebeestudios.mcbots.api.util.Pair;
import com.shanebeestudios.mcbots.api.util.logging.Logger;
import com.shanebeestudios.mcbots.api.bot.Bot;
import com.shanebeestudios.mcbots.api.bot.BotLoader;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.jetbrains.annotations.Nullable;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.SRVRecord;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

import java.net.InetSocketAddress;
import java.util.HashSet;

public class StandaloneBotManager extends BotManager {

    protected int botCount;
    private final int delayMin;
    private final int delayMax;
    private final boolean coloredChat;
    private final boolean online;
    private ProxyGenerator proxyGenerator;
    private final HashSet<Bot> controlledBots = new HashSet<>();
    private final AuthenticationService authenticationService;
    private final BotLoader botLoader;

    private boolean isMainListenerMissing = true;

    public StandaloneBotManager(Options options, String[] args) {
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("mcbots", e.getMessage(), options, "\nhttps://github.com/ShaneBeee/mc-bots", true);
            System.exit(1);
        }

        this.autoRespawnDelay = Integer.parseInt(cmd.getOptionValue("ar", "100"));
        boolean hasProxy = cmd.hasOption('t') && cmd.hasOption('l');

        if (hasProxy) {
            String proxyType = cmd.getOptionValue('t').toUpperCase();
            String proxyPath = cmd.getOptionValue("l");
            this.proxyGenerator = new ProxyGenerator(proxyType, proxyPath);
        }

        this.botCount = Integer.parseInt(cmd.getOptionValue('c', "1"));
        this.minimal = cmd.hasOption('m');
        if (cmd.hasOption('x')) {
            this.minimal = this.mostMinimal = true;
        }

        if (cmd.hasOption('d')) {
            String[] delays = cmd.getOptionValues('d');
            Pair<Integer, Integer> delay = parseDelays(delays);
            this.delayMin = delay.getFirst();
            this.delayMax = delay.getSecond();
        } else {
            this.delayMin = 4000;
            this.delayMax = 5000;
        }

        String address = cmd.getOptionValue('s');
        Pair<String, Integer> addressPort = parseAddress(address);
        this.inetAddr = createInetAddress(addressPort.getFirst(), addressPort.getSecond());

        this.coloredChat = !cmd.hasOption('n');

        if (cmd.hasOption('j')) {
            // Split messages by &&, trim and append to arraylist
            String[] messages = cmd.getOptionValue('j').split("&&");
            for (String msg : messages) {
                this.joinMessages.add(msg.trim());
            }
        }

        boolean useRealNick = cmd.hasOption('r');
        this.nickPath = cmd.getOptionValue("nicks", null);
        this.nickPrefix = cmd.getOptionValue('p', "");

        this.nickGenerator = new NickGenerator(this.nickPath, this.nickPrefix, useRealNick);

        this.online = cmd.hasOption("o");
        this.hasGravity = cmd.hasOption("g");

        getAndPrintServerInfo(getInetAddr());

        if (isOnline()) {
            OnlineService onlineService = new OnlineService(this);
            this.authenticationService = onlineService.getOnlineAuthenticationService();
        } else {
            this.authenticationService = null;
        }

        // Load bots
        this.botLoader = new BotLoader(this);
        this.botLoader.spin();
        if (hasGravity()) {
            this.gravityTimer.startTimer();
        }

        // Load console reader
        ConsoleReader consoleReader = new ConsoleReader(this);
        consoleReader.start();
    }

    private Pair<String, Integer> parseAddress(String address) {
        int port = 25565;
        if (address.contains(":")) {
            String[] split = address.split(":", 2);
            address = split[0];
            port = Integer.parseInt(split[1]);
        } else {
            Record[] records;
            try {
                records = new Lookup("_minecraft._tcp." + address, Type.SRV).run();
            } catch (TextParseException e) {
                throw new RuntimeException(e);
            }
            if (records != null) {
                for (Record record : records) {
                    SRVRecord srv = (SRVRecord) record;
                    address = srv.getTarget().toString().replaceFirst("\\.$", "");
                    port = srv.getPort();
                }
            }
        }
        return new Pair<>(address, port);
    }

    private Pair<Integer, Integer> parseDelays(String[] delays) {
        int delayMin = Integer.parseInt(delays[0]);
        int delayMax = delayMin + 1;
        if (delays.length == 2) {
            delayMax = Integer.parseInt(delays[1]);
        }
        if (delayMax <= delayMin) {
            throw new IllegalArgumentException("delay max must not be equal or lower than delay min");
        }
        return new Pair<>(delayMin, delayMax);
    }

    private void getAndPrintServerInfo(InetSocketAddress inetAddr) {
        //print info
        Logger.info("Server Address:", inetAddr.getHostString());
        Logger.info("Server Port: " + inetAddr.getPort());
        Logger.info("Bot count: " + getBotCount());

        //get and print server info
        ServerInfo serverInfo = new ServerInfo(inetAddr);
        serverInfo.requestInfo();
        ServerStatusInfo statusInfo = serverInfo.getStatusInfo();
        if (statusInfo != null) {
            Logger.info(
                "Server version: "
                    + statusInfo.getVersionInfo().getVersionName()
                    + " (" + statusInfo.getVersionInfo().getProtocolVersion()
                    + ")"
            );
            Logger.info("Player Count: " + statusInfo.getPlayerInfo().getOnlinePlayers()
                + " / " + statusInfo.getPlayerInfo().getMaxPlayers());
            Logger.info();
        } else {
            Logger.warn("There was an error retrieving server status information. The server may be offline or running on a different version.");
        }
    }

    public void renewMainListener() {
        this.bots.get(0).registerMainListener();
    }

    @Override
    public void removeBot(Bot bot) {
        super.removeBot(bot);
        controlledBots.remove(bot);
        if (bot.hasMainListener()) {
            Logger.info("Bot with MainListener removed");
            this.isMainListenerMissing = true;
        }
        if (!this.bots.isEmpty()) {
            if (this.isMainListenerMissing && !isMinimal()) {
                Logger.info("Renewing MainListener");
                renewMainListener();
                this.isMainListenerMissing = false;
            }
        } else {
            if (this.botLoader.getTriedToConnect() == getBotCount()) {
                Logger.error("All bots disconnected, exiting");
                System.exit(0);
            }
        }
    }

    public String getPrompt() {
        int count = this.getControlledBots().size();
        if (count == 0) {
            return "ALL";
        } else if (count == 1) {
            //If controlling only one bot, return its nickname
            return getControlledBots().iterator().next().getNickname();
        } else {
            return count + " BOTS";
        }
    }

    public int getBotCount() {
        return this.botCount;
    }

    public void setBotCount(int botCount) {
        this.botCount = botCount;
    }

    public int getDelayMin() {
        return this.delayMin;
    }

    public int getDelayMax() {
        return this.delayMax;
    }

    public boolean isColoredChat() {
        return this.coloredChat;
    }

    public boolean isOnline() {
        return this.online;
    }

    @Nullable
    public ProxyGenerator getProxyGenerator() {
        return this.proxyGenerator;
    }

    public HashSet<Bot> getControlledBots() {
        return this.controlledBots;
    }

    public AuthenticationService getAuthenticationService() {
        return this.authenticationService;
    }

    public boolean isMainListenerMissing() {
        return this.isMainListenerMissing;
    }

    public void setMainListenerMissing(boolean missing) {
        this.isMainListenerMissing = missing;
    }

}
