package dev.kaooot.debugger.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@Getter
@RequiredArgsConstructor
public enum DebugElement {

    HIDE("?&!!"),
    CLIENT_NETWORK_INFO("!&??"),
    WIDE_SERVER_FORM("§a§n§r"),
    DEBUG_MENU_FORM("§a§m§r"),
    HIDE_BUILD_INFO("?&&!"),
    BUILD_INFO("!!??"),
    SMALL_INFO("!&?&");

    private final String key;
}