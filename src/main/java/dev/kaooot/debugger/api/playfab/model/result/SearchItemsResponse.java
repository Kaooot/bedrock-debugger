package dev.kaooot.debugger.api.playfab.model.result;

import com.google.gson.annotations.SerializedName;
import dev.kaooot.debugger.api.playfab.model.CatalogItem;
import java.util.List;
import lombok.Value;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@Value
public class SearchItemsResponse {

    @SerializedName("ContinuationToken")
    String continuationToken;
    @SerializedName("Items")
    List<CatalogItem> items;
}