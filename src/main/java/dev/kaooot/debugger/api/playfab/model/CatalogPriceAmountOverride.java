package dev.kaooot.debugger.api.playfab.model;

import com.google.gson.annotations.SerializedName;
import lombok.Value;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@Value
public class CatalogPriceAmountOverride {

    @SerializedName("FixedValue")
    int fixedValue;
    @SerializedName("ItemId")
    String itemId;
    @SerializedName("Multiplier")
    double multiplier;
}