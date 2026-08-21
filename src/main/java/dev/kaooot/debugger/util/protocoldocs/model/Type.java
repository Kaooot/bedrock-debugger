package dev.kaooot.debugger.util.protocoldocs.model;

import com.google.gson.annotations.SerializedName;
import lombok.RequiredArgsConstructor;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@RequiredArgsConstructor
public enum Type {

    @SerializedName("object")
    OBJECT("object"),
    @SerializedName("array")
    ARRAY("array"),
    @SerializedName("integer")
    INTEGER("integer"),
    @SerializedName("number")
    NUMBER("number"),
    @SerializedName("string")
    STRING("string"),
    @SerializedName("boolean")
    BOOLEAN("bool");

    private final String id;

    @Override
    public String toString() {
        return this.id;
    }
}