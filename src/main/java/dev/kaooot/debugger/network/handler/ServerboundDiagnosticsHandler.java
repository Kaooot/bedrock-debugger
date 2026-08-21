package dev.kaooot.debugger.network.handler;

import org.cloudburstmc.protocol.bedrock.packet.ServerboundDiagnosticsPacket;
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
public class ServerboundDiagnosticsHandler implements PacketHandler<ServerboundDiagnosticsPacket> {

    @Override
    public PacketSignal handle(ServerboundDiagnosticsPacket packet, BedrockDebuggerProxy proxy) {
        if (!proxy.getPlayer().isDiagnosticsEnabled()) {
            proxy.getPlayer().setDiagnosticsEnabled(true);
        }
        proxy.getPlayer().setDiagnostics(packet);

        if (!proxy.isLoadPacks()) {
            return PacketSignal.UNHANDLED;
        }

        final SettingsConfig settingsConfig = Registries.
            <ConfigRegistry>getRegistry(RegistryKey.CONFIG)
                .get(SettingsConfig.class);

        proxy.getServer().sendBuildInfo(settingsConfig.isRenderBuildInfo() ?
            proxy.getDebugScreenInfo().getBuildInfo(settingsConfig) : "");
        return PacketSignal.UNHANDLED;
    }
}