package dev.kaooot.debugger.api.playfab.model;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.ToString;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@Data
@ToString
public class PlayerProfileViewConstraints {

    @SerializedName("ShowAvatarUrl")
    private boolean showAvatarUrl;
    @SerializedName("ShowBannedUntil")
    private boolean showBannedUntil;
    @SerializedName("ShowCampaignAttributions")
    private boolean showCampaignAttributions;
    @SerializedName("ShowContactEmailAddresses")
    private boolean showContactEmailAddresses;
    @SerializedName("ShowCreated")
    private boolean showCreated;
    @SerializedName("ShowDisplayName")
    private boolean showDisplayName;
    @SerializedName("ShowExperimentVariants")
    private boolean showExperimentVariants;
    @SerializedName("ShowLastLogin")
    private boolean showLastLogin;
    @SerializedName("ShowLinkedAccounts")
    private boolean showLinkedAccounts;
    @SerializedName("ShowLocations")
    private boolean showLocations;
    @SerializedName("showMemberships")
    private boolean ShowMemberships;
    @SerializedName("showOrigination")
    private boolean ShowOrigination;
    @SerializedName("ShowPushNotificationRegistrations")
    private boolean showPushNotificationRegistrations;
    @SerializedName("ShowStatistics")
    private boolean showStatistics;
    @SerializedName("ShowTags")
    private boolean showTags;
    @SerializedName("ShowTotalValueToDateInUsd")
    private boolean showTotalValueToDateInUsd;
    @SerializedName("ShowValuesToDate")
    private boolean showValuesToDate;
}