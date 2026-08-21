package dev.kaooot.debugger.api.playfab.model;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@Data
public class EntityLineage {

    @SerializedName("CharacterId")
    private String characterId;
    @SerializedName("GroupId")
    private String groupId;
    @SerializedName("MasterPlayerAccountId")
    private String masterPlayerAccountId;
    @SerializedName("NamespaceId")
    private String namespaceId;
    @SerializedName("TitleId")
    private String titleId;
    @SerializedName("TitlePlayerAccountId")
    private String titlePlayerAccountId;
}