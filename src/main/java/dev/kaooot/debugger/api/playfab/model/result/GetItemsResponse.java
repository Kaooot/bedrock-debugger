package dev.kaooot.debugger.api.playfab.model.result;

import dev.kaooot.debugger.api.playfab.model.CatalogItem;
import com.google.gson.annotations.SerializedName;
import java.util.List;
import lombok.Value;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@Value
public class GetItemsResponse {

    @SerializedName("Items")
    List<CatalogItem> items;
}