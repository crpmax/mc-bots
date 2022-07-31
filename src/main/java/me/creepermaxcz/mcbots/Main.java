package me.creepermaxcz.mcbots;

import com.github.steveice10.mc.protocol.data.status.ServerStatusInfo;
import com.github.steveice10.packetlib.ProxyInfo;
import org.apache.commons.cli.*;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.SRVRecord;
import org.xbill.DNS.Type;

import java.io.*;
import java.net.*;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;


public class Main {

    public static boolean coloredChat = true;
    static ArrayList<Bot> bots = new ArrayList<>();
    private static int triedToConnect;
    private static int botCount;
    private static boolean isMainListenerMissing = true;
    private static final SecureRandom random = new SecureRandom();
    private static int delayMin = 4000;
    private static int delayMax = 5000;
    private static boolean minimal = false;
    private static boolean mostMinimal = false;
    public static String joinMessage;

    private static boolean useProxies = false;
    private static final ArrayList<InetSocketAddress> proxies = new ArrayList<>();
    private static int proxyIndex = 0;
    private static int proxyCount = 0;
    private static ProxyInfo.Type proxyType;

    private static Timer timer = new Timer();

    public static void main(String[] args) throws Exception {

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
        options.addOption("j", "join-msg", true, "join message / command");

        options.addOption("l", "proxy-list", true, "Path or URL to proxy list file with proxy:port on every line");
        options.addOption("t", "proxy-type", true, "Proxy type: SOCKS4 or SOCKS5");

        options.addOption(null, "nicks", true, "Path to nicks file with nick on every line");

        options.addOption("g", "gravity", false, "Try to simulate gravity by falling down");

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("mcbots", e.getMessage(), options, "\nhttps://github.com/crpmax/mc-bots",true);
            System.exit(1);
        }

        if (cmd.hasOption('t') && cmd.hasOption('l')) {
            String typeStr = cmd.getOptionValue('t').toUpperCase();

            //get proxy type
            try {
                proxyType = ProxyInfo.Type.valueOf(typeStr);
            } catch (IllegalArgumentException e) {
                Log.error("Inavlid proxy type, use SOCKS4 or SOCKS5.");
                System.exit(1);
            }

            String proxyPath = cmd.getOptionValue("l");

            //read proxy list file
            try {

                try {
                    //try to read specified path as URL
                    URL url = new URL(proxyPath);

                    BufferedReader read = new BufferedReader(
                            new InputStreamReader(url.openStream()));

                    Log.info("Reading proxies from URL");
                    String line;
                    while ((line = read.readLine()) != null) {
                        try {
                            String[] parts = line.trim().split(":");
                            if (parts.length == 2) {
                                int port = Integer.parseInt(parts[1]);
                                proxies.add(new InetSocketAddress(parts[0], port));
                                proxyCount++;
                            }
                        }
                        catch (Exception ignored) { }
                    }
                    read.close();

                } catch (MalformedURLException e) {
                    Log.info("Specified proxy file is not a URL, trying to read file");

                    Scanner scanner = new Scanner(new File(proxyPath));
                    while (scanner.hasNextLine()) {
                        try {
                            String[] parts = scanner.nextLine().trim().split(":");
                            if (parts.length == 2) {
                                int port = Integer.parseInt(parts[1]);
                                proxies.add(new InetSocketAddress(parts[0], port));
                                proxyCount++;
                            }
                        }
                        catch (Exception ignored) { }
                    }
                    scanner.close();
                }
            } catch (FileNotFoundException e) {
                Log.error("Invalid proxy list file path.");
                System.exit(1);
            }

            if (proxyCount > 0) {
                useProxies = true;
                Log.info("Loaded " + proxyCount + " valid proxies");
            } else {
                Log.error("No valid proxies loaded");
                System.exit(1);
            }

        }


        botCount = Integer.parseInt(cmd.getOptionValue('c', "1"));

        minimal = cmd.hasOption('m');
        if (cmd.hasOption('x')) {
           minimal = mostMinimal = true;
        }


        if (cmd.hasOption('d')) {
            String[] delays = cmd.getOptionValues('d');
            delayMin = Integer.parseInt(delays[0]);
            delayMax = delayMin + 1;
            if (delays.length == 2) {
                delayMax = Integer.parseInt(delays[1]);
            }
            if (delayMax <= delayMin) {
                throw new IllegalArgumentException("delay max must not be equal or lower than delay min");
            }
        }


        String address = cmd.getOptionValue('s');
        coloredChat = !cmd.hasOption('n');

        joinMessage = cmd.getOptionValue('j');

