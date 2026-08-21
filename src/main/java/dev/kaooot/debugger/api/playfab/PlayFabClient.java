package dev.kaooot.debugger.api.playfab;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import javax.net.ssl.HttpsURLConnection;
import lombok.Builder;
import lombok.Value;
import dev.kaooot.debugger.api.playfab.model.ApiErrorWrapper;
import dev.kaooot.debugger.api.playfab.model.request.GetEntityTokenRequest;
import dev.kaooot.debugger.api.playfab.model.request.GetItemsRequest;
import dev.kaooot.debugger.api.playfab.model.request.LoginWithXboxRequest;
import dev.kaooot.debugger.api.playfab.model.request.SearchItemsRequest;
import dev.kaooot.debugger.api.playfab.model.result.EntityTokenResponse;
import dev.kaooot.debugger.api.playfab.model.result.GetItemsResponse;
import dev.kaooot.debugger.api.playfab.model.result.LoginResult;
import dev.kaooot.debugger.api.playfab.model.result.SearchItemsResponse;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@Builder
public class PlayFabClient {

    private static final Gson GSON = new GsonBuilder()
        .setPrettyPrinting()
        .disableHtmlEscaping()
        .create();

    private final String titleId;

    private String entityToken;

    public ResultWrapper<LoginResult> loginWithXbox(LoginWithXboxRequest request) {
        final ResultWrapper<LoginResult> result = this.postRequest(
            "Client/LoginWithXbox", request, LoginResult.class, false
        );
        if (result.getError() == null) {
            this.entityToken = result.getResult().getEntityToken().getEntityToken();
        }
        return result;
    }

    public ResultWrapper<EntityTokenResponse> getEntityToken(GetEntityTokenRequest request) {
        final ResultWrapper<EntityTokenResponse> result = this.postRequest(
            "Authentication/GetEntityToken", request, EntityTokenResponse.class
        );
        if (result.getError() == null) {
            this.entityToken = result.getResult().getEntityToken();
        }
        return result;
    }

    public ResultWrapper<SearchItemsResponse> searchItems(SearchItemsRequest request) {
        return this.postRequest("Catalog/SearchItems", request, SearchItemsResponse.class);
    }

    public ResultWrapper<GetItemsResponse> getItems(GetItemsRequest request) {
        return this.postRequest("Catalog/GetItems", request, GetItemsResponse.class);
    }

    private <R, E> ResultWrapper<R> postRequest(String endpoint, E body, Class<R> clazz) {
        return this.postRequest(endpoint, body, clazz, true);
    }

    private <R, E> ResultWrapper<R> postRequest(String endpoint, E body, Class<R> clazz,
                                                boolean authentication) {
        try {
            final String base = "https://" + this.titleId.toLowerCase() + ".playfabapi.com/";
            final URL url = new URL(base + endpoint);
            final HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            if (authentication) {
                Preconditions.checkNotNull(this.entityToken, "The entity token must not be null");
                connection.setRequestProperty("X-EntityToken", this.entityToken);
            }
            connection.setConnectTimeout(60000);
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.connect();

            try (final OutputStream outputStream = connection.getOutputStream()) {
                outputStream.write(GSON.toJson(body).getBytes(StandardCharsets.UTF_8));
            }

            final int code = connection.getResponseCode();
            final String message = connection.getResponseMessage();

            if (code != 200) {
                return new ResultWrapper<>(new ApiErrorWrapper(code, message, code, null, message,
                    null), null);
            }

            final byte[] data;
            try (final InputStream inputStream = connection.getInputStream()) {
                data = inputStream.readAllBytes();
            }
            connection.disconnect();
            final JsonObject result = GSON.fromJson(new String(data), JsonObject.class);

            if (!result.has("code") && !result.has("status")) {
                return (ResultWrapper<R>) ResultWrapper.INVALID;
            }

            final int resultCode = result.get("code").getAsInt();
            final String status = result.get("status").getAsString();

            if (resultCode != 200) {
                return new ResultWrapper<>(new ApiErrorWrapper(resultCode, message, resultCode,
                    null, message, status), null);
            }

            final R res = GSON.fromJson(result.getAsJsonObject("data"), clazz);
            return new ResultWrapper<>(null, res);
        } catch (IOException e) {
            e.printStackTrace();
            return (ResultWrapper<R>) ResultWrapper.INVALID;
        }
    }

    @Value
    public static class ResultWrapper<R> {
        public static final ResultWrapper<?> INVALID =
            new ResultWrapper<>(ApiErrorWrapper.INVALID, null);

        ApiErrorWrapper error;
        R result;
    }
}