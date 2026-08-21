package dev.kaooot.debugger.core.registry;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@Getter
@RequiredArgsConstructor
public enum RegistryKey {

    CONFIG("config"),
    COMMAND("command.internal"),
    PACKET_HANDLER("network.handler"),
    DEBUG_SCREEN("screen"),
    IMGUI_RENDERER("imgui.renderer");

    private final String subPackageName;
}