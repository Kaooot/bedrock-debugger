package dev.kaooot.debugger.network.handler;

import dev.kaooot.debugger.BedrockDebuggerProxy;
import dev.kaooot.debugger.config.ConfigRegistry;
import dev.kaooot.debugger.config.SettingsConfig;
import dev.kaooot.debugger.core.registry.Registries;
import dev.kaooot.debugger.core.registry.RegistryKey;
import dev.kaooot.debugger.network.PacketHandler;
import dev.kaooot.debugger.util.Util;
import org.cloudburstmc.protocol.bedrock.data.PlatformType;
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket;
import org.cloudburstmc.protocol.common.PacketSignal;

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

        final SettingsConfig settingsConfig = Registries.
            <ConfigRegistry>getRegistry(RegistryKey.CONFIG)
            .get(SettingsConfig.class);
        final PlatformType platformType = settingsConfig.getPlatformType();

        packet.setInputMode(Util.getInputMode(platformType));
        packet.setPlayMode(Util.getClientPlayMode(platformType));
        packet.setNewInteractionModel(Util.getInputInteractionModel(platformType));

        if (proxy.getPlayer().isReadyToRoll() && settingsConfig.isRenderCurrentChunk()) {
            proxy.getPlayer().getChunkDebugRenderer()
                .updateChunkPosForRenderingIfEnabled(settingsConfig);
        }
        if (proxy.getPlayer().getCheatClientAuthority().handleNuker(packet)) {
            proxy.getClient().sendPacket(packet);
            return PacketSignal.HANDLED;
        }
        return PacketSignal.UNHANDLED;
    }
}