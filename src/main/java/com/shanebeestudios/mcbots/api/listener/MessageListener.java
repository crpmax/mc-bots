package com.shanebeestudios.mcbots.api.listener;

import com.github.steveice10.mc.protocol.packet.ingame.clientbound.ClientboundPlayerChatPacket;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.ClientboundSystemChatPacket;
import com.github.steveice10.packetlib.Session;
import com.github.steveice10.packetlib.event.session.SessionAdapter;
import com.github.steveice10.packetlib.packet.Packet;
import com.shanebeestudios.mcbots.api.util.Utils;
import com.shanebeestudios.mcbots.api.util.logging.Logger;
import com.shanebeestudios.mcbots.api.bot.Bot;
import com.shanebeestudios.mcbots.standalone.bot.StandaloneBotManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;

public class MessageListener extends SessionAdapter {

    private final boolean coloredChat;

    public MessageListener(Bot bot) {
        this.coloredChat = !(bot.getBotManager() instanceof StandaloneBotManager botManager) || botManager.isColoredChat();
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

}