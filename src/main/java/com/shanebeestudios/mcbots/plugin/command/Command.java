package com.shanebeestudios.mcbots.plugin.command;

import com.shanebeestudios.mcbots.api.util.logging.PluginLogger;
import com.shanebeestudios.mcbots.api.bot.Bot;
import com.shanebeestudios.mcbots.plugin.McBotPlugin;
import com.shanebeestudios.mcbots.plugin.bot.PluginBotManager;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.Collection;

public class Command {

    private final McBotPlugin plugin;
    private final PluginBotManager botManager;
    private final BukkitScheduler scheduler;

    public Command(McBotPlugin plugin) {
        this.plugin = plugin;
        this.botManager = plugin.getPluginBotManager();
        this.scheduler = Bukkit.getScheduler();
        registerCommand();
    }

    @SuppressWarnings("unchecked")
    public void registerCommand() {
        CommandTree command = new CommandTree("mcbots")
            // Create a bot
            .then(new LiteralArgument("create")
                .then(new LiteralArgument("named")
                    .then(new StringArgument("name")
                        .executes((sender, args) -> {
                            String name = (String) args.get("name");
                            Bot bot = this.botManager.createBot(name);
                            if (bot != null) {
                                PluginLogger.logToSender(sender, "Created new bot '&b" + bot.getNickname() + "&7'");
                            } else {
                                PluginLogger.logToSender(sender, "&cFailed to create bot '&b" + name + "&7'");
                            }
                        })))
                .then(new LiteralArgument("random")
                    .then(new IntegerArgument("amount")
                        .setOptional(true)
                        .then(new IntegerArgument("delay-ticks")
                            .setOptional(true)
                            .executes((sender, args) -> {
                                int amount = (int) args.getOrDefault("amount", 1);
                                long delay = (int) args.getOrDefault("delay-ticks", 20);
                                for (int i = 0; i < amount; i++) {
                                    this.scheduler.runTaskLater(this.plugin, () -> {
                                        Bot bot = this.botManager.createBot(null);
                                        if (bot != null) {
                                            PluginLogger.logToSender(sender, "Created new bot '&b" + bot.getNickname() + "&7'");
                                        } else {
                                            PluginLogger.logToSender(sender, "&cFailed to create random bot!");
                                        }
                                    }, delay * i);
                                }
                            })))))

            // Remove a bot
            .then(new LiteralArgument("remove")
                .then(new EntitySelectorArgument.ManyPlayers("players")
                    .executes((sender, args) -> {
                        Collection<Entity> players = (Collection<Entity>) args.get("players");
                        assert players != null;
                        players.forEach(player -> {
                            Bot bot = this.botManager.findBotByName(player.getName());
                            if (bot != null) {
                                this.botManager.disconnectBot(bot);
                                PluginLogger.logToSender(sender, "Removed bot &7'" + bot.getNickname() + "&7'");
                            }
                        });
                    })))

            // Make a bot send a chat message or command
            .then(new LiteralArgument("chat")
                .then(new EntitySelectorArgument.ManyPlayers("players")
                    .then(new GreedyStringArgument("message")
                        .executes((sender, args) -> {
                            String message = (String) args.get("message");
                            Collection<Entity> players = (Collection<Entity>) args.get("players");
                            assert message != null;
                            assert players != null;
                            players.forEach(player -> {
                                Bot bot = this.botManager.findBotByName(player.getName());
                                if (bot != null) bot.sendChat(message);
                            });
                        }))));

        command.register();
    }

}
