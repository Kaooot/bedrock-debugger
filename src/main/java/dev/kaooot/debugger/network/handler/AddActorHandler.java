package dev.kaooot.debugger.network.handler;

import org.cloudburstmc.protocol.bedrock.data.payload.attribute.AttributeData;
import org.cloudburstmc.protocol.bedrock.packet.AddActorPacket;
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
public class AddActorHandler implements PacketHandler<AddActorPacket> {

    @Override
    public PacketSignal handle(AddActorPacket packet, BedrockDebuggerProxy proxy) {
        final Actor actor = new Actor(
            proxy, packet.getActorType(),
            packet.getPosition(),
            packet.getTargetActorID(),
            packet.getTargetRuntimeID()
        );
        proxy.getActors().add(actor);
        proxy.getServer().sendPacket(packet);
        if (!packet.getActorLinks().isEmpty()) {
            actor.setLink(packet.getActorLinks().getFirst());
        }
        actor.setMetadata(packet.getActorData());
        for (final AttributeData attributeData : packet.getAttributesList()) {
            if (attributeData.getAttributeName().equalsIgnoreCase("minecraft:health")) {
                actor.setHealth(attributeData.getCurrentValue());
                actor.setMaxHealth(attributeData.getMaxValue());
            }
        }

        final SettingsConfig settingsConfig = Registries.
            <ConfigRegistry>getRegistry(RegistryKey.CONFIG)
            .get(SettingsConfig.class);

        if (proxy.getPlayer().isReadyToRoll() &&
            settingsConfig.isActorDebugRendererEnabled()) {
            actor.renderBounds(settingsConfig);
        }
        return PacketSignal.HANDLED;
    }
}