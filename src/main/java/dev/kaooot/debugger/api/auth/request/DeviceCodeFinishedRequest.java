package dev.kaooot.debugger.api.auth.request;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import dev.kaooot.debugger.api.auth.AuthHttpRequest;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@Builder
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class DeviceCodeFinishedRequest extends AuthRequest<DeviceCodeFinishedRequest.Result> {

    private final String deviceCode;

    /**
     * Proofs whether the authentication process is finished and caches the tokens retrieved by the
     * response
     *
     * @return true, when the process is completed, otherwise false
     */
    @Override
    public Result make() {
        Preconditions.checkNotNull(this.deviceCode);

        final JsonObject response = AuthHttpRequest.builder()
            .uri(this.oAuth20TokenUrl)
            .method(AuthHttpRequest.RequestMethod.POST)
            .contentType(AuthHttpRequest.ContentType.FORM)
            .header("client_id", this.clientId)
            .header("grant_type", "urn:ietf:params:oauth:grant-type:device_code")
            .header("device_code", this.deviceCode)
            .send();
        return response == null ? new Result(false, null, null) :
            new Result(true, response.get("access_token").getAsString(),
                response.get("refresh_token").getAsString());
    }

    @Value
    public static class Result {
        boolean finished;
        String accessToken;
        String refreshToken;
    }
}