package dev.kaooot.debugger.api.playfab.model;

import com.google.gson.annotations.SerializedName;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
public enum ModerationStatus {

    @SerializedName("Unknown")
    UNKNOWN,
    @SerializedName("Awaiting_Moderation")
    AWAITING_MODERATION,
    @SerializedName("Approved")
    APPROVED,
    @SerializedName("Rejected")
    REJECTED
}