        int port = 25565;
        if (address.contains(":")) {
            String[] split = address.split(":", 2);
            address = split[0];
            port = Integer.parseInt(split[1]);
        } else {
            Record[] records = new Lookup("_minecraft._tcp." + address, Type.SRV).run();
            if (records != null) {
                for (Record record : records) {
                    SRVRecord srv = (SRVRecord) record;
                    address = srv.getTarget().toString().replaceFirst("\\.$", "");
                    port = srv.getPort();
                }
            }
        }

        boolean realNicknames = cmd.hasOption('r');

        NickGenerator nickGen = new NickGenerator();

        if (cmd.hasOption("nicks")) {
            Log.info("Loading nicknames from specified file");
            int nicksCount = nickGen.loadFromFile(cmd.getOptionValue("nicks"));

            if (nicksCount == 0) {
                Log.error("No valid nicknames loaded");
                System.exit(1);
            }

            if (nicksCount < botCount) {
                Log.warn("Nickname count is lower than bot count!");
                Thread.sleep(3000);
            }
        } else {
            nickGen.setReal(realNicknames);
        }

        nickGen.setPrefix(cmd.getOptionValue('p', ""));

        InetSocketAddress inetAddr = new InetSocketAddress(
            InetAddress.getByName(address).getHostAddress(),
            port
        );

        //print info
        Log.info("IP:", inetAddr.getHostString());
        Log.info("Port: " + inetAddr.getPort());
        Log.info("Bot count: " + botCount);

        //get and print server info
        ServerInfo serverInfo = new ServerInfo(inetAddr);
        serverInfo.requestInfo();
        ServerStatusInfo statusInfo = serverInfo.getStatusInfo();
        Log.info(
                "Server version: "
                + statusInfo.getVersionInfo().getVersionName()
                + " (" + statusInfo.getVersionInfo().getProtocolVersion()
                + ")"
        );
        Log.info("Player Count: " + statusInfo.getPlayerInfo().getOnlinePlayers()
                + " / " + statusInfo.getPlayerInfo().getMaxPlayers());
        Log.info();

        new Thread(() -> {
            for (int i = 0; i < botCount; i++) {
                try {
                    ProxyInfo proxyInfo = null;
                    if (useProxies) {
                        InetSocketAddress proxySocket = proxies.get(proxyIndex);

                        if (!minimal) {
                            Log.info(
                                    "Using proxy: (" + proxyIndex + ")",
                                    proxySocket.getHostString() + ":" + proxySocket.getPort()
                            );
                        }

                        proxyInfo = new ProxyInfo(
                                proxyType,
                                proxySocket
                        );

                        //increment or reset current proxy index
                        if (proxyIndex < (proxyCount - 1)) {
                            proxyIndex++;
                        } else {
                            proxyIndex = 0;
                        }

                    }

                    Bot bot = new Bot(
                            nickGen.nextNick(),
                            inetAddr,
                            proxyInfo
                    );
                    bot.start();

                    if (!mostMinimal) bots.add(bot);

                    triedToConnect++;

                    if (isMainListenerMissing && !isMinimal()) {
                        isMainListenerMissing = false;
                        bot.registerMainListener();
                    }

                    if (i < botCount - 1) {
                        long delay = getRandomDelay();
                        Log.info("Waiting", delay + "", "ms");
                        Thread.sleep(delay);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }).start();

        //gravity timer
        if (cmd.hasOption("g")) {
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    bots.forEach(Bot::fallDown);
                }
            }, 1000L, 500L);
        }


        Scanner scanner = new Scanner(System.in);

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line.isEmpty()) continue;

            bots.forEach(bot -> bot.sendChat(line));

            Thread.sleep(50);
        }
    }

    public static synchronized void renewMainListener() {
        bots.get(0).registerMainListener();
    }

    public static synchronized void removeBot(Bot bot) {
        bots.remove(bot);
        if (bot.hasMainListener()) {
            Log.info("Bot with MainListener removed");
            isMainListenerMissing = true;
        }
        if (bots.size() > 0) {
            if (isMainListenerMissing && !isMinimal()) {
                Log.info("Renewing MainListener");
                renewMainListener();
                isMainListenerMissing = false;
            }
        } else {
            if (triedToConnect == botCount) {
                Log.error("All bots disconnected, exiting");
                System.exit(0);
            }
        }
        bot = null;
    }

    public static long getRandomDelay() {
        return random.nextInt(delayMax - delayMin) + delayMin;
    }

    public static boolean isMinimal() {
        return minimal;
    }
}
