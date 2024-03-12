package com.shanebeestudios.mcbots.plugin.bot;

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

import java.util.Timer;
import java.util.TimerTask;

@SuppressWarnings("DuplicatedCode")
public class PluginSessionListener extends BaseSessionListener {

    @Override
    public void packetReceived(Session session, Packet packet) {
        if (packet instanceof ClientboundGameEventPacket gameEventPacket) {
            if (gameEventPacket.getNotification() == GameEvent.ENABLE_RESPAWN_SCREEN) {
                RespawnScreenValue respawnScreenValue = (RespawnScreenValue) gameEventPacket.getValue();
                if (respawnScreenValue == RespawnScreenValue.IMMEDIATE_RESPAWN) {
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

            // Might implement this later
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
                new Timer().schedule(
                    new TimerTask() {
                        @Override
                        public void run() {
                            PluginSessionListener.this.client.send(new ServerboundClientCommandPacket(ClientCommand.RESPAWN));
                        }
                    }, this.autoRespawnDelay);
            }
        }
    }

    @Override
    public void disconnected(DisconnectedEvent event) {
        this.bot.setConnected(false);
        this.botManager.logBotDisconnected(this.bot.getNickname());
        this.botManager.removeBot(this.bot);
        Thread.currentThread().interrupt();
    }

}
