package dev.kaooot.debugger.network.handler;

import org.cloudburstmc.protocol.bedrock.data.ClientPlayMode;
import org.cloudburstmc.protocol.bedrock.data.InputInteractionModel;
import org.cloudburstmc.protocol.bedrock.data.InputMode;
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket;
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
public class PlayerAuthInputHandler implements PacketHandler<PlayerAuthInputPacket> {

    @Override
    public PacketSignal handle(PlayerAuthInputPacket packet, BedrockDebuggerProxy proxy) {
        if (proxy.isTransferring() || proxy.getPlayers().isEmpty() || proxy.getPlayer() == null) {
            return PacketSignal.HANDLED;
        }

        proxy.getPlayer().setPosition(packet.getPosition());
        proxy.getPlayer().setRotation(packet.getPlayerRotation());

        packet.setInputMode(InputMode.TOUCH);
        packet.setPlayMode(ClientPlayMode.NORMAL);
        packet.setNewInteractionModel(InputInteractionModel.TOUCH);

        final SettingsConfig settingsConfig = Registries.
            <ConfigRegistry>getRegistry(RegistryKey.CONFIG)
            .get(SettingsConfig.class);

        if (proxy.getPlayer().isReadyToRoll() && settingsConfig.isRenderCurrentChunk()) {
            proxy.getPlayer().updateChunkPosForRenderingIfEnabled(settingsConfig);
        }
        if (proxy.getPlayer().getCheatClientAuthority().handleNuker(packet)) {
            proxy.getClient().sendPacket(packet);
            return PacketSignal.HANDLED;
        }
        return PacketSignal.UNHANDLED;
    }
}