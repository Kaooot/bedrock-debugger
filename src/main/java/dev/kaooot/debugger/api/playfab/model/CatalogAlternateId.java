package dev.kaooot.debugger.api.playfab.model;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.ToString;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@Data
@ToString
public class CatalogAlternateId {

    @SerializedName("Type")
    private String type;
    @SerializedName("Value")
    private String value;
}