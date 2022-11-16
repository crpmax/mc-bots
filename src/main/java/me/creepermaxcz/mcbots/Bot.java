package me.creepermaxcz.mcbots;

import com.github.steveice10.mc.auth.service.AuthenticationService;
import com.github.steveice10.mc.auth.service.SessionService;
import com.github.steveice10.mc.protocol.MinecraftConstants;
import com.github.steveice10.mc.protocol.MinecraftProtocol;
import com.github.steveice10.mc.protocol.data.UnexpectedEncryptionException;
import com.github.steveice10.mc.protocol.data.game.ClientCommand;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.ClientboundLoginPacket;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.ClientboundRespawnPacket;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.entity.player.ClientboundPlayerCombatKillPacket;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.entity.player.ClientboundPlayerPositionPacket;
import com.github.steveice10.mc.protocol.packet.ingame.serverbound.ServerboundChatCommandPacket;
import com.github.steveice10.mc.protocol.packet.ingame.serverbound.ServerboundChatPacket;
import com.github.steveice10.mc.protocol.packet.ingame.serverbound.ServerboundClientCommandPacket;
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
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
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

    private boolean manualDisconnecting = false;

    public Bot(String nickname, InetSocketAddress address, ProxyInfo proxy) {
        this.nickname = nickname;
        this.address = address;
        this.proxy = proxy;

        Log.info("Creating bot", nickname);
        protocol = new MinecraftProtocol(nickname);
        client = new TcpClientSession(address.getHostString(), address.getPort(), protocol, proxy);
    }

    public Bot(AuthenticationService authService, InetSocketAddress address, ProxyInfo proxy) {
        this.nickname = authService.getUsername();
        this.address = address;
        this.proxy = proxy;

        Log.info("Creating bot", nickname);
        protocol = new MinecraftProtocol(authService.getSelectedProfile(), authService.getAccessToken());

        client = new TcpClientSession(address.getHostString(), address.getPort(), protocol, proxy);

        SessionService sessionService = new SessionService();
        client.setFlag(MinecraftConstants.SESSION_SERVICE_KEY, sessionService);
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
                    else if (packet instanceof ClientboundPlayerCombatKillPacket){
                        if (Main.autoRespawnDelay >= 0) {
                            Log.info("Bot " + nickname + " died. Respawning in " + Main.autoRespawnDelay + " ms.");
                            new Timer().schedule(
                                    new TimerTask() {
                                        @Override
                                        public void run() {
                                            client.send(new ServerboundClientCommandPacket(ClientCommand.RESPAWN));
                                        }
                                    },
                                    Main.autoRespawnDelay
                            );

                        }
                    }
                }

                @Override
                public void disconnected(DisconnectedEvent event) {
                    connected = false;
                    Main.removeBot(Bot.this);
                    Log.info(nickname + " disconnected");

                    // Do not write disconnect reason if disconnected by command
                    if (!manualDisconnecting) {
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

                            if (event.getCause() instanceof UnexpectedEncryptionException) {
                                Log.warn("Server is running in online (premium) mode. Please use the -o option to use online mode bot.");
                                System.exit(1);
                            }
                        }
                        Log.info();
                    }

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

        // Send command
        if (text.startsWith("/")) {
            client.send(new ServerboundChatCommandPacket(
                    text.substring(1),
                    timeStamp,
                    0,
                    new ArrayList<>(),
                    true,
                    new ArrayList<>(),
                    null
            ));
        } else {
            // Send chat message
            // From 1.19.1 or 1.19, the ServerboundChatPacket needs timestamp, salt and signed signature to generate packet.
            // tmpSignature will provide an empty byte array that can pretend it as signature.
            // salt is set 0 since this is offline server and no body will check it.
            client.send(new ServerboundChatPacket(text,
                    timeStamp,
                    0,
                    new byte[0],
                    true,
                    new ArrayList<>(),
                    null
            ));
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

    public void disconnect()
    {
        manualDisconnecting = true;
        client.disconnect("Leaving");
    }
}
