package dev.kaooot.debugger.api.playfab.model;

import lombok.Value;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@Value
public class ApiErrorWrapper {
    public static final ApiErrorWrapper INVALID =
        new ApiErrorWrapper(-1, null, -1, null, null, null);

    int code;
    String error;
    int errorCode;
    Object errorDetails;
    String errorMessage;
    String status;
}