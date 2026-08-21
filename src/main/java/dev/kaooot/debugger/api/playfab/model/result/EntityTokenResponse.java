package dev.kaooot.debugger.api.playfab.model.result;

import dev.kaooot.debugger.api.playfab.model.EntityKey;
import com.google.gson.annotations.SerializedName;
import java.util.Date;
import lombok.Value;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@Value
public class EntityTokenResponse {

    @SerializedName("Entity")
    EntityKey entity;
    @SerializedName("EntityToken")
    String entityToken;
    @SerializedName("TokenExpiration")
    Date tokenExpiration;
}