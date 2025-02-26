package me.creepermaxcz.mcbots;


import org.geysermc.mcprotocollib.network.ClientSession;
import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.event.session.DisconnectedEvent;
import org.geysermc.mcprotocollib.network.event.session.SessionAdapter;
import org.geysermc.mcprotocollib.network.factory.ClientNetworkSessionFactory;
import org.geysermc.mcprotocollib.protocol.MinecraftConstants;
import org.geysermc.mcprotocollib.protocol.MinecraftProtocol;
import org.geysermc.mcprotocollib.protocol.data.status.ServerStatusInfo;
import org.geysermc.mcprotocollib.protocol.data.status.handler.ServerInfoHandler;
import org.geysermc.mcprotocollib.protocol.data.status.handler.ServerPingTimeHandler;


import java.net.InetSocketAddress;

public class ServerInfo {

    private final ClientSession client;
    private ServerStatusInfo serverStatusInfo;
    private long ping;
    private boolean done;

    public ServerInfo(InetSocketAddress address) {

        MinecraftProtocol protocol = new MinecraftProtocol();

        client = ClientNetworkSessionFactory.factory()
                .setAddress(address.getHostString(), address.getPort())
                .setProtocol(protocol)
                .create();

        client.setFlag(MinecraftConstants.SERVER_INFO_HANDLER_KEY, (ServerInfoHandler) (session, info) -> {
            this.serverStatusInfo = info;
        });

        client.setFlag(MinecraftConstants.SERVER_PING_TIME_HANDLER_KEY, (ServerPingTimeHandler) (session, pingTime) -> {
            this.ping = pingTime;
        });

        client.addListener(new SessionAdapter() {
            @Override
            public void disconnected(DisconnectedEvent event) {
                done = true;
            }
        });
    }

    public void requestInfo()
    {
        client.connect();

        //wait to disconnect
        while (!done) {
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public ServerStatusInfo getStatusInfo()
    {
        return serverStatusInfo;
    }

    public long getPing() {
        return ping;
    }
}
