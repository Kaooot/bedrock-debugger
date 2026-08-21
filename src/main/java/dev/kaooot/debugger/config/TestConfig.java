package dev.kaooot.debugger.config;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import dev.kaooot.debugger.api.config.Config;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class TestConfig extends Config {

    @SerializedName("packet_testing")
    private boolean packetTesting;
    @SerializedName("debug_server_path")
    private String debugServerPath = "";

    @Override
    public String getName() {
        return "test_config";
    }

    @Override
    public Config getDefaults() {
        return new TestConfig();
    }
}