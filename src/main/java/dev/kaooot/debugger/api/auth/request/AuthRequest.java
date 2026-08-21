package dev.kaooot.debugger.api.auth.request;

import java.net.URI;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
public abstract class AuthRequest<T> {

    protected final String clientId = "0000000048183522";

    protected final URI oAuth20TokenUrl = URI.create("https://login.live.com/oauth20_token.srf");
    protected final URI oAuth20ConnectUri = URI.create(
            "https://login.live.com/oauth20_connect.srf"
        );
    protected final URI minecraftMultiplayerUrl = URI.create(
        "https://multiplayer.minecraft.net/authentication"
    );

    public abstract T make();
}