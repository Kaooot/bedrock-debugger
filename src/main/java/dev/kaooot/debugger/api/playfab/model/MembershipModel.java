package dev.kaooot.debugger.api.playfab.model;

import com.google.gson.annotations.SerializedName;
import java.util.Date;
import java.util.List;
import lombok.Value;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@Value
public class MembershipModel {

    @SerializedName("IsActive")
    boolean isActive;
    @SerializedName("MembershipExpiration")
    Date membershipExpiration;
    @SerializedName("MembershipId")
    String membershipId;
    @SerializedName("OverrideExpiration")
    Date overrideExpiration;
    @SerializedName("OverrideIsSet")
    boolean overrideIsSet;
    @SerializedName("Subscriptions")
    List<SubscriptionModel> subscriptions;
}