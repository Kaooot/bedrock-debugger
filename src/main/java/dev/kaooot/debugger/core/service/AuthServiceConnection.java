package dev.kaooot.debugger.core.service;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import dev.kaooot.debugger.BedrockDebuggerProxy;
import dev.kaooot.debugger.api.auth.request.DeviceTokenAuthRequest;
import dev.kaooot.debugger.api.auth.request.XBLTokenAuthRequest;
import dev.kaooot.debugger.api.playfab.PlayFabClient;
import dev.kaooot.debugger.api.playfab.model.GetPlayerCombinedInfoRequestParams;
import dev.kaooot.debugger.api.playfab.model.request.LoginWithXboxRequest;
import dev.kaooot.debugger.api.playfab.model.result.LoginResult;
import dev.kaooot.debugger.core.http.HttpRequest;
import dev.kaooot.debugger.core.http.HttpRequestMethod;
import dev.kaooot.debugger.core.model.auth.AuthServiceInfoRequest;
import dev.kaooot.debugger.core.model.auth.AuthServiceInfoResponse;
import dev.kaooot.debugger.core.model.auth.DeviceInfo;
import dev.kaooot.debugger.core.model.auth.UserInfo;
import dev.kaooot.debugger.core.model.auth.multiplayer.AuthServiceMultiplayerInfoRequest;
import dev.kaooot.debugger.core.model.auth.multiplayer.AuthServiceMultiplayerInfoResponse;
import dev.kaooot.debugger.network.NetworkConstants;
import dev.kaooot.debugger.player.login.LoginData;
import dev.kaooot.debugger.util.BedrockGameVersion;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@RequiredArgsConstructor
public class AuthServiceConnection {

    private final BedrockDebuggerProxy proxy;
    private final String host = "authorization.franchise.minecraft-services.net";
    private final String playFabTitleId = "20CA2";
    private final PlayFabClient playFabClient = PlayFabClient.builder()
        .titleId(this.playFabTitleId)
        .build();

    @Getter
    private AuthServiceInfoResponse result;

    @Getter
    private AuthServiceMultiplayerInfoResponse multiplayerResult;

    @Getter
    private PlayFabLoginData playFabLoginData;

    public void startSession(LoginData loginData) {
        this.startSession(loginData.getGameVersion(), loginData.isPreview());
    }

    public void startSession(BedrockGameVersion version, boolean isPreview) {
        final String gameVersion = NetworkConstants.CODEC.getMinecraftVersion() +
            (isPreview ? "-preview" + version.getRevision() : "");
        final PlayFabLoginData playFabLoginData = this.loginWithXbox();
        final AuthServiceInfoRequest request = new AuthServiceInfoRequest(
            new DeviceInfo(
                "MinecraftPE",
                Arrays.asList(
                    "RayTracing",
                    "VibrantVisuals"
                ),
                gameVersion,
                playFabLoginData.getDeviceId(),
                isPreview,
                "1024",
                "Windows10",
                this.playFabTitleId,
                "uwp.store",
                "Windows10"
            ),
            new UserInfo(
                "en",
                "en-US",
                "US",
                playFabLoginData.getSessionTicket(),
                "PlayFab"
            )
        );

        final AuthServiceInfoResponse result = HttpRequest.<AuthServiceInfoResponse>builder()
            .proxy(this.proxy)
            .method(HttpRequestMethod.POST)
            .url("https://" + this.host + "/api/v1.0/session/start")
            .body(request)
            .build()
            .send(AuthServiceInfoResponse.class);

        if (result == null) {
            throw new NullPointerException(
                "Failed to retrieve authorization service session start result"
            );
        }
        this.result = result;
        this.proxy.getLogger().info("Established authorization service connection");
    }

