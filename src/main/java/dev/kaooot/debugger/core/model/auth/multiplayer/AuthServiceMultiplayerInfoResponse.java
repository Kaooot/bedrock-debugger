package dev.kaooot.debugger.core.model.auth.multiplayer;

import java.util.Date;
import lombok.Value;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@Value
public class AuthServiceMultiplayerInfoResponse {

    Result result;

    @Value
    public static class Result {
        String signedToken;
        Date validUntil;
        Date issuedAt;
    }
}