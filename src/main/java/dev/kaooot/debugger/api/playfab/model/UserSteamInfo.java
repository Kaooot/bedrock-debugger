package dev.kaooot.debugger.api.playfab.model;

import com.google.gson.annotations.SerializedName;
import lombok.Value;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@Value
public class UserSteamInfo {

    @SerializedName("SteamActivationStatus")
    TitleActivationStatus steamActivationStatus;
    @SerializedName("SteamCountry")
    String SteamCountry;
    @SerializedName("SteamCurrency")
    Currency SteamCurrency;
    @SerializedName("SteamId")
    String SteamId;
    @SerializedName("SteamName")
    String SteamName;
}