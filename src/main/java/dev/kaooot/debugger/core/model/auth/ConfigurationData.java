package dev.kaooot.debugger.core.model.auth;

import java.util.Map;
import lombok.Value;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@Value
public class ConfigurationData {

    String id;
    Map<String, String> parameters;
}