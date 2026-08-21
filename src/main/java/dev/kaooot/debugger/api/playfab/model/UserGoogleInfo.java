package dev.kaooot.debugger.api.playfab.model;

import com.google.gson.annotations.SerializedName;
import lombok.Value;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@Value
public class UserGoogleInfo {

    @SerializedName("GoogleEmail")
    String googleEmail;
    @SerializedName("GoogleGender")
    String googleGender;
    @SerializedName("GoogleId")
    String googleId;
    @SerializedName("GoogleLocale")
    String googleLocale;
    @SerializedName("GoogleName")
    String googleName;
}