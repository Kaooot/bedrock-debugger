package dev.kaooot.debugger.network.handler;

import org.cloudburstmc.protocol.bedrock.data.payload.attribute.AttributeData;
import org.cloudburstmc.protocol.bedrock.packet.UpdateAttributesPacket;
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
public class UpdateAttributesHandler implements PacketHandler<UpdateAttributesPacket> {

    @Override
    public PacketSignal handle(UpdateAttributesPacket packet, BedrockDebuggerProxy proxy) {
        for (final Actor actor : proxy.getActors()) {
            if (actor.getRuntimeId() == packet.getRuntimeID()) {
                for (final AttributeData attributeData : packet.getAttributeList()) {
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
                break;
            }
        }
        return PacketSignal.UNHANDLED;
    }
}