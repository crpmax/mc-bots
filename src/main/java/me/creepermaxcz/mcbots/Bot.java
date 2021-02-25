package me.creepermaxcz.mcbots;

import com.github.steveice10.mc.protocol.MinecraftProtocol;
import com.github.steveice10.mc.protocol.packet.ingame.client.ClientChatPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.ServerJoinGamePacket;
import com.github.steveice10.packetlib.Client;
import com.github.steveice10.packetlib.ProxyInfo;
import com.github.steveice10.packetlib.event.session.DisconnectedEvent;
import com.github.steveice10.packetlib.event.session.PacketReceivedEvent;
import com.github.steveice10.packetlib.event.session.SessionAdapter;
import com.github.steveice10.packetlib.tcp.TcpSessionFactory;

import java.net.InetSocketAddress;

public class Bot extends Thread {

    MinecraftProtocol protocol = null;

    private String nickname;
    private ProxyInfo proxy;
    private InetSocketAddress address;
    private Client client;
    private boolean hasMainListener;

    public Bot(String nickname, InetSocketAddress address, ProxyInfo proxy) {
        this.nickname = nickname;
        this.address = address;
        this.proxy = proxy;

        Log.info("Creating bot", nickname);
        protocol = new MinecraftProtocol(nickname);
        client = new Client(address.getHostString(), address.getPort(), protocol, new TcpSessionFactory(proxy));
    }

    @Override
    public void run() {

        if (!Main.isMinimal()) {
            client.getSession().addListener(new SessionAdapter() {

                @Override
                public void packetReceived(PacketReceivedEvent event) {
                    if (event.getPacket() instanceof ServerJoinGamePacket) {
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
                    Log.info(event.getReason());
                    if(event.getCause() != null) {
                        event.getCause().printStackTrace();
                    }
                    Log.info();
                    Main.removeBot(Bot.this);
                    Thread.currentThread().interrupt();
                }
            });
        }
        client.getSession().connect();
    }

    public void sendChat(String text) {
        client.getSession().send(new ClientChatPacket(text));
    }


    public String getNickname() {
        return nickname;
    }

    public void registerMainListener() {
        hasMainListener = true;
        if (Main.isMinimal()) return;
        client.getSession().addListener(new MainListener(nickname));
    }

    public boolean hasMainListener() {
        return hasMainListener;
    }
}
