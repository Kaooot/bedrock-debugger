package dev.kaooot.debugger.api.playfab.model;

import com.google.gson.annotations.SerializedName;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
public enum PushNotificationPlatform {

    @SerializedName("ApplePushNotificationService")
    APPLE_PUSH_NOTIFICATION_SERVICE,
    @SerializedName("GoogleCloudMessaging")
    GOOGLE_CLOUD_MESSAGING
}