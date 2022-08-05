package me.creepermaxcz.mcbots;

import com.github.steveice10.mc.protocol.MinecraftProtocol;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.ClientboundLoginPacket;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.entity.player.ClientboundPlayerPositionPacket;
import com.github.steveice10.mc.protocol.packet.ingame.serverbound.ServerboundChatCommandPacket;
import com.github.steveice10.mc.protocol.packet.ingame.serverbound.ServerboundChatPacket;
import com.github.steveice10.mc.protocol.packet.ingame.serverbound.level.ServerboundAcceptTeleportationPacket;
import com.github.steveice10.mc.protocol.packet.ingame.serverbound.player.*;
import com.github.steveice10.packetlib.ProxyInfo;
import com.github.steveice10.packetlib.Session;
import com.github.steveice10.packetlib.event.session.DisconnectedEvent;
import com.github.steveice10.packetlib.event.session.SessionAdapter;
import com.github.steveice10.packetlib.packet.Packet;
import com.github.steveice10.packetlib.tcp.TcpClientSession;

import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Bot extends Thread {

    MinecraftProtocol protocol = null;

    private String nickname;
    private ProxyInfo proxy;
    private InetSocketAddress address;
    private Session client;
    private boolean hasMainListener;

    private double lastX, lastY, lastZ = -1;

    private boolean connected;

    public Bot(String nickname, InetSocketAddress address, ProxyInfo proxy) {
        this.nickname = nickname;
        this.address = address;
        this.proxy = proxy;

        Log.info("Creating bot", nickname);
        protocol = new MinecraftProtocol(nickname);
        client = new TcpClientSession(address.getHostString(), address.getPort(), protocol, proxy);
    }

    @Override
    public void run() {

        if (!Main.isMinimal()) {
            client.addListener(new SessionAdapter() {

                @Override
                public void packetReceived(Session session, Packet packet) {
                    if (packet instanceof ClientboundLoginPacket) {
                        connected = true;
                        Log.info(nickname + " connected");
                        if (Main.joinMessage != null) {
                            sendChat(Main.joinMessage);
                        }
                    }
                    else if (packet instanceof ClientboundPlayerPositionPacket) {
                        ClientboundPlayerPositionPacket p = (ClientboundPlayerPositionPacket) packet;

                        lastX = p.getX();
                        lastY = p.getY();
                        lastZ = p.getZ();

                        client.send(new ServerboundAcceptTeleportationPacket(p.getTeleportId()));
                    }
                }

                @Override
                public void disconnected(DisconnectedEvent event) {
                    connected = false;
                    Log.info();
                    Log.info(nickname + " disconnected");

                    //Log.info(" -> " + event.getReason());

                    //fix broken reason string by finding the content with regex
                    Pattern pattern = Pattern.compile("content=\"(.*?)\"");
                    Matcher matcher = pattern.matcher(event.getReason());

                    StringBuilder reason = new StringBuilder();
                    while (matcher.find()) {
                        reason.append(matcher.group(1));
                    }

                    Log.info(" -> " + reason.toString());

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
        // timeStamp will provide when this message was sent by the user. If this value was not set or was set to 0,
        // The server console will print out that the message was "expired". To avoid this, set timeStamp as now.
        long timeStamp = Instant.now().toEpochMilli();

        //send command
        if (text.startsWith("/")) {

            client.send(new ServerboundChatCommandPacket(
                    text.substring(1), //remove slash on start
                    timeStamp,
                    0,
                    new HashMap<>(),
                    true
            ));
        } else {
            //send chat message

            // From 1.19.1 or 1.19, the ServerboundChatPacket needs timestamp, salt and signed signature to generate packet.
            // tmpSignature will provide an empty byte array that can pretend it as signature.
            byte[] tmpSignature = new byte[0]; // Set it empty byte array.
            // salt is set 0 since this is offline server and no body will check it.
            client.send(new ServerboundChatPacket(text, timeStamp, 0, tmpSignature, true));
        }

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

    public void fallDown()
    {
        if (connected && lastY > 0) {
            move(0, -0.5, 0);
        }
    }

    public void move(double x, double y, double z)
    {
        lastX += x;
        lastY += y;
        lastZ += z;
        moveTo(lastX, lastY, lastZ);
    }

    public void moveTo(double x, double y, double z)
    {
        client.send(new ServerboundMovePlayerPosPacket(true, x, y, z));
    }

    public boolean isConnected() {
        return connected;
    }
}
