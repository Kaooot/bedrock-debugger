package dev.kaooot.debugger.api.playfab.model;

import com.google.gson.annotations.SerializedName;
import java.util.Map;
import lombok.Value;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@Value
public class RealMoneyPriceDetails {

    @SerializedName("AppleAppStorePrices")
    Map<String, Integer> appleAppStorePrices;
    @SerializedName("GooglePlayPrices")
    Map<String, Integer> googlePlayPrices;
    @SerializedName("MicrosoftStorePrices")
    Map<String, Integer> microsoftStorePrices;
    @SerializedName("NintendoEShopPrices")
    Map<String, Integer> nintendoEShopPrices;
    @SerializedName("PlayStationStorePrices")
    Map<String, Integer> playStationStorePrices;
    @SerializedName("SteamPrices")
    Map<String, Integer> steamPrices;
}