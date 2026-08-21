package dev.kaooot.debugger.api.playfab.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import lombok.Value;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@Value
public class CatalogPriceOverride {

    @SerializedName("Amounts")
    List<CatalogPriceAmountOverride> amounts;
}