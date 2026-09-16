package dev.kaooot.debugger.config;

import dev.kaooot.debugger.api.config.Config;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

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

    @Data
    @AllArgsConstructor
    public static class AccountDetails {
        private String name;
        private  String refreshToken;
    }
}