package com.shanebeestudios.mcbots.api;


import com.github.steveice10.mc.protocol.MinecraftConstants;
import com.github.steveice10.mc.protocol.MinecraftProtocol;
import com.github.steveice10.mc.protocol.data.status.ServerStatusInfo;
import com.github.steveice10.mc.protocol.data.status.handler.ServerInfoHandler;
import com.github.steveice10.mc.protocol.data.status.handler.ServerPingTimeHandler;
import com.github.steveice10.packetlib.Session;
import com.github.steveice10.packetlib.event.session.DisconnectedEvent;
import com.github.steveice10.packetlib.event.session.SessionAdapter;
import com.github.steveice10.packetlib.tcp.TcpClientSession;

import java.net.InetSocketAddress;

public class ServerInfo {

    private final Session client;
    private ServerStatusInfo serverStatusInfo;
    private long ping;
    private boolean done;

    public ServerInfo(InetSocketAddress address) {
        MinecraftProtocol protocol = new MinecraftProtocol();
        this.client = new TcpClientSession(address.getHostString(), address.getPort(), protocol, null);

        this.client.setFlag(MinecraftConstants.SERVER_INFO_HANDLER_KEY, (ServerInfoHandler) (session, info) -> {
            this.serverStatusInfo = info;
        });

        this.client.setFlag(MinecraftConstants.SERVER_PING_TIME_HANDLER_KEY, (ServerPingTimeHandler) (session, pingTime) -> {
            this.ping = pingTime;
        });

        this.client.addListener(new SessionAdapter() {
            @Override
            public void disconnected(DisconnectedEvent event) {
                done = true;
            }
        });
    }

    public void requestInfo() {
        this.client.connect();

        //wait to disconnect
        while (!this.done) {
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public ServerStatusInfo getStatusInfo() {
        return this.serverStatusInfo;
    }

    public long getPing() {
        return this.ping;
    }
}
