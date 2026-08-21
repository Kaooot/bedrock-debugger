package dev.kaooot.debugger.api.auth.util;

import com.google.gson.JsonObject;
import com.nimbusds.jwt.SignedJWT;
import java.text.ParseException;
import java.util.Map;
import java.util.UUID;
import lombok.Value;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@Value
public class AuthExtraData {

    public static final AuthExtraData INVALID = new AuthExtraData(
        null, new UUID(0L, 0L), null, null
    );

    String displayName;
    UUID identity;
    String xuid;
    String titleId;

    public static AuthExtraData fromChain(JsonObject loginChain) {
        try {
            final Map<?, ?> map = SignedJWT.parse(loginChain
                .getAsJsonArray("chain").get(1).getAsString()).getPayload()
                .toJSONObject();
            final Map<String, Object> extraData = (Map<String, Object>) map.get("extraData");
            return new AuthExtraData(
                extraData.get("displayName").toString(),
                UUID.fromString(extraData.get("identity").toString()),
                extraData.get("XUID").toString(),
                extraData.get("titleId").toString()
            );
        } catch (ParseException e) {
            e.printStackTrace();
            return AuthExtraData.INVALID;
        }
    }
}