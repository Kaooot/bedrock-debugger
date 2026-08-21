package dev.kaooot.debugger.api.playfab.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;
import lombok.Value;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@Value
public class GetPlayerCombinedInfoResultPayload {

    /**
     * Account information for the user. This is always retrieved.
     */
    @SerializedName("AccountInfo")
    UserAccountInfo accountInfo;
    /**
     * Inventories for each character for the user.
     */
    @SerializedName("CharacterInventories")
    List<CharacterInventory> characterInventories;
    /**
     * List of characters for the user.
     */
    @SerializedName("CharacterList")
    List<CharacterResult> characterList;
    /**
     * The profile of the players. This profile is not guaranteed to be up-to-date.
     * For a new player, this profile will not exist.
     */
    @SerializedName("PlayerProfile")
    PlayerProfileModel playerProfile;
    /**
     * List of statistics for this player.
     */
    @SerializedName("PlayerStatistics")
    List<StatisticValue> playerStatistics;
    /**
     * Title data for this title.
     */
    @SerializedName("TitleData")
    Map<String, String> titleData;
    /**
     * User specific custom data.
     */
    @SerializedName("UserData")
    Map<String, UserDataRecord> userData;
    /**
     * The version of the UserData that was returned.
     */
    @SerializedName("UserDataVersion")
    long userDataVersion;
    /**
     * Array of inventory items in the user's current inventory.
     */
    @SerializedName("UserInventory")
    List<ItemInstance> userInventory;
    /**
     * User specific read-only data.
     */
    @SerializedName("UserReadOnlyData")
    Map<String, UserDataRecord> userReadOnlyData;
    /**
     * The version of the Read-Only UserData that was returned.
     */
    @SerializedName("UserReadOnlyDataVersion")
    long UserReadOnlyDataVersion;
    /**
     * Dictionary of virtual currency balance(s) belonging to the user.
     */
    @SerializedName("UserVirtualCurrency")
    Map<String, Integer> userVirtualCurrency;
    /**
     * Dictionary of remaining times and timestamps for virtual currencies.
     */
    @SerializedName("UserVirtualCurrencyRechargeTimes")
    Map<String, VirtualCurrencyRechargeTime> userVirtualCurrencyRechargeTimes;
}