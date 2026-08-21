package dev.kaooot.debugger.network.handler;

import org.cloudburstmc.protocol.bedrock.packet.ClientCacheStatusPacket;
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
public class ClientCacheStatusHandler implements PacketHandler<ClientCacheStatusPacket> {

    @Override
    public PacketSignal handle(ClientCacheStatusPacket packet, BedrockDebuggerProxy proxy) {
        packet.setCacheSupported(
            Registries.<ConfigRegistry>getRegistry(RegistryKey.CONFIG)
                .get(SettingsConfig.class).isClientBlobCacheEnabled()
        );
        proxy.getClient().sendPacket(packet);
        return PacketSignal.HANDLED;
    }
}