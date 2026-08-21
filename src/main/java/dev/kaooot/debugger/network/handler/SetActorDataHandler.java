package dev.kaooot.debugger.network.handler;

import org.cloudburstmc.protocol.bedrock.data.actor.ActorDataTypes;
import org.cloudburstmc.protocol.bedrock.packet.SetActorDataPacket;
import org.cloudburstmc.protocol.common.PacketSignal;
import dev.kaooot.debugger.BedrockDebuggerProxy;
import dev.kaooot.debugger.actor.Actor;
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
public class SetActorDataHandler implements PacketHandler<SetActorDataPacket> {

    @Override
    public PacketSignal handle(SetActorDataPacket packet, BedrockDebuggerProxy proxy) {
        final SettingsConfig settingsConfig = Registries.
            <ConfigRegistry>getRegistry(RegistryKey.CONFIG)
            .get(SettingsConfig.class);

        if (packet.getActorData().containsKey(ActorDataTypes.NAME)) {
            for (final ServerPlayer player : proxy.getPlayers()) {
                if (player.getRuntimeId() == packet.getTargetRuntimeID()) {
                    player.setNameTag(packet.getActorData().get(ActorDataTypes.NAME));
                    break;
                }
            }
        }

        for (final Actor actor : proxy.getActors()) {
            if (actor.getRuntimeId() == packet.getTargetRuntimeID()) {
                actor.getMetadata().putAll(packet.getActorData());
                if (proxy.getPlayer().isReadyToRoll() &&
                    settingsConfig.isActorDebugRendererEnabled()) {
                    actor.renderBounds(settingsConfig);
                }
                break;
            }
        }
        if (!packet.getActorData().containsKey(ActorDataTypes.NAME)) {
            return PacketSignal.UNHANDLED;
        }
        final String name = packet.getActorData().get(ActorDataTypes.NAME);
        for (final Actor actor : proxy.getActors()) {
            if (actor.getRuntimeId() == packet.getTargetRuntimeID()) {
                actor.setName(name);
                break;
            }
        }
        return PacketSignal.UNHANDLED;
    }
}