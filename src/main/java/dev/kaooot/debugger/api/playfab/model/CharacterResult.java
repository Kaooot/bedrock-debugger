package dev.kaooot.debugger.api.playfab.model;

import com.google.gson.annotations.SerializedName;
import lombok.Value;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@Value
public class CharacterResult {

    @SerializedName("CharacterId")
    String characterId;
    @SerializedName("CharacterName")
    String characterName;
    @SerializedName("CharacterType")
    String characterType;
}