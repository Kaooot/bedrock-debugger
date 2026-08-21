package dev.kaooot.debugger.api.playfab.model;

import com.google.gson.annotations.SerializedName;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
public enum SubscriptionProviderStatus {

    @SerializedName("NoError")
    NO_ERROR,
    @SerializedName("Cancelled")
    CANCELLED,
    @SerializedName("UnknownError")
    UNKNOWN_ERROR,
    @SerializedName("BillingError")
    BILLING_ERROR,
    @SerializedName("ProductUnavailable")
    PRODUCT_UNAVAILABLE,
    @SerializedName("CustomerDidNotAcceptPriceChange")
    CUSTOMER_DID_NOT_ACCEPT_PRICE_CHANGE,
    @SerializedName("FreeTrial")
    FREE_TRIAL,
    @SerializedName("PaymentPending")
    PAYMENT_PENDING
}