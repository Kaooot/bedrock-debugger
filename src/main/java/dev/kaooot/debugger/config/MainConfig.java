package dev.kaooot.debugger.config;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import dev.kaooot.debugger.api.config.Config;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class MainConfig extends Config {

    @SerializedName("remote_address")
    private String remoteAddress;
    @SerializedName("remote_port")
    private int remotePort;
    @SerializedName("proxy_address")
    private String proxyAddress;
    @SerializedName("proxy_port")
    private int proxyPort;
    @SerializedName("account_name")
    private String accountName;
    @SerializedName("connection_type")
    private ConnectionType connectionType = ConnectionType.DEFAULT;
    @SerializedName("experience_id")
    private String experienceId = "";

    public MainConfig() {

    }

    @Override
    public String getName() {
        return "config";
    }

    @Override
    public Config getDefaults() {
        return new MainConfig("127.0.0.1", 19132, "0.0.0.0", 19122, "", ConnectionType.DEFAULT, "");
    }

    public enum ConnectionType {
        DEFAULT,
        EXPERIENCE
    }
}