package dev.kaooot.debugger.api.playfab.model;

import com.google.gson.annotations.SerializedName;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
public enum TitleActivationStatus {

    @SerializedName("None")
    NONE,
    @SerializedName("ActivatedTitleKey")
    ACTIVATED_TITLE_KEY,
    @SerializedName("PendingSteam")
    PENDING_STEAM,
    @SerializedName("ActivatedSteam")
    ACTIVATED_STEAM,
    @SerializedName("RevokedSteam")
    REVOKED_STEAM
}