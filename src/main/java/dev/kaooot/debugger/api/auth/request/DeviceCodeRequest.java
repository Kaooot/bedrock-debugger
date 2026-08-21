package dev.kaooot.debugger.api.auth.request;

import com.google.gson.JsonObject;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Value;
import dev.kaooot.debugger.api.auth.AuthHttpRequest;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DeviceCodeRequest extends AuthRequest<DeviceCodeRequest.Result> {

    /**
     * Generates the code which is used for the authentication process
     *
     * @return a fresh device code used for auth
     */
    @Override
    public Result make() {
        final JsonObject response = AuthHttpRequest.builder()
            .uri(this.oAuth20ConnectUri)
            .method(AuthHttpRequest.RequestMethod.POST)
            .contentType(AuthHttpRequest.ContentType.FORM)
            .header("client_id", this.clientId)
            .header("scope", "service::user.auth.xboxlive.com::MBI_SSL")
            .header("response_type", "device_code")
            .responseHandler(() -> {
                throw new RuntimeException("The authentication code could not be generated");
            })
            .send();
        return new Result(response.get("device_code").getAsString(),
            response.get("user_code").getAsString());
    }

    @Value
    public static class Result {
        String deviceCode;
        String userCode;
    }
}