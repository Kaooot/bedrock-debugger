package dev.kaooot.debugger.network.handler;

import java.util.List;
import org.cloudburstmc.protocol.bedrock.data.command.CommandData;
import org.cloudburstmc.protocol.bedrock.packet.AvailableCommandsPacket;
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
public class AvailableCommandsHandler implements PacketHandler<AvailableCommandsPacket> {

    @Override
    public PacketSignal handle(AvailableCommandsPacket packet, BedrockDebuggerProxy proxy) {
        final List<CommandData> serverCommands = packet.getCommands();
        proxy.getPlayer().getServerCommands().clear();
        proxy.getPlayer().getServerCommands().addAll(serverCommands);
        proxy.getPlayer().sendAvailableCommands(
            serverCommands,
            Registries.<ConfigRegistry>getRegistry(RegistryKey.CONFIG)
                .get(SettingsConfig.class)
                .isDebugCommandsEnabled()
        );
        return PacketSignal.HANDLED;
    }
}