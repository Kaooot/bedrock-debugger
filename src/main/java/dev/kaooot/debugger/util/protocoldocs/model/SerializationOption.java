package dev.kaooot.debugger.util.protocoldocs.model;

import com.google.gson.annotations.SerializedName;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
public enum SerializationOption {

    @SerializedName("Compression")
    COMPRESSION,
    @SerializedName("NoSizeCompression")
    NO_SIZE_COMPRESSION,
    @SerializedName("BigEndian")
    BIG_ENDIAN,
    @SerializedName(value = "EnumAsValue", alternate = "Enum-as-Value")
    ENUM_AS_VALUE
}