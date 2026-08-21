package dev.kaooot.debugger.network.handler;

import org.cloudburstmc.protocol.bedrock.data.actor.ActorDataTypes;
import org.cloudburstmc.protocol.bedrock.packet.AddPlayerPacket;
import org.cloudburstmc.protocol.common.PacketSignal;
import dev.kaooot.debugger.BedrockDebuggerProxy;
import dev.kaooot.debugger.config.ConfigRegistry;
import dev.kaooot.debugger.config.SettingsConfig;
import dev.kaooot.debugger.core.registry.Registries;
import dev.kaooot.debugger.core.registry.RegistryKey;
import dev.kaooot.debugger.network.PacketHandler;
import dev.kaooot.debugger.player.ServerPlayer;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
public class AddPlayerHandler implements PacketHandler<AddPlayerPacket> {

    @Override
    public PacketSignal handle(AddPlayerPacket packet, BedrockDebuggerProxy proxy) {
        if (packet.getPlayerName().isEmpty()) {
            return PacketSignal.UNHANDLED;
        }

        final ServerPlayer player = new ServerPlayer(packet.getTargetActorID(),
            packet.getTargetRuntimeID(), proxy);
        player.setPosition(packet.getPosition());
        final SettingsConfig settingsConfig = Registries.
            <ConfigRegistry>getRegistry(RegistryKey.CONFIG)
            .get(SettingsConfig.class);

        if (proxy.getPlayer().isReadyToRoll() && settingsConfig.isPlayerDebugRendererEnabled()) {
            player.renderBounds(settingsConfig);
        }

        if (packet.getActorData().containsKey(ActorDataTypes.NAME)) {
            player.setNameTag(packet.getActorData().get(ActorDataTypes.NAME));
        }

        proxy.getPlayers().add(player);
        return PacketSignal.UNHANDLED;
    }
}