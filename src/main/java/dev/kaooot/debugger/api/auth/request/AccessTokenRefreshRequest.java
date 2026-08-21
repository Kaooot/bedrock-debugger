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
public class AccessTokenRefreshRequest extends AuthRequest<AccessTokenRefreshRequest.Result> {

    private final String refreshToken;

    /**
     * Refreshes the access token by the given refresh token
     *
     * @return the refreshed tokens
     */
    @Override
    public Result make() {
        Preconditions.checkNotNull(this.refreshToken);

        final JsonObject response = AuthHttpRequest.builder()
            .uri(this.oAuth20TokenUrl)
            .method(AuthHttpRequest.RequestMethod.POST)
            .contentType(AuthHttpRequest.ContentType.FORM)
            .header("client_id", this.clientId)
            .header("scope", "service::user.auth.xboxlive.com::MBI_SSL")
            .header("grant_type", "refresh_token")
            .header("refresh_token", this.refreshToken)
            .responseHandler(() -> {
                throw new RuntimeException(
                    "The access token could not be refreshed by the given refresh token"
                );
            })
            .send();
        return new Result(response.get("access_token").getAsString(),
            response.get("refresh_token").getAsString());
    }

    @Value
    public static class Result {
        String accessToken;
        String refreshToken;
    }
}