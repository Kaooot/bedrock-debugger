package dev.kaooot.debugger.api.playfab.model;

import com.google.gson.annotations.SerializedName;
import java.util.Date;
import java.util.List;
import java.util.Map;
import lombok.Value;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@Value
public class CatalogItem {

    @SerializedName("AlternateIds")
    List<CatalogAlternateId> alternateIds;
    @SerializedName("ContentType")
    String contentType;
    @SerializedName("Content")
    Content content;
    @SerializedName("CreationDate")
    Date creationDate;
    @SerializedName("CreatorEntity")
    EntityKey creatorEntity;
    @SerializedName("DeepLinks")
    List<DeepLink> deepLinks;
    @SerializedName("DefaultStackId")
    String defaultStackId;
    @SerializedName("Description")
    Map<String, String> description;
    @SerializedName("DisplayProperties")
    Object displayProperties;
    @SerializedName("DisplayVersion")
    String displayVersion;
    @SerializedName("ETag")
    String eTag;
    @SerializedName("EndDate")
    Date endDate;
    @SerializedName("Id")
    String id;
    @SerializedName("Images")
    List<Image> images;
    @SerializedName("IsHidden")
    boolean isHidden;
    @SerializedName("ItemReferences")
    List<CatalogItemReference> itemReferences;
    @SerializedName("Keywords")
    KeywordSet keywords;
    @SerializedName("LastModifiedDate")
    Date lastModifiedDate;
    @SerializedName("Moderation")
    ModerationStatus moderation;
    @SerializedName("Platforms")
    List<String> platforms;
    @SerializedName("PriceOptions")
    CatalogPriceOptions priceOptions;
    @SerializedName("Rating")
    Rating rating;
    @SerializedName("RealMoneyPriceDetails")
    RealMoneyPriceDetails realMoneyPriceDetails;
    @SerializedName("StartDate")
    Date startDate;
    @SerializedName("StoreDetails")
    StoreDetails storeDetails;
    @SerializedName("Tags")
    List<String> tags;
    @SerializedName("Title")
    Map<String, String> title;
    @SerializedName("Type")
    String type;
}