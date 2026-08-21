package dev.kaooot.debugger.config;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Value;
import dev.kaooot.debugger.api.config.Config;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class AccountsConfig extends Config {

    private final List<AccountDetails> accounts = new ObjectArrayList<>();

    @Override
    public String getName() {
        return "accounts";
    }

    @Override
    public Config getDefaults() {
        return new AccountsConfig();
    }

    @Value
    public static class AccountDetails {
        String name;
        String refreshToken;
    }
}