package dev.kaooot.debugger.api.playfab.model;

import com.google.gson.annotations.SerializedName;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
public enum LoginIdentityProvider {

    @SerializedName("Unknown")
    UNKNOWN,
    @SerializedName("PlayFab")
    PLAY_FAB,
    @SerializedName("Custom")
    CUSTOM,
    @SerializedName("GameCenter")
    GAME_CENTER,
    @SerializedName("GooglePlay")
    GOOGLE_PLAY,
    @SerializedName("Steam")
    STEAM,
    @SerializedName("XBoxLive")
    XBOX_LIVE,
    PSN,
    @SerializedName("Kongregate")
    KONGREGATE,
    @SerializedName("Facebook")
    FACEBOOK,
    @SerializedName("IOSDevice")
    IOS_DEVICE,
    @SerializedName("AndroidDevice")
    ANDROID_DEVICE,
    @SerializedName("Twitch")
    TWITCH,
    @SerializedName("WindowsHello")
    WINDOWS_HELLO,
    @SerializedName("GameServer")
    GAME_SERVER,
    @SerializedName("CustomServer")
    CUSTOM_SERVER,
    @SerializedName("NintendoSwitch")
    NINTENDO_SWITCH,
    @SerializedName("FacebookInstantGames")
    FACEBOOK_INSTANT_GAMES,
    @SerializedName("OpenIdConnect")
    OPEN_ID_CONNECT,
    @SerializedName("Apple")
    APPLE,
    @SerializedName("NintendoSwitchAccount")
    NINTENDO_SWITCH_ACCOUNT,
    @SerializedName("GooglePlayGames")
    GOOGLE_PLAY_GAMES,
    @SerializedName("XboxMobileStore")
    XBOX_MOBILE_STORE,
    @SerializedName("King")
    KING,
    @SerializedName("BattleNet")
    BATTLE_NET
}