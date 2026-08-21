package dev.kaooot.debugger.network.handler;

import org.cloudburstmc.math.vector.Vector3f;
import org.cloudburstmc.protocol.bedrock.packet.MoveActorAbsolutePacket;
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
public class MoveActorAbsoluteHandler implements PacketHandler<MoveActorAbsolutePacket> {

    @Override
    public PacketSignal handle(MoveActorAbsolutePacket packet, BedrockDebuggerProxy proxy) {
        final SettingsConfig settingsConfig = Registries.
            <ConfigRegistry>getRegistry(RegistryKey.CONFIG)
            .get(SettingsConfig.class);

        for (final Actor actor : proxy.getActors()) {
            if (actor.getRuntimeId() == packet.getMoveData().getActorRuntimeID()) {
                if (actor.getPosition().getX() != packet.getMoveData().getPos().getX() ||
                    actor.getPosition().getY() != packet.getMoveData().getPos().getY() ||
                    actor.getPosition().getZ() != packet.getMoveData().getPos().getZ()) {
                    actor.setPosition(packet.getMoveData().getPos());
                }
                break;
            }
        }
        for (final ServerPlayer player : proxy.getPlayers()) {
            if (player.getRuntimeId() == packet.getMoveData().getActorRuntimeID() &&
                player.getActorId() != proxy.getPlayer().getActorId()) {
                final Vector3f position = packet.getMoveData().getPos().clone()
                    .sub(0f, player.getEyeHeight(), 0f);
                player.setPosition(position);
                break;
            }
        }
        return PacketSignal.UNHANDLED;
    }
}