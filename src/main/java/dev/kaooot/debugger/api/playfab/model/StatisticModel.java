package dev.kaooot.debugger.api.playfab.model;

import com.google.gson.annotations.SerializedName;
import lombok.Value;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@Value
public class StatisticModel {

    @SerializedName("Name")
    String name;
    @SerializedName("Value")
    int value;
    @SerializedName("Version")
    int version;
}