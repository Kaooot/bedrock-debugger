package dev.kaooot.debugger.api.playfab.model;

import com.google.gson.annotations.SerializedName;
import lombok.Value;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@Value
public class StatisticValue {

    @SerializedName("StatisticName")
    String statisticName;
    @SerializedName("Value")
    int value;
    @SerializedName("Version")
    long version;
}