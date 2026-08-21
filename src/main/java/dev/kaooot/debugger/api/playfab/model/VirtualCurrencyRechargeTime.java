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
public class VirtualCurrencyRechargeTime {

    @SerializedName("RechargeMax")
    int rechargeMax;
    @SerializedName("RechargeTime")
    Date rechargeTime;
    @SerializedName("SecondsToRecharge")
    int secondsToRecharge;
}