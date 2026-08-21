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
public class ItemInstance {

    @SerializedName("Annotation")
    String annotation;
    @SerializedName("BundleContents")
    List<String> bundleContents;
    @SerializedName("BundleParent")
    String bundleParent;
    @SerializedName("CatalogVersion")
    String catalogVersion;
    @SerializedName("CustomData")
    Map<String, String> customData;
    @SerializedName("DisplayName")
    String displayName;
    @SerializedName("Expiration")
    String expiration;
    @SerializedName("ItemClass")
    String itemClass;
    @SerializedName("ItemId")
    String itemId;
    @SerializedName("ItemInstanceId")
    String itemInstanceId;
    @SerializedName("purchaseDate")
    String purchaseDate;
    @SerializedName("RemainingUses")
    int remainingUses;
    @SerializedName("UnitCurrency")
    String unitCurrency;
    @SerializedName("UnitPrice")
    long unitPrice;
    @SerializedName("UsesIncrementedBy")
    int usesIncrementedBy;
}