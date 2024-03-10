package com.shanebeestudios.mcbots.listener;

import com.github.steveice10.mc.protocol.packet.ingame.clientbound.ClientboundPlayerChatPacket;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.ClientboundSystemChatPacket;
import com.github.steveice10.packetlib.Session;
import com.github.steveice10.packetlib.event.session.ConnectedEvent;
import com.github.steveice10.packetlib.event.session.DisconnectedEvent;
import com.github.steveice10.packetlib.event.session.DisconnectingEvent;
import com.github.steveice10.packetlib.event.session.PacketErrorEvent;
import com.github.steveice10.packetlib.event.session.PacketSendingEvent;
import com.github.steveice10.packetlib.event.session.SessionListener;
import com.github.steveice10.packetlib.packet.Packet;
import com.shanebeestudios.mcbots.api.util.Logger;
import com.shanebeestudios.mcbots.standalone.StandaloneInfo;
import com.shanebeestudios.mcbots.bot.Bot;
import com.shanebeestudios.mcbots.api.util.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;

public class MessageListener implements SessionListener {

    private final boolean coloredChat;

    public MessageListener(Bot bot) {
        this.coloredChat = !(bot.getLoader().getInfoBase() instanceof StandaloneInfo standaloneInfo) || standaloneInfo.isColoredChat();
        Logger.info("MessageListener registered for: " + bot.getNickname());
    }

    @Override
    public void packetReceived(Session session, Packet packet) {
        // From 1.19.1 (as well as 1.19), the class ClientboundChatPacket was removed.
        // Instead, they use ClientboundPlayerChatPacket and ClientboundSystemChatPacket for taking care of chat packets.
        Component message = null;
        Component sender;
        boolean chatPrintedOut = false;

        if (packet instanceof ClientboundPlayerChatPacket playerChatPacket) {
            message = playerChatPacket.getUnsignedContent();
            sender = playerChatPacket.getName();

            // Sometimes the message's body gets null.
            // For example, some commands like /say makes the message content as null.
            // However, the message exists as in getMessagePlain(), thus can retrieve message using the method.
            if (message == null) {
                Logger.chat(Utils.getFullText((TextComponent) sender, Component.text(playerChatPacket.getContent()), this.coloredChat));
            } else {
                Logger.chat(Utils.getFullText((TextComponent) sender, (TextComponent) message, this.coloredChat));
            }
            chatPrintedOut = true;

        } else if (packet instanceof ClientboundSystemChatPacket systemChatPacket) { // When this was SystemChat packet.
            if (systemChatPacket.isOverlay()) return; // Actionbar
            message = systemChatPacket.getContent();
        }

        // For output of commands, this is the case where this program prints out the message to user.
        if (message instanceof TextComponent textComponent && !chatPrintedOut) {
            Logger.chat(Utils.getFullText(textComponent, this.coloredChat));
        }

        if (message instanceof TranslatableComponent translatableComponent) {
            Logger.chat("[T]", Utils.translate(translatableComponent));
        }
    }

    @Override
    public void packetSending(PacketSendingEvent event) {
    }

    @Override
    public void packetSent(Session session, Packet packet) {
    }

    @Override
    public void packetError(PacketErrorEvent event) {
    }

    @Override
    public void connected(ConnectedEvent event) {
    }

    @Override
    public void disconnecting(DisconnectingEvent event) {
    }

    @Override
    public void disconnected(DisconnectedEvent event) {
    }

}
