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
        Component sender = null;
        boolean isMessageNull = false; // For some cases when message is null.

        if (packet instanceof ClientboundPlayerChatPacket) {
            ClientboundPlayerChatPacket clientboundPlayerChatPacket = ((ClientboundPlayerChatPacket) packet);
            message = clientboundPlayerChatPacket.getUnsignedContent();
            sender = clientboundPlayerChatPacket.getName();
            isMessageNull = message == null;
        } else if (packet instanceof ClientboundSystemChatPacket) {
            message = ((ClientboundSystemChatPacket) packet).getContent();
        }

        // For some commands (like /say), ClientboundPlayerChatPacket.getUnsignedContent() is null.
        // However for those cases, the message is still valid in ClientboundPlayerChatPacket.getMessagePlain()
        // Thus, this if condition will figure out if this case was the case and prints out message from the packet.
        if (isMessageNull) { // When this message was null.
            ClientboundPlayerChatPacket clientboundPlayerChatPacket = ((ClientboundPlayerChatPacket) packet);
            Log.chat(Utils.getFullText((TextComponent) sender, clientboundPlayerChatPacket.getMessagePlain(), Main.coloredChat));
        } else {  // If this was normal case when unsigned content was not null.
            if (message instanceof TextComponent) { // For normal chat packets.
                // Log.chat(Utils.getFullText((TextComponent) message, Main.coloredChat)); // Use this for only messages.
                Log.chat(Utils.getFullText((TextComponent) sender, (TextComponent) message, Main.coloredChat)); // Use this for messages and sender's username.
            }

            if (message instanceof TranslatableComponent) { // For system chat packets (such as user joining server).
                TranslatableComponent msg = (TranslatableComponent) message;
                Log.chat("[T]", Utils.translate(msg));
            }
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
