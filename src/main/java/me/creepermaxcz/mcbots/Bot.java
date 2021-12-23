package me.creepermaxcz.mcbots;

import com.github.steveice10.mc.protocol.MinecraftProtocol;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.ClientboundLoginPacket;
import com.github.steveice10.mc.protocol.packet.ingame.serverbound.ServerboundChatPacket;
import com.github.steveice10.packetlib.ProxyInfo;
import com.github.steveice10.packetlib.Session;
import com.github.steveice10.packetlib.event.session.DisconnectedEvent;
import com.github.steveice10.packetlib.event.session.SessionAdapter;
import com.github.steveice10.packetlib.packet.Packet;
import com.github.steveice10.packetlib.tcp.TcpClientSession;

import java.net.InetSocketAddress;

public class Bot extends Thread {

    MinecraftProtocol protocol = null;

    private String nickname;
    private ProxyInfo proxy;
    private InetSocketAddress address;
    private Session client;
    private boolean hasMainListener;

    public Bot(String nickname, InetSocketAddress address, ProxyInfo proxy) {
        this.nickname = nickname;
        this.address = address;
        this.proxy = proxy;

        Log.info("Creating bot", nickname);
        protocol = new MinecraftProtocol(nickname);
        client = new TcpClientSession(address.getHostString(), address.getPort(), protocol);
    }

    @Override
    public void run() {

        if (!Main.isMinimal()) {
            client.addListener(new SessionAdapter() {

                @Override
                public void packetReceived(Session session, Packet packet) {
                    if (packet instanceof ClientboundLoginPacket) {
                        Log.info(nickname + " connected");
                        if (Main.joinMessage != null) {
                            sendChat(Main.joinMessage);
                        }
                    }
                }

                @Override
                public void disconnected(DisconnectedEvent event) {
                    Log.info();
                    Log.info(nickname + " disconnected");
                    Log.info(" -> " + event.getReason());
                    if(event.getCause() != null) {
                        event.getCause().printStackTrace();
                    }
                    Log.info();
                    Main.removeBot(Bot.this);
                    Thread.currentThread().interrupt();
                }
            });
        }
        client.connect();
    }

    public void sendChat(String text) {
        client.send(new ServerboundChatPacket(text));
    }


    public String getNickname() {
        return nickname;
    }

    public void registerMainListener() {
        hasMainListener = true;
        if (Main.isMinimal()) return;
        client.addListener(new MainListener(nickname));
    }

    public boolean hasMainListener() {
        return hasMainListener;
    }
}
