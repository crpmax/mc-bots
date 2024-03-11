package com.shanebeestudios.mcbots.standalone;

import com.shanebeestudios.mcbots.bot.Bot;
import com.shanebeestudios.mcbots.api.util.logging.Logger;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.stream.Collectors;

public class ConsoleReader {

    private final StandaloneBotManager standaloneLoader;

    public ConsoleReader(StandaloneBotManager standaloneLoader) {
        this.standaloneLoader = standaloneLoader;
    }

    @SuppressWarnings("BusyWait")
    public void start() {
        Terminal terminal;
        try {
            terminal = TerminalBuilder.builder().build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        LineReader lineReader = LineReaderBuilder.builder()
            .terminal(terminal)
            .parser(new org.jline.reader.impl.DefaultParser())
            .build();

        while (true) {
            HashSet<Bot> controlledBots = this.standaloneLoader.getControlledBots();
            ArrayList<Bot> bots = this.standaloneLoader.getBots();
            String line;
            try {
                line = lineReader.readLine();
            } catch (UserInterruptException e) {
                System.exit(0);
                break;
            }
            if (line.isEmpty()) {
                System.out.print(this.standaloneLoader.getPrompt() + "> ");
                continue;
            }

            if (line.startsWith("!") || line.startsWith(".")) {
                String command = line.substring(1);
                String[] split = command.split(" ");
                String commandName = split[0];

                // Bot control selection
                if (commandName.equalsIgnoreCase("control") || commandName.equalsIgnoreCase("ctrl")) {
                    if (split.length >= 2) {

                        controlledBots.clear();
                        int newBotCount = 0;

                        // Search for all bot names, from index 1 - skip command name
                        for (int i = 1; i < split.length; i++) {
                            String searchedName = split[i];
                            Bot bot = this.standaloneLoader.findBotByName(searchedName);

                            if (bot != null) {
                                controlledBots.add(bot);
                                newBotCount++;
                            } else {
                                Logger.warn("Bot not found: " + searchedName);
                            }
                        }


                        if (newBotCount > 0) {
                            // Join bot nicknames
                            String botNames = controlledBots
                                .stream()
                                .map(Bot::getNickname)
                                .collect(Collectors.joining(", "));

                            if (newBotCount == 1) {
                                Logger.info("Now controlling 1 bot: " + botNames);
                            } else {
                                Logger.info("Now controlling " + newBotCount + " bots: " + botNames);
                            }

                        } else {
                            Logger.warn("No bots found.");
                        }


                    } else {
                        // If no bot names are supplied, remove all bots
                        controlledBots.clear();
                        Logger.info("No bots selected - now controlling all bots.");
                    }
                }

                // List command
                else if (commandName.equalsIgnoreCase("list") || commandName.equalsIgnoreCase("ls")) {
                    Logger.info("There are " + bots.size() + " bots connected:");
                    for (Bot bot : bots) {
                        Logger.info(bot.getNickname(), bot.hasMainListener() ? "[MainListener]" : "");
                    }
                }

                // Leave command
                else if (commandName.equalsIgnoreCase("leave") || commandName.equalsIgnoreCase("exit")) {
                    int limit = -1;
                    if (split.length >= 2) {
                        try {
                            limit = Integer.parseInt(split[1]);
                        } catch (NumberFormatException e) {
                            limit = 0;
                            Logger.warn("Invalid limit.");
                        }
                    }

                    int i = 0;
                    if (!controlledBots.isEmpty()) {
                        Logger.info("Disconnecting controlled bots.");
                        for (Bot bot : controlledBots) {
                            if (i++ == limit) {
                                break;
                            }

                            bot.disconnect();
                        }
                    } else {
                        Logger.info("Disconnecting all bots.");
                        for (Bot bot : bots) {
                            if (i++ == limit) {
                                break;
                            }

                            bot.disconnect();
                        }
                    }
                } else {
                    Logger.warn("Invalid command");
                }
            } else if (!controlledBots.isEmpty()) {
                controlledBots.forEach(bot -> bot.sendChat(line));
            } else {
                bots.forEach(bot -> bot.sendChat(line));
            }

            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
