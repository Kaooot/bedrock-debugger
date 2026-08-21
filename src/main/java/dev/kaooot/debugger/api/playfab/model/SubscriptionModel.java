package dev.kaooot.debugger.api.playfab.model;

import com.google.gson.annotations.SerializedName;
import java.util.Date;
import lombok.Value;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@Value
public class SubscriptionModel {

    @SerializedName("Expiration")
    Date expiration;
    @SerializedName("InitialSubscriptionTime")
    Date initialSubscriptionTime;
    @SerializedName("IsActive")
    boolean isActive;
    @SerializedName("Status")
    SubscriptionProviderStatus status;
    @SerializedName("SubscriptionId")
    String subscriptionId;
    @SerializedName("SubscriptionItemId")
    String subscriptionItemId;
    @SerializedName("SubscriptionProvider")
    String subscriptionProvider;
}