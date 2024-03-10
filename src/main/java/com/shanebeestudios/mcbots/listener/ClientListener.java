package com.shanebeestudios.mcbots.listener;

import com.github.steveice10.mc.protocol.data.UnexpectedEncryptionException;
import com.github.steveice10.mc.protocol.data.game.ClientCommand;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.ClientboundLoginPacket;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.entity.player.ClientboundPlayerCombatKillPacket;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.entity.player.ClientboundPlayerPositionPacket;
import com.github.steveice10.mc.protocol.packet.ingame.serverbound.ServerboundClientCommandPacket;
import com.github.steveice10.mc.protocol.packet.ingame.serverbound.level.ServerboundAcceptTeleportationPacket;
import com.github.steveice10.packetlib.Session;
import com.github.steveice10.packetlib.event.session.DisconnectedEvent;
import com.github.steveice10.packetlib.event.session.SessionAdapter;
import com.github.steveice10.packetlib.packet.Packet;
import com.shanebeestudios.mcbots.api.Loader;
import com.shanebeestudios.mcbots.bot.Bot;
import com.shanebeestudios.mcbots.api.util.Logger;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("CallToPrintStackTrace")
public class ClientListener extends SessionAdapter {

    private final Bot bot;
    private final Session client;
    private final Loader loader;
    private final ArrayList<String> joinMessages;
    private final int autoRespawnDelay;

    public ClientListener(Bot bot) {
        this.bot = bot;
        this.client = bot.getClient();
        this.loader = bot.getLoader();
        this.joinMessages = this.loader.getInfoBase().getJoinMessages();
        this.autoRespawnDelay = this.loader.getInfoBase().getAutoRespawnDelay();
    }

    @Override
    public void packetReceived(Session session, Packet packet) {
        if (packet instanceof ClientboundLoginPacket) {
            this.bot.setConnected(true);
            Logger.info(this.bot.getNickname() + " connected");

            if (!this.joinMessages.isEmpty()) {
                for (String msg : joinMessages) {
                    this.bot.sendChat(msg);

                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ignored) {
                    }
                }
            }
        } else if (packet instanceof ClientboundPlayerPositionPacket positionPacket) {
            this.bot.setLastPosition(positionPacket.getX(), positionPacket.getY(), positionPacket.getZ());
            this.client.send(new ServerboundAcceptTeleportationPacket(positionPacket.getTeleportId()));
        } else if (packet instanceof ClientboundPlayerCombatKillPacket) {
            if (this.autoRespawnDelay >= 0) {
                Logger.info("Bot " + this.bot.getNickname() + " died. Respawning in " + this.autoRespawnDelay + " ms.");
                new Timer().schedule(
                    new TimerTask() {
                        @Override
                        public void run() {
                            ClientListener.this.client.send(new ServerboundClientCommandPacket(ClientCommand.RESPAWN));
                        }
                    }, this.autoRespawnDelay);
            }
        }
    }

    @Override
    public void disconnected(DisconnectedEvent event) {
        this.bot.setConnected(false);
        Logger.info(this.bot.getNickname() + " disconnected");

        // Do not write disconnect reason if disconnected by command
        if (!this.bot.isManualDisconnecting()) {
            // Fix broken reason string by finding the content with regex
            Pattern pattern = Pattern.compile("content=\"(.*?)\"");
            Matcher matcher = pattern.matcher(String.valueOf(event.getReason()));

            StringBuilder reason = new StringBuilder();
            while (matcher.find()) {
                reason.append(matcher.group(1));
            }

            Logger.info(" -> " + reason);

            if (event.getCause() != null) {
                event.getCause().printStackTrace();

                if (event.getCause() instanceof UnexpectedEncryptionException) {
                    Logger.warn("Server is running in online (premium) mode. Please use the -o option to use online mode bot.");
                    System.exit(1);
                }
            }
            Logger.info();
        }

        this.loader.removeBot(this.bot);

        Thread.currentThread().interrupt();
    }

}
