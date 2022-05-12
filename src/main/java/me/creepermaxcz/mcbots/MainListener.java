package me.creepermaxcz.mcbots;

import com.github.steveice10.mc.protocol.packet.ingame.clientbound.ClientboundChatPacket;
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
        if(packet instanceof ClientboundChatPacket) {
            Component message = ((ClientboundChatPacket) packet).getMessage();
            //Log.chat(message.toString());
            if (message instanceof TextComponent) {
                TextComponent msg = (TextComponent) message;
                Log.chat(Utils.getFullText(msg, Main.coloredChat));
            }
            if (message instanceof TranslatableComponent) {
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