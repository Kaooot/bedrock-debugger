package dev.kaooot.debugger.network.handler;

import org.cloudburstmc.protocol.bedrock.packet.SetLocalPlayerAsInitializedPacket;
import org.cloudburstmc.protocol.common.PacketSignal;
import dev.kaooot.debugger.BedrockDebuggerProxy;
import dev.kaooot.debugger.config.ConfigRegistry;
import dev.kaooot.debugger.config.SettingsConfig;
import dev.kaooot.debugger.core.registry.Registries;
import dev.kaooot.debugger.core.registry.RegistryKey;
import dev.kaooot.debugger.network.PacketHandler;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
public class SetLocalPlayerAsInitializedHandler
    implements PacketHandler<SetLocalPlayerAsInitializedPacket> {

    @Override
    public PacketSignal handle(SetLocalPlayerAsInitializedPacket packet, BedrockDebuggerProxy proxy) {
        proxy.getPlayer().setReadyToRoll(true);
        proxy.getLogger().info("READY TO ROLL");
        final SettingsConfig settingsConfig = Registries.
            <ConfigRegistry>getRegistry(RegistryKey.CONFIG)
            .get(SettingsConfig.class);
        if (settingsConfig.isPlayerDebugRendererEnabled()) {
            proxy.getPlayer().asServerPlayer().renderBounds(settingsConfig);
        }
        return PacketSignal.UNHANDLED;
    }
}