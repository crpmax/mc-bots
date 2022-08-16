package me.creepermaxcz.mcbots;

import com.github.steveice10.mc.protocol.packet.ingame.clientbound.ClientboundPlayerChatPacket;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.ClientboundSystemChatPacket;
import com.github.steveice10.packetlib.Session;
import com.github.steveice10.packetlib.event.session.*;
import com.github.steveice10.packetlib.packet.Packet;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;

public class MainListener implements SessionListener {

    public MainListener(String nickname) {
        Log.info("MainListener registered for: " + nickname);
    }

    @Override
    public void packetReceived(Session session, Packet packet) {
        // From 1.19.1 (as well as 1.19), the class ClientboundChatPacket was removed.
        // Instead, they use ClientboundPlayerChatPacket and ClientboundSystemChatPacket for taking care of chat packets.
        Component message = null;
        Component sender;
        boolean chatPrintedOut = false;

        if (packet instanceof ClientboundPlayerChatPacket) {
            ClientboundPlayerChatPacket clientboundPlayerChatPacket = ((ClientboundPlayerChatPacket) packet);
            message = clientboundPlayerChatPacket.getUnsignedContent();
            sender = clientboundPlayerChatPacket.getName();

            // Sometimes the message's body gets null.
            // For example, some commands like /say makes the message content as null.
            // However, the message exists as in getMessagePlain(), thus can retrieve message using the method.
            if (message == null) {  // When this message was null.
                Log.chat(Utils.getFullText((TextComponent) sender, clientboundPlayerChatPacket.getMessagePlain(), Main.coloredChat));
            } else { // When message exists.
                Log.chat(Utils.getFullText((TextComponent) sender, (TextComponent) message, Main.coloredChat));
            }
            chatPrintedOut = true;

        } else if (packet instanceof ClientboundSystemChatPacket) { // When this was SystemChat packet.
            message = ((ClientboundSystemChatPacket) packet).getContent();
        }

        // For output of commands, this is the case where this program prints out the message to user.
        if (message instanceof TextComponent && !chatPrintedOut) {
            TextComponent msg = (TextComponent) message;
            Log.chat(Utils.getFullText(msg, Main.coloredChat));
        }

        if (message instanceof TranslatableComponent) {
            TranslatableComponent msg = (TranslatableComponent) message;
            Log.chat("[T]", Utils.translate(msg));
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
