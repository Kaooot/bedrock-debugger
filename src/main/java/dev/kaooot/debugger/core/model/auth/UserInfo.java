package dev.kaooot.debugger.core.model.auth;

import lombok.Value;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@Value
public class UserInfo {

    String language;
    String languageCode;
    String regionCode;
    String token;
    String tokenType;
}