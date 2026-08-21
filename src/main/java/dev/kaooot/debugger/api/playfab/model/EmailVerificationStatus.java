package dev.kaooot.debugger.api.playfab.model;

import com.google.gson.annotations.SerializedName;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
public enum EmailVerificationStatus {

    @SerializedName("Unverified")
    UNVERIFIED,
    @SerializedName("Pending")
    PENDING,
    @SerializedName("Confirmed")
    CONFIRMED
}