package dev.kaooot.debugger.api.playfab.model;

import com.google.gson.annotations.SerializedName;
import lombok.Value;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@Value
public class UserPsnInfo {

    @SerializedName("PsnAccountId")
    String psnAccountId;
    @SerializedName("PsnOnlineId")
    String psnOnlineId;
}