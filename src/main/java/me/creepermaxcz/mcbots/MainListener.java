package me.creepermaxcz.mcbots;

import com.github.steveice10.mc.protocol.data.message.Message;
import com.github.steveice10.mc.protocol.data.message.TextMessage;
import com.github.steveice10.mc.protocol.data.message.TranslationMessage;
import com.github.steveice10.mc.protocol.packet.ingame.server.ServerChatPacket;
import com.github.steveice10.packetlib.event.session.*;

public class MainListener implements SessionListener {

    public MainListener(String nickname) {
        Log.info("MainListener registered for: " + nickname);
    }

    @Override
    public void packetReceived(PacketReceivedEvent event) {
        if(event.getPacket() instanceof ServerChatPacket) {
            Message message = event.<ServerChatPacket>getPacket().getMessage();
            if (message instanceof TextMessage) {
                if (Main.coloredChat) {
                    Log.chat(Utils.getFormattedFullText(message));
                } else {
                    Log.chat(Utils.getFullText((TextMessage) message));
                }

            } else if (message instanceof TranslationMessage) {
                Log.chat("[T]", Utils.translate((TranslationMessage) message));
            }
        }
    }

    @Override
    public void packetSending(PacketSendingEvent event) {

    }

    @Override
    public void packetSent(PacketSentEvent event) {

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
