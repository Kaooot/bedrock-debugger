package dev.kaooot.debugger.api.playfab.model.result;

import dev.kaooot.debugger.api.playfab.model.GetPlayerCombinedInfoResultPayload;
import dev.kaooot.debugger.api.playfab.model.TreatmentAssignment;
import dev.kaooot.debugger.api.playfab.model.UserSettings;
import com.google.gson.annotations.SerializedName;
import java.util.Date;
import lombok.Value;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@Value
public class LoginResult {

    @SerializedName("EntityToken")
    EntityTokenResponse entityToken;
    @SerializedName("InfoResultPayload")
    GetPlayerCombinedInfoResultPayload infoResultPayload;
    @SerializedName("LastLoginTime")
    Date lastLoginTime;
    @SerializedName("NewlyCreated")
    boolean newlyCreated;
    @SerializedName("PlayFabId")
    String playFabId;
    @SerializedName("SessionTicket")
    String sessionTicket;
    @SerializedName("SettingsForUser")
    UserSettings settingsForUser;
    @SerializedName("TreatmentAssignment")
    TreatmentAssignment treatmentAssignment;
}