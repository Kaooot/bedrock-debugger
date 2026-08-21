package dev.kaooot.debugger.player.login;

import java.util.Map;
import lombok.Value;
import dev.kaooot.debugger.util.BedrockGameVersion;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@Value
public class LoginData {

    BedrockGameVersion gameVersion;
    int clientNetworkVersion;
    boolean isPreview;
    Map<String, Object> clientChainData;
}