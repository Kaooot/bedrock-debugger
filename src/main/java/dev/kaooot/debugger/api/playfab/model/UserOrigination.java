package dev.kaooot.debugger.api.playfab.model;

import com.google.gson.annotations.SerializedName;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
public enum UserOrigination {

    @SerializedName("Organic")
    ORGANIC,
    @SerializedName("Steam")
    STEAM,
    @SerializedName("Google")
    GOOGLE,
    @SerializedName("Amazon")
    AMAZON,
    @SerializedName("Facebook")
    FACEBOOK,
    @SerializedName("Kongregate")
    KONGREGATE,
    @SerializedName("GamersFirst")
    GAMERS_FIRST,
    @SerializedName("Unknown")
    UNKNOWN,
    @SerializedName("Ios")
    IOS,
    @SerializedName("LoadTest")
    LOAD_TEST,
    @SerializedName("Android")
    ANDROID,
    PSN,
    @SerializedName("GameCenter")
    GAME_CENTER,
    @SerializedName("CustomId")
    CUSTOM_ID,
    @SerializedName("XboxLive")
    XBOX_LIVE,
    @SerializedName("Parse")
    PARSE,
    @SerializedName("Twitch")
    TWITCH,
    @SerializedName("ServerCustomId")
    SERVER_CUSTOM_ID,
    @SerializedName("NintendoSwitchDeviceId")
    NINTENDO_SWITCH_DEVICE_ID,
    @SerializedName("FacebookInstantGamesId")
    FACEBOOK_INSTANT_GAMES_ID,
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