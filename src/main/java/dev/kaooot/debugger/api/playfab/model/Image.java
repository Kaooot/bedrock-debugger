package dev.kaooot.debugger.api.playfab.model;

import com.google.gson.annotations.SerializedName;
import lombok.Value;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@Value
public class Image {

    @SerializedName("Id")
    String id;
    @SerializedName("Tag")
    String tag;
    @SerializedName("Type")
    String type;
    @SerializedName("Url")
    String url;
}