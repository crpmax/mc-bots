package com.shanebeestudios.mcbots.standalone;

import com.shanebeestudios.mcbots.api.Info;
import com.shanebeestudios.mcbots.api.generator.ProxyGenerator;
import com.shanebeestudios.mcbots.api.util.Pair;
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

public class StandaloneInfo extends Info {

    private int botCount;
    private final int delayMin;
    private final int delayMax;
    private final boolean coloredChat;
    private final boolean online;

    private ProxyGenerator proxyGenerator;

    public StandaloneInfo(Options options, String[] args) {
        super();
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("mcbots", e.getMessage(), options, "\nhttps://github.com/crpmax/mc-bots", true);
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
        this.address = addressPort.getFirst();
        this.port = addressPort.getSecond();

        this.coloredChat = !cmd.hasOption('n');

        if (cmd.hasOption('j')) {
            // Split messages by &&, trim and append to arraylist
            String[] messages = cmd.getOptionValue('j').split("&&");
            for (String msg : messages) {
                this.joinMessages.add(msg.trim());
            }
        }

        this.useRealNicknames = cmd.hasOption('r');
        this.nickPath = cmd.getOptionValue("nicks", null);
        this.nickPrefix = cmd.getOptionValue('p', "");

        this.online = cmd.hasOption("o");
        this.hasGravity = cmd.hasOption("g");
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

}
