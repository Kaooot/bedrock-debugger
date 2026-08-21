package dev.kaooot.debugger.api.auth.util;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import java.security.interfaces.ECPublicKey;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import dev.kaooot.debugger.BedrockDebuggerProxy;
import dev.kaooot.debugger.api.auth.request.AccessTokenRefreshRequest;
import dev.kaooot.debugger.api.auth.request.DeviceCodeFinishedRequest;
import dev.kaooot.debugger.api.auth.request.DeviceCodeRequest;
import dev.kaooot.debugger.api.auth.request.DeviceTokenAuthRequest;
import dev.kaooot.debugger.api.auth.request.MinecraftLoginChainRequest;
import dev.kaooot.debugger.api.auth.request.XBLTokenAuthRequest;
import dev.kaooot.debugger.config.AccountsConfig;
import dev.kaooot.debugger.config.ConfigRegistry;
import dev.kaooot.debugger.config.MainConfig;
import dev.kaooot.debugger.core.registry.Registries;
import dev.kaooot.debugger.core.registry.RegistryKey;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@RequiredArgsConstructor
public class MsaAuth {

    @Getter
    private String accessToken;
    private DeviceTokenAuthRequest.Result deviceTokenResult;
    private XBLTokenAuthRequest.Result xblTokenResult;

    private final BedrockDebuggerProxy proxy;

    public void doPrompt(AccountsConfig config, MainConfig mainConfig) {
        if (mainConfig.getAccountName() == null || mainConfig.getAccountName().isEmpty()) {
            final DeviceCodeRequest.Result result = DeviceCodeRequest.builder()
                .build()
                .make();

            this.proxy.getLogger().info("Open https://microsoft.com/link and paste code: {}",
                result.getUserCode());

            final DeviceCodeFinishedRequest request = DeviceCodeFinishedRequest.builder()
                .deviceCode(result.getDeviceCode())
                .build();

            DeviceCodeFinishedRequest.Result finishedResult;

            do {
                try {
                    Thread.sleep(5000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } while (!(finishedResult = request.make()).isFinished());

            this.accessToken = finishedResult.getAccessToken();
            this.addDefaultAccount(config, mainConfig, finishedResult.getRefreshToken());
        } else {
            String refreshToken = null;
            for (final AccountsConfig.AccountDetails account : config.getAccounts()) {
                if (account.getName().equalsIgnoreCase(mainConfig.getAccountName())) {
                    refreshToken = account.getRefreshToken();
                }
            }
            if (refreshToken == null) {
                throw new IllegalStateException(
                    "The provided account name was not found in the accounts configuration"
                );
            }

            final AccessTokenRefreshRequest.Result result = AccessTokenRefreshRequest.builder()
                .refreshToken(refreshToken)
                .build()
                .make();

            this.accessToken = result.getAccessToken();
            this.proxy.getLogger().info("Authentication using stored refresh token was successful");
        }
    }

    public JsonObject generateLoginChain(ECPublicKey publicKey, String clientVersion) {
        Preconditions.checkNotNull(this.accessToken);

        if (this.deviceTokenResult == null) {
            this.deviceTokenResult = DeviceTokenAuthRequest.builder()
                .build()
                .make();
        }

        if (this.xblTokenResult == null) {
            this.xblTokenResult = XBLTokenAuthRequest.builder()
                .accessToken(this.accessToken)
                .relyingParty("https://multiplayer.minecraft.net/")
                .result(this.deviceTokenResult)
                .build()
                .make();
        }
        return MinecraftLoginChainRequest.builder()
            .publicKey(publicKey)
            .clientVersion(clientVersion)
            .result(this.xblTokenResult)
            .build()
            .make();
    }

    private void addDefaultAccount(AccountsConfig config, MainConfig mainConfig,
                                   String refreshToken) {
        final String defaultAccountName = "default";
        config.getAccounts().add(new AccountsConfig.AccountDetails(
                defaultAccountName, refreshToken
            )
        );
        mainConfig.setAccountName(defaultAccountName);

        final ConfigRegistry registry = Registries.getRegistry(RegistryKey.CONFIG);
        registry.save(config);
        registry.save(mainConfig);

        this.proxy.getLogger().info(
            "Added the \"" + defaultAccountName + "\" account to the accounts configuration."
        );
    }
}