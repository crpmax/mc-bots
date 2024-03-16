package com.shanebeestudios.mcbots.api.bot;

import com.github.steveice10.mc.auth.service.AuthenticationService;
import com.github.steveice10.mc.auth.service.SessionService;
import com.github.steveice10.mc.protocol.MinecraftConstants;
import com.github.steveice10.mc.protocol.MinecraftProtocol;
import com.github.steveice10.mc.protocol.packet.ingame.serverbound.ServerboundChatCommandPacket;
import com.github.steveice10.mc.protocol.packet.ingame.serverbound.ServerboundChatPacket;
import com.github.steveice10.mc.protocol.packet.ingame.serverbound.player.ServerboundMovePlayerPosPacket;
import com.github.steveice10.packetlib.ProxyInfo;
import com.github.steveice10.packetlib.Session;
import com.github.steveice10.packetlib.packet.Packet;
import com.github.steveice10.packetlib.tcp.TcpClientSession;

import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.BitSet;
import java.util.Collections;

@SuppressWarnings("unused")
public class Bot {

    private final MinecraftProtocol protocol;
    private final BotManager botManager;
    private final String nickname;
    private final Session client;
    private double lastX, lastY, lastZ = -1;
    private boolean connected;
    private boolean manualDisconnecting = false;

    public Bot(BotManager botManager, String nickname, InetSocketAddress address, ProxyInfo proxy) {
        this.botManager = botManager;
        this.nickname = nickname;

        botManager.logBotCreated(nickname);
        this.protocol = new MinecraftProtocol(nickname);
        this.client = new TcpClientSession(address.getHostString(), address.getPort(), this.protocol, proxy);
    }

    public Bot(BotManager botManager, AuthenticationService authService, InetSocketAddress address, ProxyInfo proxy) {
        this.botManager = botManager;
        this.nickname = authService.getUsername();

        botManager.logBotCreated(this.nickname);
        this.protocol = new MinecraftProtocol(authService.getSelectedProfile(), authService.getAccessToken());

        this.client = new TcpClientSession(address.getHostString(), address.getPort(), this.protocol, proxy);

        SessionService sessionService = new SessionService();
        this.client.setFlag(MinecraftConstants.SESSION_SERVICE_KEY, sessionService);
    }

    /**
     * Connect the bot to the server
     */
    public void connect() {
        new Thread(() -> {
            this.client.addListener(new PacketListener(this));
            this.client.connect();
        }).start();
    }

    public BotManager getBotManager() {
        return this.botManager;
    }

    public Session getClient() {
        return this.client;
    }

    public String getNickname() {
        return this.nickname;
    }

    public boolean isConnected() {
        return this.connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public boolean isManualDisconnecting() {
        return this.manualDisconnecting;
    }

    public void sendChat(String text) {
        // timeStamp will provide when this message was sent by the user. If this value was not set or was set to 0,
        // The server console will print out that the message was "expired". To avoid this, set timeStamp as now.
        long timeStamp = Instant.now().toEpochMilli();
        BitSet bitSet = new BitSet();

        Packet packet;
        if (text.startsWith("/")) {
            // Send command
            packet = new ServerboundChatCommandPacket(text.substring(1), timeStamp, 0L, Collections.emptyList(), 0, bitSet);
        } else {
            // Send chat
            packet = new ServerboundChatPacket(text, timeStamp, 0L, null, 0, bitSet);
        }
        this.client.send(packet);
    }

    public void fallDown() {
        if (this.connected && this.lastY > 0) {
            move(0, -0.5, 0);
        }
    }

    public void setLastPosition(double x, double y, double z) {
        this.lastX = x;
        this.lastY = y;
        this.lastZ = z;
    }

    public void move(double x, double y, double z) {
        this.lastX += x;
        this.lastY += y;
        this.lastZ += z;
        moveTo(this.lastX, this.lastY, this.lastZ);
    }

    public void moveTo(double x, double y, double z) {
        this.client.send(new ServerboundMovePlayerPosPacket(true, x, y, z));
    }

    public void disconnect() {
        this.manualDisconnecting = true;
        this.client.disconnect("Leaving");
    }

}