    public void startMultiplayerSession() {
        final AuthServiceMultiplayerInfoRequest request = new AuthServiceMultiplayerInfoRequest(
            Base64.getEncoder().encodeToString(this.proxy.getKeyPair().getPublic().getEncoded())
        );
        final AuthServiceMultiplayerInfoResponse result =
            HttpRequest.<AuthServiceMultiplayerInfoResponse>builder()
                .proxy(this.proxy)
                .method(HttpRequestMethod.POST)
                .url("https://" + this.host + "/api/v1.0/multiplayer/session/start")
                .auth(this.result.getResult().getAuthorizationHeader())
                .body(request)
                .build()
                .send(AuthServiceMultiplayerInfoResponse.class);

        if (result == null) {
            throw new NullPointerException(
                "Failed to retrieve authorization service multiplayer session start result"
            );
        }

        this.multiplayerResult = result;
        this.proxy.getLogger().info("Started multiplayer session");
    }

    private PlayFabLoginData loginWithXbox() {
        final String accessToken = this.proxy.getMsaAuth().getAccessToken();
        Objects.requireNonNull(accessToken, "The access token must not be null");

        final DeviceTokenAuthRequest.Result deviceTokenResult = DeviceTokenAuthRequest.builder()
            .build()
            .make();
        final XBLTokenAuthRequest.Result xblTokenResult = XBLTokenAuthRequest.builder()
            .accessToken(this.proxy.getMsaAuth().getAccessToken())
            .relyingParty("rp://playfabapi.com/")
            .result(deviceTokenResult)
            .build()
            .make();

        final LoginWithXboxRequest loginWithXboxRequest = new LoginWithXboxRequest();
        loginWithXboxRequest.setCreateAccount(true);

        final GetPlayerCombinedInfoRequestParams params = new GetPlayerCombinedInfoRequestParams();
        params.setGetCharacterInventories(true);
        params.setGetUserInventory(true);
        params.setGetPlayerProfile(true);
        params.setGetUserAccountInfo(true);
        params.setGetTitleData(true);

        loginWithXboxRequest.setInfoRequestParameters(params);
        loginWithXboxRequest.setTitleId(this.playFabTitleId);
        loginWithXboxRequest.setXboxToken(xblTokenResult.getXboxToken());

        final PlayFabClient.ResultWrapper<LoginResult> loginResultWrapper = this.playFabClient
            .loginWithXbox(loginWithXboxRequest);

        if (loginResultWrapper.getError() != null) {
            throw new IllegalStateException(
                "LoginWithXbox request failed. Error: " + loginResultWrapper.getError()
            );
        }

        this.proxy.getLogger().info("The PlayFab Login Token has been retrieved successfully");

        final LoginResult loginResult = loginResultWrapper.getResult();
        this.playFabLoginData = new PlayFabLoginData(
            loginResult.getSessionTicket(),
            this.convertDeviceId(deviceTokenResult.getDeviceId()),
            loginResult.getPlayFabId()
        );
        return this.playFabLoginData;
    }

    private UUID convertDeviceId(String deviceId) {
        final byte[] data = deviceId.getBytes(StandardCharsets.UTF_8);
        final long mostSignificantBits = ((long) data[0] << 56) |
            ((long) (data[1] & 0xff) << 48) |
            ((long) (data[2] & 0xff) << 40) |
            ((long) (data[3] & 0xff) << 32) |
            ((long) (data[4] & 0xff) << 24) |
            ((long) (data[5] & 0xff) << 16) |
            ((long) (data[6] & 0xff) << 8) |
            ((long) data[7] & 0xff);
        final long leastSignificantBits = ((long) data[8] << 56) |
            ((long) (data[9] & 0xff) << 48) |
            ((long) (data[10] & 0xff) << 40) |
            ((long) (data[11] & 0xff) << 32) |
            ((long) (data[12] & 0xff) << 24) |
            ((long) (data[13] & 0xff) << 16) |
            ((long) (data[14] & 0xff) << 8) |
            ((long) data[15] & 0xff);
        return new UUID(mostSignificantBits, leastSignificantBits);
    }

    @Value
    public static class PlayFabLoginData {
        String sessionTicket;
        UUID deviceId;
        String playFabId;
    }
}