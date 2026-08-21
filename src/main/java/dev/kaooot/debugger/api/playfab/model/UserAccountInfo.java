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
public class UserAccountInfo {

    @SerializedName("AndroidDeviceInfo")
    UserAndroidDeviceInfo androidDeviceInfo;
    @SerializedName("AppleAccountInfo")
    UserAppleIdInfo appleAccountInfo;
    @SerializedName("BattleNetAccountInfo")
    UserBattleNetInfo battleNetAccountInfo;
    @SerializedName("Created")
    Date created;
    @SerializedName("CustomIdInfo")
    UserCustomIdInfo customIdInfo;
    @SerializedName("FacebookInfo")
    UserFacebookInfo facebookInfo;
    @SerializedName("FacebookInstantGamesIdInfo")
    UserFacebookInstantGamesIdInfo facebookInstantGamesIdInfo;
    @SerializedName("GameCenterInfo")
    UserGameCenterInfo gameCenterInfo;
    @SerializedName("GoogleInfo")
    UserGoogleInfo googleInfo;
    @SerializedName("googlePlayGamesInfo")
    UserGooglePlayGamesInfo googlePlayGamesInfo;
    @SerializedName("IosDeviceInfo")
    UserAppleIdInfo iosDeviceInfo;
    @SerializedName("KongregateInfo")
    UserKongregateInfo kongregateInfo;
    @SerializedName("NintendoSwitchAccountInfo")
    UserNintendoSwitchAccountIdInfo nintendoSwitchAccountInfo;
    @SerializedName("NintendoSwitchDeviceIdInfo")
    UserNintendoSwitchDeviceIdInfo nintendoSwitchDeviceIdInfo;
    @SerializedName("OpenIdInfo")
    List<UserOpenIdInfo> openIdInfo;
    @SerializedName("PlayFabId")
    String playFabId;
    @SerializedName("PrivateInfo")
    UserPrivateAccountInfo privateInfo;
    @SerializedName("PsnInfo")
    UserPsnInfo PsnInfo;
    @SerializedName("ServerCustomIdInfo")
    UserServerCustomIdInfo serverCustomIdInfo;
    @SerializedName("SteamInfo")
    UserSteamInfo steamInfo;
    @SerializedName("TitleInfo")
    UserTitleInfo titleInfo;
    @SerializedName("TwitchInfo")
    UserTwitchInfo twitchInfo;
    @SerializedName("Username")
    String username;
    @SerializedName("XboxInfo")
    UserXboxInfo xboxInfo;
}