package dev.kaooot.debugger.util.protocoldocs.model;

import dev.kaooot.debugger.util.protocoldocs.model.property.BedrockProperty;
import com.google.gson.annotations.SerializedName;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Data;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@Data
public class BedrockType {

    private String title;
    private String description;
    private Type type;
    private Map<String, BedrockProperty> properties;
    private Set<String> required = new HashSet<>();
    @SerializedName("$ref")
    private String ref;
    @SerializedName("enum")
    private List<String> enumValues;
    @SerializedName("x-enum-binary-value")
    private List<Long> enumBinaryValues;
    @SerializedName("x-underlying-type")
    private ValueType underlyingType;
    @SerializedName("x-protocol-version")
    private int protocolVersion;
    @SerializedName("$metaProperties")
    private MetaProperties metaProperties;

    public String getRefKey() {
        if (this.ref == null) {
            return null;
        }
        final int slash = this.ref.lastIndexOf('/');
        return slash >= 0 ? this.ref.substring(slash + 1) : this.ref;
    }

    public boolean isPacket() {
        return this.metaProperties != null;
    }

    public boolean isEnum() {
        return this.enumValues != null;
    }
}
