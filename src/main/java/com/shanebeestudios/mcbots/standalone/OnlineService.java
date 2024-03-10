package com.shanebeestudios.mcbots.standalone;

import com.github.steveice10.mc.auth.exception.request.AuthPendingException;
import com.github.steveice10.mc.auth.exception.request.RequestException;
import com.github.steveice10.mc.auth.service.AuthenticationService;
import com.github.steveice10.mc.auth.service.MsaAuthenticationService;
import com.github.steveice10.mc.auth.util.HTTP;
import com.shanebeestudios.mcbots.api.util.Logger;

import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class OnlineService {

    private final String clientId = "8bef943e-5a63-429e-a93a-96391d2e32a9";
    private final StandaloneInfo standaloneInfo;

    public OnlineService(StandaloneInfo standaloneInfo) {
        this.standaloneInfo = standaloneInfo;
    }

    public AuthenticationService getOnlineAuthenticationService() {
        Logger.warn("Online mode enabled. The bot count will be set to 1.");
        this.standaloneInfo.setBotCount(1);

        // Create request parameters map
        Map<String, String> params = new HashMap<>();
        params.put("client_id", clientId);
        params.put("scope", "XboxLive.signin");

        // Send request to Microsoft OAuth api
        // https://learn.microsoft.com/en-us/azure/active-directory/develop/v2-oauth2-device-code
        URI endpointURI = URI.create("https://login.microsoftonline.com/consumers/oauth2/v2.0/devicecode");
        MsaAuthenticationService.MsCodeResponse response;
        try {
            response = HTTP.makeRequestForm(
                Proxy.NO_PROXY, endpointURI, params, MsaAuthenticationService.MsCodeResponse.class
            );
        } catch (RequestException e) {
            throw new RuntimeException(e);
        }

        try {
            Logger.info("Please go to " + response.verification_uri.toURL() + " to authenticate your account - Code: " + response.user_code);
        } catch (MalformedURLException e) {
            Logger.error("Error while trying to get the url of the authentication page");
        }

        AuthenticationService authService = new MsaAuthenticationService(clientId, response.device_code);

        // Wait for user to login on microsoft page
        int retryMax = 20;
        while (true) {
            try {
                authService.login();
                break;
            } catch (Exception e) {
                if (e instanceof AuthPendingException) {
                    Logger.info("Authentication is pending, waiting for user to authenticate...");
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException ignored) {
                    }
                    if (retryMax == 0)
                        throw new RuntimeException(e);
                    retryMax--;
                } else {
                    throw new RuntimeException(e);
                }
            }
        }
        return authService;
    }

}
