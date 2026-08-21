package dev.kaooot.debugger.api.auth.request;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import java.security.interfaces.ECPublicKey;
import java.util.Base64;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import dev.kaooot.debugger.api.auth.AuthHttpRequest;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@Builder
@RequiredArgsConstructor
public class MinecraftLoginChainRequest extends AuthRequest<JsonObject> {

    private final ECPublicKey publicKey;
    private final String clientVersion;
    private final XBLTokenAuthRequest.Result result;

    /**
     * Generates a Minecraft Bedrock Edition login chain. The generation will be successful, when
     * the authentication process was completed.
     *
     * @return a fresh minecraft login chain that is signed by Mojang
     */
    @Override
    public JsonObject make() {
        Preconditions.checkNotNull(this.publicKey);
        Preconditions.checkNotNull(this.clientVersion);
        Preconditions.checkNotNull(this.result);

        final JsonObject data = new JsonObject();
        data.addProperty("identityPublicKey", Base64.getEncoder()
            .encodeToString(this.publicKey.getEncoded()));
        return AuthHttpRequest.builder()
            .uri(this.minecraftMultiplayerUrl)
            .method(AuthHttpRequest.RequestMethod.POST)
            .contentType(AuthHttpRequest.ContentType.JSON)
            .header("Authorization", this.result.getXboxToken())
            .header("Content-Type", "application/json")
            .header("User-Agent", "MCPE/Android")
            .header("Client-Version", clientVersion)
            .body(data)
            .responseHandler(() -> {
                throw new RuntimeException("The login chain generation failed");
            })
            .send();
    }
}