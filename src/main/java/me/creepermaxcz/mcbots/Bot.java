package me.creepermaxcz.mcbots;

import org.geysermc.mcprotocollib.auth.GameProfile;
import org.geysermc.mcprotocollib.auth.SessionService;
import org.geysermc.mcprotocollib.network.ClientSession;
import org.geysermc.mcprotocollib.network.ProxyInfo;
import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.event.session.DisconnectedEvent;
import org.geysermc.mcprotocollib.network.event.session.SessionAdapter;
import org.geysermc.mcprotocollib.network.factory.ClientNetworkSessionFactory;
import org.geysermc.mcprotocollib.network.packet.Packet;
import org.geysermc.mcprotocollib.network.session.ClientNetworkSession;
import org.geysermc.mcprotocollib.protocol.MinecraftConstants;
import org.geysermc.mcprotocollib.protocol.MinecraftProtocol;
import org.geysermc.mcprotocollib.protocol.data.UnexpectedEncryptionException;
import org.geysermc.mcprotocollib.protocol.data.game.ClientCommand;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundLoginPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.player.ClientboundPlayerCombatKillPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.player.ClientboundPlayerPositionPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.ServerboundChatCommandPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.ServerboundChatPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.ServerboundClientCommandPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.level.ServerboundAcceptTeleportationPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.player.*;

import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Bot extends Thread {

    private String nickname;
    private ProxyInfo proxy;
    private InetSocketAddress address;
    private ClientSession client;
    private boolean hasMainListener;

    private double lastX, lastY, lastZ = -1;

    private boolean connected;

    private boolean manualDisconnecting = false;

    public Bot(MinecraftProtocol protocol, InetSocketAddress address, ProxyInfo proxy) {
        this.nickname = protocol.getProfile().getName();
        this.address = address;
        this.proxy = proxy;

        Log.info("Creating bot", nickname);

        client = ClientNetworkSessionFactory.factory()
                .setAddress(address.getHostString(), address.getPort())
                .setProtocol(protocol)
                .setProxy(proxy)
                .create();

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

                        if (Main.joinMessages.size() > 0) {
                            for (String msg : Main.joinMessages) {
                                sendChat(msg);

                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException ignored) {
                                }
                            }
                        }
                    }
                    else if (packet instanceof ClientboundPlayerPositionPacket) {
                        ClientboundPlayerPositionPacket p = (ClientboundPlayerPositionPacket) packet;

                        lastX = p.getPosition().getX();
                        lastY = p.getPosition().getY();
                        lastZ = p.getPosition().getZ();

                        client.send(new ServerboundAcceptTeleportationPacket(p.getId()));
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
                    Log.info(nickname + " disconnected");

                    // Do not write disconnect reason if disconnected by command
                    if (!manualDisconnecting) {
                        // Fix broken reason string by finding the content with regex
                        Pattern pattern = Pattern.compile("content=\"(.*?)\"");
                        Matcher matcher = pattern.matcher(String.valueOf(event.getReason()));

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

                    Main.removeBot(Bot.this);

                    Thread.currentThread().interrupt();
                }
            });
        }
        client.connect();
    }

    public void sendChat(String text) {
        // Send command
        if (text.startsWith("/")) {
            client.send(new ServerboundChatCommandPacket(
                    text.substring(1)
            ));
        } else {
            // Send chat message
            // From 1.19.1 or 1.19, the ServerboundChatPacket needs timestamp, salt and signed signature to generate packet.
            // tmpSignature will provide an empty byte array that can pretend it as signature.
            // salt is set 0 since this is offline server and no body will check it.

            client.send(new ServerboundChatPacket(
                    text,
                    Instant.now().toEpochMilli(),
                    0L,
                    null,
                    0,
                    new BitSet(),
                    0
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
        client.send(new ServerboundMovePlayerPosPacket(true, false, x, y, z));
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
