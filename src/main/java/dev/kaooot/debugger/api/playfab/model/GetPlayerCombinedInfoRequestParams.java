package dev.kaooot.debugger.api.playfab.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import lombok.Data;
import lombok.ToString;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@Data
@ToString
public class GetPlayerCombinedInfoRequestParams {

    @SerializedName("GetCharacterInventories")
    private boolean getCharacterInventories;
    @SerializedName("GetCharacterList")
    private boolean getCharacterList;
    @SerializedName("GetPlayerProfile")
    private boolean getPlayerProfile;
    @SerializedName("GetPlayerStatistics")
    private boolean getPlayerStatistics;
    @SerializedName("GetTitleData")
    private boolean getTitleData;
    @SerializedName("GetUserAccountInfo")
    private boolean getUserAccountInfo;
    @SerializedName("GetUserData")
    private boolean getUserData;
    @SerializedName("GetUserInventory")
    private boolean getUserInventory;
    @SerializedName("GetUserReadOnlyData")
    private boolean getUserReadOnlyData;
    @SerializedName("GetUserVirtualCurrency")
    private boolean getUserVirtualCurrency;
    @SerializedName("PlayerStatisticNames")
    private List<String> playerStatisticNames;
    @SerializedName("ProfileConstraints")
    private PlayerProfileViewConstraints profileConstraints;
    @SerializedName("TitleDataKeys")
    private List<String> titleDataKeys;
    @SerializedName("UserDataKeys")
    private List<String> userDataKeys;
    @SerializedName("UserReadOnlyDataKeys")
    private List<String> userReadOnlyDataKeys;
}