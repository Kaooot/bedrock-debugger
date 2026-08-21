package dev.kaooot.debugger.api.playfab.model;

import com.google.gson.annotations.SerializedName;
import lombok.Value;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@Value
public class CatalogItemReference {

    @SerializedName("Amount")
    int amount;
    @SerializedName("Id")
    String id;
    @SerializedName("PriceOptions")
    CatalogPriceOptions priceOptions;
}