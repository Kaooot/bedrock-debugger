package dev.kaooot.debugger.network.handler;

import com.google.gson.JsonObject;
import com.nimbusds.jose.JWSObject;
import java.security.KeyPair;
import java.security.interfaces.ECPublicKey;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import org.cloudburstmc.protocol.bedrock.data.BuildPlatform;
import org.cloudburstmc.protocol.bedrock.data.GraphicsMode;
import org.cloudburstmc.protocol.bedrock.data.InputMode;
import org.cloudburstmc.protocol.bedrock.data.MemoryTier;
import org.cloudburstmc.protocol.bedrock.data.PlatformType;
import org.cloudburstmc.protocol.bedrock.data.UserInterfaceProfile;
import org.cloudburstmc.protocol.bedrock.data.auth.PlayerAuthenticationType;
import org.cloudburstmc.protocol.bedrock.data.definitions.ItemDefinition;
import org.cloudburstmc.protocol.bedrock.data.definitions.SimpleItemDefinition;
import org.cloudburstmc.protocol.bedrock.packet.LoginPacket;
import org.cloudburstmc.protocol.common.DefinitionRegistry;
import org.cloudburstmc.protocol.common.PacketSignal;
import org.cloudburstmc.protocol.common.SimpleDefinitionRegistry;
import dev.kaooot.debugger.BedrockDebuggerProxy;
import dev.kaooot.debugger.api.auth.util.AuthExtraData;
import dev.kaooot.debugger.api.auth.util.ForgeryUtil;
import dev.kaooot.debugger.config.ConfigRegistry;
import dev.kaooot.debugger.config.SettingsConfig;
import dev.kaooot.debugger.core.registry.Registries;
import dev.kaooot.debugger.core.registry.RegistryKey;
import dev.kaooot.debugger.core.service.AuthServiceConnection;
import dev.kaooot.debugger.network.NetworkConstants;
import dev.kaooot.debugger.network.PacketHandler;
import dev.kaooot.debugger.player.ProxiedPlayer;
import dev.kaooot.debugger.player.login.LoginData;
import dev.kaooot.debugger.util.BedrockGameVersion;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
public class LoginHandler implements PacketHandler<LoginPacket> {

    @Override
    public PacketSignal handle(LoginPacket packet, BedrockDebuggerProxy proxy) {
        final KeyPair keyPair = proxy.getKeyPair();
        final JsonObject loginChain = proxy.getMsaAuth().generateLoginChain(
            ((ECPublicKey) keyPair.getPublic()),
            NetworkConstants.CODEC.getMinecraftVersion()
        );
        final AuthExtraData authExtraData = AuthExtraData.fromChain(loginChain);

        try {
            final Map<String, Object> map = JWSObject.parse(packet.getClientJwt()).getPayload()
                .toJSONObject();
            final String gameVersion = map.get("GameVersion").toString();
            final boolean isPreview = gameVersion.split("\\.").length > 3;
            final LoginData loginData = new LoginData(
                BedrockGameVersion.from(gameVersion),
                packet.getClientNetworkVersion(),
                isPreview,
                map
            );

            proxy.getAuthServiceConnection().startSession(loginData);
            proxy.getAuthServiceConnection().startMultiplayerSession();

            final AuthServiceConnection.PlayFabLoginData pfLoginData =
                proxy.getAuthServiceConnection().getPlayFabLoginData();

            final SettingsConfig settingsConfig = Registries.
                <ConfigRegistry>getRegistry(RegistryKey.CONFIG)
                .get(SettingsConfig.class);

            final String address = proxy.getRemoteAddress() + ":" + proxy.getRemotePort();
            final Map<String, Object> modifiedExtra = new HashMap<>();
            modifiedExtra.put("DeviceOS", BuildPlatform.GOOGLE.getId());
            modifiedExtra.put("DefaultInputMode", InputMode.TOUCH.ordinal());
            modifiedExtra.put("CurrentInputMode", InputMode.TOUCH.ordinal());
            modifiedExtra.put("MemoryTier", MemoryTier.LOW.ordinal());
            modifiedExtra.put("PlatformType", PlatformType.MOBILE.ordinal());
            modifiedExtra.put("UIProfile", UserInterfaceProfile.CLASSIC.ordinal());
            modifiedExtra.put("DeviceModel", "");

            if (settingsConfig.isForceEnablePersonaSkins()) {
                modifiedExtra.put("PersonaSkin", false);
            }

            modifiedExtra.put(
                "DeviceId",
                settingsConfig.isShouldSpoofDeviceId() ? "" : pfLoginData.getDeviceId().toString()
            );
            modifiedExtra.put("ServerAddress", address);
            modifiedExtra.put("ThirdPartyName", authExtraData.getDisplayName());

            packet.setClientJwt(
                ForgeryUtil.forgeSkin(keyPair, packet.getClientJwt(), modifiedExtra)
            );

            this.updateToken(proxy, packet, authExtraData);

            if (settingsConfig.isOverrideClientNetworkVersion()) {
                packet.setClientNetworkVersion(NetworkConstants.CODEC.getProtocolVersion());
            }

            proxy.getClient().sendPacket(packet);
            proxy.setPlayer(new ProxiedPlayer(proxy, authExtraData, loginData));
            proxy.getPlayer().setGraphicsMode(
                GraphicsMode.from(
                    ((Number) map.get("GraphicsMode")).intValue()
                )
            );

            final DefinitionRegistry<ItemDefinition> itemDefinitionRegistry =
                new SimpleDefinitionRegistry.Builder<ItemDefinition>()
                    .add(new SimpleItemDefinition("minecraft:empty", 0, false))
                    .build();

            proxy.getClient().setBlockDefinitions(proxy.getBlockDefinitionRegistry());
            proxy.getServer().setBlockDefinitions(proxy.getBlockDefinitionRegistry());
            proxy.getClient().setItemDefinitions(itemDefinitionRegistry);
            proxy.getServer().setItemDefinitions(itemDefinitionRegistry);

            if (!proxy.isTransferring() && settingsConfig.isEnableExperimentalImGui()) {
                proxy.getImGuiAdapter().init();
            }
        } catch (ParseException e) {
            throw new RuntimeException(e.getMessage());
        }
        return PacketSignal.HANDLED;
    }

    private void updateToken(BedrockDebuggerProxy proxy, LoginPacket packet,
                             AuthExtraData authExtraData) {
        proxy.getLogger().info(
            "Logging in as {} (Xuid: {}, TitleID: {})",
            authExtraData.getDisplayName(),
            authExtraData.getXuid(),
            authExtraData.getTitleId()
        );
        if (!packet.getAuthenticationType().equals(PlayerAuthenticationType.FULL)) {
            throw new IllegalStateException(
                "Received invalid PlayerAuthenticationType: " +
                    packet.getAuthenticationType().name()
            );
        }
        if (packet.getToken() != null) {
            packet.setToken(
                proxy.getAuthServiceConnection()
                    .getMultiplayerResult()
                    .getResult()
                    .getSignedToken()
            );
        }
    }
}