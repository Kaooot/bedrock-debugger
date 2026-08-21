package dev.kaooot.debugger.util.protocoldocs.model;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@Getter
@RequiredArgsConstructor
public enum ValueType {

    @SerializedName(value = "bool", alternate = {"boolean"})
    BOOL("bool"),
    @SerializedName(value = "int8", alternate = {"byte", "char"})
    BYTE("int8"),
    @SerializedName(value = "uint8", alternate = {"unsigned byte", "unsigned char"})
    UNSIGNED_BYTE("uint8"),
    @SerializedName(value = "int16", alternate = "short")
    SHORT("int16"),
    @SerializedName(value = "uint16", alternate = "unsinged short")
    UNSIGNED_SHORT("uint16"),
    @SerializedName("int24")
    INT24("int24"),
    @SerializedName("uint24")
    UNSIGNED_INT24("uint24"),
    @SerializedName("int32")
    INT("int32"),
    @SerializedName(value = "uint32", alternate = "unsigned int")
    UNSIGNED_INT("uint32"),
    @SerializedName("float")
    FLOAT("float"),
    @SerializedName("double")
    DOUBLE("double"),
    @SerializedName("int64")
    LONG("int64"),
    @SerializedName(value = "uint64", alternate = "unsigned int64")
    UNSIGNED_LONG("uint64");

    private final String id;

    @Override
    public String toString() {
        return this.id;
    }
}