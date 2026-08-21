package dev.kaooot.debugger.core.model.auth;

import java.util.Date;
import java.util.List;
import lombok.Value;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@Value
public class AuthServiceInfoResponse {

    Result result;

    @Value
    public static class Result {
        String authorizationHeader;
        Date validUntil;
        Date issuedAt;
        List<String> treatments;
        Configurations configurations;
        String treatmentContext;
    }
}