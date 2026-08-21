package dev.kaooot.debugger.core.model.auth;

import java.util.List;
import java.util.UUID;
import lombok.Value;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@Value
public class DeviceInfo {

    String applicationType;
    List<String> capabilities;
    String gameVersion;
    UUID id;
    boolean isPreview;
    String memory;
    String platform;
    String playFabTitleId;
    String storePlatform;
    List<String> treatmentOverrides = null;
    String type;
}