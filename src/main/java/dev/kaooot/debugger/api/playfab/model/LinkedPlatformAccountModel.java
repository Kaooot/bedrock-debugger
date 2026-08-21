package dev.kaooot.debugger.api.playfab.model;

import com.google.gson.annotations.SerializedName;
import lombok.Value;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@Value
public class LinkedPlatformAccountModel {

    @SerializedName("Email")
    String email;
    @SerializedName("Platform")
    LoginIdentityProvider platform;
    @SerializedName("PlatformUserId")
    String platformUserId;
    @SerializedName("Username")
    String username;
}