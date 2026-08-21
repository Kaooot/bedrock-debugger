package dev.kaooot.debugger.network.handler;

import org.cloudburstmc.protocol.bedrock.data.command.CommandOriginData;
import org.cloudburstmc.protocol.bedrock.data.command.CommandOriginType;
import org.cloudburstmc.protocol.bedrock.packet.CommandRequestPacket;
import org.cloudburstmc.protocol.common.PacketSignal;
import dev.kaooot.debugger.BedrockDebuggerProxy;
import dev.kaooot.debugger.api.command.Command;
import dev.kaooot.debugger.command.CommandRegistry;
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
public class CommandRequestHandler implements PacketHandler<CommandRequestPacket> {

    @Override
    public PacketSignal handle(CommandRequestPacket packet, BedrockDebuggerProxy proxy) {
        final CommandOriginType type = packet.getCommandOrigin().getCommandType();
        final boolean isDevConsoleOrigin = type.equals(CommandOriginType.DEV_CONSOLE);
        if (!type.equals(CommandOriginType.PLAYER) && !isDevConsoleOrigin) {
            return PacketSignal.UNHANDLED;
        }
        final Command command = Registries.<CommandRegistry>getRegistry(RegistryKey.COMMAND)
            .parseCommand(packet.getCommand());
        if (command != null) {
            command.execute(command.getName(), command.getArgs(), proxy);
            final SettingsConfig settingsConfig = Registries.
                <ConfigRegistry>getRegistry(RegistryKey.CONFIG)
                .get(SettingsConfig.class);
            if (settingsConfig.isPrintDebugInfo()) {
                if (command.getArgs().length > 0) {
                    proxy.getLogger().debug("Internal command execution: /{}, args: {}",
                        command.getName(), String.join(",", command.getArgs()));
                } else {
                    proxy.getLogger().debug("Internal command execution: /{}", command.getName());
                }
            }
            return PacketSignal.HANDLED;
        }
        proxy.getLogger().debug("Server command execution: {}", packet.getCommand());
        if (isDevConsoleOrigin) {
            final CommandOriginData data = packet.getCommandOrigin();
            packet.setCommandOrigin(new CommandOriginData(CommandOriginType.PLAYER,
                data.getCommandUUID(), data.getRequestID(), -1L));
            proxy.getClient().sendPacket(packet);
            return PacketSignal.HANDLED;
        }
        return PacketSignal.UNHANDLED;
    }
}