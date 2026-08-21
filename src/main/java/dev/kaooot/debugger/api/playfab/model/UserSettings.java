package dev.kaooot.debugger.api.playfab.model;

import com.google.gson.annotations.SerializedName;
import lombok.Value;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@Value
public class UserSettings {

    @SerializedName("GatherDeviceInfo")
    String gatherDeviceInfo;
    @SerializedName("GatherFocusInfo")
    String gatherFocusInfo;
    @SerializedName("NeedsAttribution")
    String needsAttribution;
}