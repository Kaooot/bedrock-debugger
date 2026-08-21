package dev.kaooot.debugger.network.handler;

import org.cloudburstmc.math.vector.Vector3f;
import org.cloudburstmc.protocol.bedrock.packet.MoveActorDeltaPacket;
import org.cloudburstmc.protocol.common.PacketSignal;
import dev.kaooot.debugger.BedrockDebuggerProxy;
import dev.kaooot.debugger.actor.Actor;
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
public class MoveActorDeltaHandler implements PacketHandler<MoveActorDeltaPacket> {

    @Override
    public PacketSignal handle(MoveActorDeltaPacket packet, BedrockDebuggerProxy proxy) {
        final SettingsConfig settingsConfig = Registries.
            <ConfigRegistry>getRegistry(RegistryKey.CONFIG)
            .get(SettingsConfig.class);
        if (!proxy.getPlayer().isReadyToRoll() || !settingsConfig.isActorDebugRendererEnabled()) {
            return PacketSignal.UNHANDLED;
        }
        for (final Actor actor : proxy.getActors()) {
            if (actor.getRuntimeId() == packet.getMoveData().getActorRuntimeID()) {
                actor.setPosition(Vector3f.from(
                    packet.getMoveData().getNewPositionX() != null ?
                        packet.getMoveData().getNewPositionX() : actor.getPosition().getX(),
                    packet.getMoveData().getNewPositionY() != null ?
                        packet.getMoveData().getNewPositionY() : actor.getPosition().getY(),
                    packet.getMoveData().getNewPositionZ() != null ?
                        packet.getMoveData().getNewPositionZ() : actor.getPosition().getZ())
                );
                break;
            }
        }
        return PacketSignal.UNHANDLED;
    }
}