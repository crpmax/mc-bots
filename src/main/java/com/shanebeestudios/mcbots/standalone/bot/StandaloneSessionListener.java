package com.shanebeestudios.mcbots.standalone.bot;

import com.github.steveice10.mc.protocol.data.UnexpectedEncryptionException;
import com.github.steveice10.mc.protocol.data.game.ClientCommand;
import com.github.steveice10.mc.protocol.data.game.level.notify.GameEvent;
import com.github.steveice10.mc.protocol.data.game.level.notify.RespawnScreenValue;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.ClientboundLoginPacket;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.entity.player.ClientboundPlayerCombatKillPacket;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.entity.player.ClientboundPlayerPositionPacket;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.level.ClientboundGameEventPacket;
import com.github.steveice10.mc.protocol.packet.ingame.serverbound.ServerboundClientCommandPacket;
import com.github.steveice10.mc.protocol.packet.ingame.serverbound.level.ServerboundAcceptTeleportationPacket;
import com.github.steveice10.packetlib.Session;
import com.github.steveice10.packetlib.event.session.DisconnectedEvent;
import com.github.steveice10.packetlib.packet.Packet;
import com.shanebeestudios.mcbots.api.listener.BaseSessionListener;
import com.shanebeestudios.mcbots.api.util.Utils;
import com.shanebeestudios.mcbots.api.util.logging.Logger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;

import java.util.Timer;
import java.util.TimerTask;

@SuppressWarnings({"CallToPrintStackTrace", "DuplicatedCode"})
public class StandaloneSessionListener extends BaseSessionListener {

    @Override
    public void packetReceived(Session session, Packet packet) {
        if (packet instanceof ClientboundGameEventPacket gameEventPacket) {
            if (gameEventPacket.getNotification() == GameEvent.ENABLE_RESPAWN_SCREEN) {
                RespawnScreenValue value = (RespawnScreenValue) gameEventPacket.getValue();
                if (value == RespawnScreenValue.IMMEDIATE_RESPAWN) {
                    this.autoRespawnDelay = 0;
                } else {
                    this.autoRespawnDelay = this.botManager.getAutoRespawnDelay();
                }
            }
        } else if (packet instanceof ClientboundLoginPacket loginPacket) {
            if (!loginPacket.isEnableRespawnScreen()) {
                this.autoRespawnDelay = 0;
            }
            this.bot.setConnected(true);
            Logger.info(this.bot.getNickname() + " connected");

            if (!this.joinMessages.isEmpty()) {
                for (String msg : this.joinMessages) {
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
                            StandaloneSessionListener.this.client.send(new ServerboundClientCommandPacket(ClientCommand.RESPAWN));
                        }
                    }, this.autoRespawnDelay);
            }
        }
    }

    @Override
    public void disconnected(DisconnectedEvent event) {
        this.bot.setConnected(false);
        this.botManager.logBotDisconnected(this.bot.getNickname());

        // Do not write disconnect reason if disconnected by command
        if (!this.bot.isManualDisconnecting()) {
            Component reason = event.getReason();
            if (reason != null) {
                String reasonText = Utils.getFullText((TextComponent) reason, true);
                Logger.info("Reason: " + reasonText);
            }

            Throwable cause = event.getCause();
            if (cause != null) {
                cause.printStackTrace();
                if (cause instanceof UnexpectedEncryptionException) {
                    Logger.warn("Server is running in online (premium) mode. Please use the -o option to use online mode bot.");
                    System.exit(1);
                }
            }
            Logger.info();
        }

        this.botManager.removeBot(this.bot);
        Thread.currentThread().interrupt();
    }

}
