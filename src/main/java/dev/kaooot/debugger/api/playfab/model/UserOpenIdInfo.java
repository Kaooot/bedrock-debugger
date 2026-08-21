package dev.kaooot.debugger.api.playfab.model;

import com.google.gson.annotations.SerializedName;
import lombok.Value;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@Value
public class UserOpenIdInfo {

    @SerializedName("ConnectionId")
    String connectionId;
    @SerializedName("Issuer")
    String issuer;
    @SerializedName("Subject")
    String subject;
}