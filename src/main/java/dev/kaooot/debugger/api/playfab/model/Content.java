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
public class Content {

    @SerializedName("Id")
    String id;
    @SerializedName("MaxClientVersion")
    String maxClientVersion;
    @SerializedName("MinClientVersion")
    String minClientVersion;
    @SerializedName("Tags")
    List<String> tags;
    @SerializedName("Type")
    String type;
    @SerializedName("Url")
    String url;
}