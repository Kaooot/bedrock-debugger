package dev.kaooot.debugger.api.playfab.model;

import com.google.gson.annotations.SerializedName;
import lombok.Value;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@Value
public class ValueToDateModel {

    @SerializedName("Currency")
    String currency;
    @SerializedName("TotalValue")
    long totalValue;
    @SerializedName("TotalValueAsDecimal")
    String totalValueAsDecimal;
}