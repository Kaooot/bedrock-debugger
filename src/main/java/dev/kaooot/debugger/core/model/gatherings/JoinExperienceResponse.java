package dev.kaooot.debugger.core.model.gatherings;

import lombok.Value;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@Value
public class JoinExperienceResponse {

    Result result;

    @Value
    public static class Result {
        String ipV4Address;
        String networkProtocol;
        int port;
    }
}