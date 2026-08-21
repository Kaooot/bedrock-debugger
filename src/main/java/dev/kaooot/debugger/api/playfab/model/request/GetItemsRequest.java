package dev.kaooot.debugger.api.playfab.model.request;

import com.google.gson.annotations.SerializedName;
import dev.kaooot.debugger.api.playfab.model.CatalogAlternateId;
import dev.kaooot.debugger.api.playfab.model.EntityKey;
import java.util.List;
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
public class GetItemsRequest {

    @SerializedName("AlternateIds")
    private List<CatalogAlternateId> alternateIds;
    @SerializedName("CustomTags")
    private Map<String, String> customTags;
    @SerializedName("Entity")
    private EntityKey entity;
    @SerializedName("Ids")
    private List<String> ids;
}