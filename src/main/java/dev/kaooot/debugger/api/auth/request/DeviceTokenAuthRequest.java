package dev.kaooot.debugger.api.auth.request;

import com.google.gson.JsonObject;
import java.security.KeyPair;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.Value;
import dev.kaooot.debugger.api.auth.AuthHttpRequest;
import dev.kaooot.debugger.api.auth.util.ECDSAUtil;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@Builder
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class DeviceTokenAuthRequest extends TokenAuthRequest<DeviceTokenAuthRequest.Result> {

    @SneakyThrows
    @Override
    public Result make() {
        final KeyPair deviceTokenProofKeyPair = ECDSAUtil.generateKeyPair();
        final ECPublicKey publicKey = (ECPublicKey) deviceTokenProofKeyPair.getPublic();
        final ECPrivateKey privateKey = (ECPrivateKey) deviceTokenProofKeyPair.getPrivate();

        final JsonObject properties = new JsonObject();
        properties.addProperty("AuthMethod", "ProofOfPossession");
        properties.addProperty("DeviceType", "Android");
        properties.addProperty("Id", "{" + UUID.randomUUID() + "}");
        properties.add("ProofKey", this.buildProofKey(publicKey));
        properties.addProperty("Version", "0.0.0");

        final JsonObject data = new JsonObject();
        data.add("Properties", properties);
        data.addProperty("RelyingParty", "http://auth.xboxlive.com");
        data.addProperty("TokenType", "JWT");

        final JsonObject response = AuthHttpRequest.builder()
            .uri(this.xboxLiveDeviceAuthUrl)
            .method(AuthHttpRequest.RequestMethod.POST)
            .contentType(AuthHttpRequest.ContentType.JSON)
            .header("x-xbl-contract-version", "1")
            .header("Signature", this.sign(data.toString().replace("\\/", "/"),
                this.xboxLiveDeviceAuthUrl.toURL(), privateKey))
            .body(data)
            .responseHandler(() -> {
                throw new RuntimeException("The device token generation failed");
            })
            .send();
        final JsonObject displayClaims = response.getAsJsonObject("DisplayClaims");
        final JsonObject xdi = displayClaims.getAsJsonObject("xdi");
        final String deviceId = xdi.get("did").getAsString();
        return new Result(deviceTokenProofKeyPair,
            response.get("Token").getAsString(), deviceId);
    }

    @Value
    public static class Result {
        KeyPair deviceTokenProofKeyPair;
        String deviceToken;
        String deviceId;
    }
}