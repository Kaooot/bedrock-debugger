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
public class PlayerProfileModel {

    @SerializedName("AdCampaignAttributions")
    List<AdCampaignAttributionModel> adCampaignAttributions;
    @SerializedName("AvatarUrl")
    String avatarUrl;
    @SerializedName("BannedUntil")
    Date bannedUntil;
    @SerializedName("ContactEmailAddresses")
    List<ContactEmailInfoModel> contactEmailAddresses;
    @SerializedName("Created")
    Date created;
    @SerializedName("DisplayName")
    String displayName;
    @SerializedName("ExperimentVariants")
    List<String> experimentVariants;
    @SerializedName("LastLogin")
    Date lastLogin;
    @SerializedName("LinkedAccounts")
    List<LinkedPlatformAccountModel> linkedAccounts;
    @SerializedName("Locations")
    List<LocationModel> locations;
    @SerializedName("Memberships")
    List<MembershipModel> memberships;
    @SerializedName("Origination")
    LoginIdentityProvider origination;
    @SerializedName("PlayerId")
    String playerId;
    @SerializedName("PublisherId")
    String publisherId;
    @SerializedName("PushNotificationRegistrations")
    List<PushNotificationRegistrationModel> pushNotificationRegistrations;
    @SerializedName("Statistics")
    List<StatisticModel> statistics;
    @SerializedName("Tags")
    List<TagModel> tags;
    @SerializedName("TitleId")
    String titleId;
    @SerializedName("TotalValueToDateInUSD")
    long totalValueToDateInUSD;
    @SerializedName("ValuesToDate")
    List<ValueToDateModel> valuesToDate;
}