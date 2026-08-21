package dev.kaooot.debugger.network.handler;

import org.cloudburstmc.protocol.bedrock.data.ActorLinkType;
import org.cloudburstmc.protocol.bedrock.data.actor.ActorLink;
import org.cloudburstmc.protocol.bedrock.packet.SetActorLinkPacket;
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
public class SetActorLinkHandler implements PacketHandler<SetActorLinkPacket> {

    @Override
    public PacketSignal handle(SetActorLinkPacket packet, BedrockDebuggerProxy proxy) {
        final ActorLink link = packet.getLink();
        for (final Actor actor : proxy.getActors()) {
            if (actor.getActorId() == link.getTargetB() ||
                actor.getActorId() == link.getTargetA()) {
                actor.setLink(link.getType().equals(ActorLinkType.NONE) ? null :
                    link);

                final SettingsConfig settingsConfig = Registries.
                    <ConfigRegistry>getRegistry(RegistryKey.CONFIG)
                    .get(SettingsConfig.class);

                if (proxy.getPlayer().isReadyToRoll() &&
                    settingsConfig.isActorDebugRendererEnabled()) {
                    actor.renderBounds(settingsConfig);
                }
            }
        }
        return PacketSignal.UNHANDLED;
    }
}