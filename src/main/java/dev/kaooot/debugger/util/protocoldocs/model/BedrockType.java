package dev.kaooot.debugger.util.protocoldocs.model;

import dev.kaooot.debugger.util.protocoldocs.model.property.BedrockProperty;
import com.google.gson.annotations.SerializedName;
import java.util.HashSet;
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

    public long getRefId() {
        return Long.parseLong(this.ref.split("#/definitions/")[1]);
    }
}