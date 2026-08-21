package dev.kaooot.debugger.api.playfab.model;

import com.google.gson.annotations.SerializedName;
import java.util.Date;
import lombok.Value;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@Value
public class ModerationState {

    @SerializedName("LastModifiedDate")
    Date lastModifiedDate;
    @SerializedName("Reason")
    String reason;
    @SerializedName("Status")
    ModerationStatus status;
}