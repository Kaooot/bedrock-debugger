package dev.kaooot.debugger.api.playfab.model.request;

import dev.kaooot.debugger.api.playfab.model.EntityKey;
import dev.kaooot.debugger.api.playfab.model.StoreReference;
import com.google.gson.annotations.SerializedName;
import java.util.Map;
import lombok.Data;
import lombok.ToString;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@Data
@ToString
public class SearchItemsRequest {

    @SerializedName("Count")
    private int count;
    @SerializedName("ContinuationToken")
    private String continuationToken;
    @SerializedName("CustomTags")
    private Map<String, String> customTags;
    @SerializedName("Entity")
    private EntityKey entity;
    @SerializedName("Filter")
    private String filter;
    @SerializedName("Language")
    private String language;
    @SerializedName("OrderBy")
    private String orderBy;
    @SerializedName("Search")
    private String search;
    @SerializedName("Select")
    private String select;
    @SerializedName("Store")
    private StoreReference store;
}