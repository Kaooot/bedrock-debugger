package dev.kaooot.debugger.util.protocoldocs.model;

import com.google.gson.annotations.SerializedName;
import lombok.Value;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@Value
public class MetaProperties {

    @SerializedName("[cereal:packet]")
    int packetId;
    @SerializedName("[cereal:packet_details]")
    String packetDetails;
}