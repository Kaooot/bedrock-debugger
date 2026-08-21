package dev.kaooot.debugger.api.playfab.model;

import com.google.gson.annotations.SerializedName;
import lombok.Value;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@Value
public class UserGooglePlayGamesInfo {

    @SerializedName("GooglePlayGamesPlayerAvatarImageUrl")
    String googlePlayGamesPlayerAvatarImageUrl;
    @SerializedName("GooglePlayGamesPlayerDisplayName")
    String googlePlayGamesPlayerDisplayName;
    @SerializedName("GooglePlayGamesPlayerId")
    String googlePlayGamesPlayerId;
}