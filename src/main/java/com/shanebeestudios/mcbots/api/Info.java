package com.shanebeestudios.mcbots.api;

import java.util.ArrayList;

public abstract class Info {

    protected int autoRespawnDelay;
    protected boolean minimal = false;
    protected boolean mostMinimal = false;
    protected boolean useRealNicknames;
    protected String nickPath = null;
    protected String nickPrefix = "";
    protected String address;
    protected int port;
    protected boolean hasGravity;
    protected ArrayList<String> joinMessages = new ArrayList<>();

    public Info() {
    }

    public boolean isMinimal() {
        return this.minimal;
    }

    public boolean isMostMinimal() {
        return this.mostMinimal;
    }

    public int getAutoRespawnDelay() {
        return this.autoRespawnDelay;
    }

    public boolean isUseRealNicknames() {
        return this.useRealNicknames;
    }

    public String getNickPath() {
        return this.nickPath;
    }

    public String getNickPrefix() {
        return this.nickPrefix;
    }

    public String getAddress() {
        return this.address;
    }

    public int getPort() {
        return this.port;
    }

    public boolean hasGravity() {
        return this.hasGravity;
    }

    public ArrayList<String> getJoinMessages() {
        return this.joinMessages;
    }

}
