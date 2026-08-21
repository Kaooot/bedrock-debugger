package dev.kaooot.debugger.api.playfab.model.request;

import dev.kaooot.debugger.api.playfab.model.GetPlayerCombinedInfoRequestParams;
import com.google.gson.annotations.SerializedName;
import java.util.Map;
import lombok.Data;
import lombok.ToString;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@Data
@ToString
public class LoginWithXboxRequest {

    @SerializedName("TitleId")
    private String titleId;
    @SerializedName("CreateAccount")
    private boolean createAccount;
    @SerializedName("CustomTags")
    private Map<String, String> customTags;
    @SerializedName("EncryptedRequest")
    private String encryptedRequest;
    @SerializedName("InfoRequestParameters")
    private GetPlayerCombinedInfoRequestParams infoRequestParameters;
    @SerializedName("PlayerSecret")
    private String playerSecret;
    @SerializedName("XboxToken")
    private String xboxToken;
}