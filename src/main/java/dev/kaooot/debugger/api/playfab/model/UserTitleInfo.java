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
public class UserTitleInfo {

    @SerializedName("AvatarUrl")
    String avatarUrl;
    @SerializedName("Created")
    Date created;
    @SerializedName("DisplayName")
    String displayName;
    @SerializedName("FirstLogin")
    String firstLogin;
    @SerializedName("LastLogin")
    String lastLogin;
    @SerializedName("Origination")
    UserOrigination origination;
    @SerializedName("TitlePlayerAccount")
    EntityKey titlePlayerAccount;
    boolean isBanned;
}