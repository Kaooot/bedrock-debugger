package dev.kaooot.debugger.network.handler;

import org.cloudburstmc.protocol.bedrock.packet.RemoveActorPacket;
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
public class RemoveActorHandler implements PacketHandler<RemoveActorPacket> {

    @Override
    public PacketSignal handle(RemoveActorPacket packet, BedrockDebuggerProxy proxy) {
        final SettingsConfig settingsConfig = Registries.
            <ConfigRegistry>getRegistry(RegistryKey.CONFIG)
            .get(SettingsConfig.class);

        if (settingsConfig.isActorDebugRendererEnabled()) {
            for (final Actor actor : proxy.getActors()) {
                if (actor.getActorId() == packet.getTargetActorID() &&
                    proxy.getDebugShapeRenderer()
                        .isShapeRendered("actor_box_" + actor.getActorId())) {
                    proxy.getDebugShapeRenderer().removeShapes(true,
                        "actor_box_" + actor.getActorId(),
                        "actor_box_text_" + actor.getActorId(),
                        "actor_box_link_text_" + actor.getActorId());
                    break;
                }
            }
        }

        for (final ServerPlayer player : proxy.getPlayers()) {
            if (player.getActorId() != packet.getTargetActorID()) {
                continue;
            }
            if (settingsConfig.isPlayerDebugRendererEnabled()) {
                final String id = "player_box_" + player.getActorId();
                if (proxy.getDebugShapeRenderer().isShapeRendered(id)) {
                    proxy.getDebugShapeRenderer()
                        .removeShapes(true, id);
                    break;
                }
            }
        }
        proxy.getActors().removeIf(
            actor -> actor.getActorId() == packet.getTargetActorID() ||
                actor.getRuntimeId() == packet.getTargetActorID()
        );
        proxy.getPlayers().removeIf(
            player -> player.getActorId() == packet.getTargetActorID()
        );
        return PacketSignal.UNHANDLED;
    }
}