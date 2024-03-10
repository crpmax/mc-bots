package com.shanebeestudios.mcbots.api.generator;

import com.github.steveice10.packetlib.ProxyInfo;
import com.shanebeestudios.mcbots.api.util.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

public class ProxyGenerator {

    private ProxyInfo.Type proxyType;
    private final String proxyPath;
    private final ArrayList<InetSocketAddress> proxies = new ArrayList<>();

    public ProxyGenerator(String proxyType, String proxyPath) {
        this.proxyPath = proxyPath;
        //get proxy type
        try {
            this.proxyType = ProxyInfo.Type.valueOf(proxyType);
        } catch (IllegalArgumentException e) {
            Logger.error("Invalid proxy type, use SOCKS4 or SOCKS5.");
            System.exit(1);
        }

        readProxyFileList();
    }

    private void readProxyFileList() {
        try {
            try {
                //try to read specified path as URL
                URL url = new URL(this.proxyPath);

                BufferedReader read = new BufferedReader(
                    new InputStreamReader(url.openStream()));

                Logger.info("Reading proxies from URL");
                String line;
                while ((line = read.readLine()) != null) {
                    try {
                        String[] parts = line.trim().split(":");
                        if (parts.length == 2) {
                            int port = Integer.parseInt(parts[1]);
                            this.proxies.add(new InetSocketAddress(parts[0], port));
                        }
                    } catch (Exception ignored) {
                    }
                }
                read.close();

            } catch (MalformedURLException e) {
                Logger.info("Specified proxy file is not a URL, trying to read file");

                Scanner scanner = new Scanner(new File(this.proxyPath));
                while (scanner.hasNextLine()) {
                    try {
                        String[] parts = scanner.nextLine().trim().split(":");
                        if (parts.length == 2) {
                            int port = Integer.parseInt(parts[1]);
                            proxies.add(new InetSocketAddress(parts[0], port));
                        }
                    } catch (Exception ignored) {
                    }
                }
                scanner.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } catch (FileNotFoundException e) {
            Logger.error("Invalid proxy list file path.");
            System.exit(1);
        }

        int size = this.proxies.size();
        if (size > 0) {
            Logger.info("Loaded " + size + " valid proxies");
        } else {
            Logger.error("No valid proxies loaded");
            System.exit(1);
        }
    }

    public ProxyInfo.Type getProxyType() {
        return this.proxyType;
    }

    public ArrayList<InetSocketAddress> getProxies() {
        return this.proxies;
    }

}
