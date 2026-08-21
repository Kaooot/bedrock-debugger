package dev.kaooot.debugger.api.playfab.model;

import com.google.gson.annotations.SerializedName;
import lombok.Value;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@Value
public class Rating {

    @SerializedName("Average")
    float average;
    @SerializedName("Count1Star")
    int count1Star;
    @SerializedName("Count2Star")
    int count2Star;
    @SerializedName("Count3Star")
    int count3Star;
    @SerializedName("Count4Star")
    int count4Star;
    @SerializedName("Count5Star")
    int count5Star;
    @SerializedName("TotalCount")
    int totalCount;
}