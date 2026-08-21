package dev.kaooot.debugger.api.playfab.model;

import com.google.gson.annotations.SerializedName;
import lombok.Value;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@Value
public class LocationModel {

    @SerializedName("City")
    String city;
    @SerializedName("ContinentCode")
    ContinentCode continentCode;
    @SerializedName("CountryCode")
    CountryCode countryCode;
    @SerializedName("Latitude")
    double latitude;
    @SerializedName("Longitude")
    double longitude;
}