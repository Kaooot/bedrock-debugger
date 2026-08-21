package dev.kaooot.debugger.api.auth.request;

import com.google.common.base.Preconditions;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.Value;
import dev.kaooot.debugger.api.auth.AuthHttpRequest;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@Builder
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class XBLTokenAuthRequest extends TokenAuthRequest<XBLTokenAuthRequest.Result> {

    private final String accessToken;
    private final String relyingParty;
    private final DeviceTokenAuthRequest.Result result;

    @SneakyThrows
    @Override
    public Result make() {
        Preconditions.checkNotNull(this.accessToken);
        Preconditions.checkNotNull(this.relyingParty);
        Preconditions.checkNotNull(this.result);

        final JsonObject data = new JsonObject();
        data.addProperty("AccessToken", "t=" + this.accessToken);
        data.addProperty("AppId", this.clientId);
        data.addProperty("DeviceToken", this.result.getDeviceToken());
        data.addProperty("Sandbox", "RETAIL");
        data.addProperty("UseModernGamertag", "true");
        data.addProperty("SiteName", "user.auth.xboxlive.com");
        data.addProperty("RelyingParty", this.relyingParty);
        data.add("ProofKey",
            this.buildProofKey((ECPublicKey) this.result.getDeviceTokenProofKeyPair().getPublic()));

        final JsonObject response = AuthHttpRequest.builder()
            .uri(this.xboxLiveSisuUrl)
            .method(AuthHttpRequest.RequestMethod.POST)
            .contentType(AuthHttpRequest.ContentType.JSON)
            .header("X-Xbl-Contract-Version", "1")
            .header("Signature", this.sign(
                    data.toString().replace("\\/", "/"),
                    this.xboxLiveSisuUrl.toURL(),
                    (ECPrivateKey) this.result.getDeviceTokenProofKeyPair().getPrivate()
                )
            )
            .body(data)
            .responseHandler(() -> {
                throw new RuntimeException("Failed to retrieve XBL Token");
            })
            .send();

        final JsonObject authToken = response.getAsJsonObject("AuthorizationToken");

        final String xblToken = authToken.get("Token").getAsString();

        final JsonObject displayClaims = authToken.getAsJsonObject("DisplayClaims");
        final JsonArray userInfo = displayClaims.getAsJsonArray("xui");
        final JsonObject userInfoEntry = userInfo.get(0).getAsJsonObject();

        final String userHash = userInfoEntry.get("uhs").getAsString();
        return new Result(xblToken, userHash, "XBL3.0 x=" + userHash + ";" + xblToken);
    }

    @Value
    public static class Result {
        String xblToken;
        String userHash;
        String xboxToken;
    }
}