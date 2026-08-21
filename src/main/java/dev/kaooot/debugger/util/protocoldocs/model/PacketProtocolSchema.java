package dev.kaooot.debugger.util.protocoldocs.model;

import dev.kaooot.debugger.util.protocoldocs.model.property.BedrockProperty;
import com.google.gson.annotations.SerializedName;
import java.util.Map;
import java.util.Set;
import lombok.Value;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@Value
public class PacketProtocolSchema {

    @SerializedName("$id")
    Long id;
    @SerializedName("x-format-version")
    String formatVersion;
    @SerializedName("x-minecraft-version")
    String minecraftVersion;
    @SerializedName("x-protocol-version")
    int protocolVersion;
    Map<Long, BedrockType> definitions;
    String title;
    String description;
    Type type;
    Map<String, BedrockProperty> properties;
    Set<String> required;
    @SerializedName("$metaProperties")
    MetaProperties metaProperties;
}