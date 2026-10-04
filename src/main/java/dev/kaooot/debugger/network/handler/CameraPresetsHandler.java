package dev.kaooot.debugger.network.handler;

import dev.kaooot.debugger.BedrockDebuggerProxy;
import dev.kaooot.debugger.network.PacketHandler;
import dev.kaooot.debugger.util.Util;
import org.cloudburstmc.protocol.bedrock.data.camera.CameraPresets;
import org.cloudburstmc.protocol.bedrock.packet.CameraPresetsPacket;
import org.cloudburstmc.protocol.common.NamedDefinition;
import org.cloudburstmc.protocol.common.PacketSignal;
import org.cloudburstmc.protocol.common.SimpleDefinitionRegistry;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
public class CameraPresetsHandler implements PacketHandler<CameraPresetsPacket> {

    @Override
    public PacketSignal handle(CameraPresetsPacket packet, BedrockDebuggerProxy proxy) {
        proxy.getPlayer().setCameraPresets(
            Util.convertCameraPresetsToNbt(
                packet.getCameraPresets()
            )
        );
        final SimpleDefinitionRegistry.Builder<NamedDefinition> registry =
            new SimpleDefinitionRegistry.Builder<>();
        for (final CameraPresets preset : packet.getCameraPresets()) {
            registry.add(new NamedDefinition() {
                @Override
                public String getIdentifier() {
                    return preset.getName();
                }

                @Override
                public int getRuntimeId() {
                    return packet.getCameraPresets().indexOf(preset);
                }
            });
        }
        proxy.getClient().setCameraPresetDefinitions(registry.build());
        proxy.getServer().setCameraPresetDefinitions(registry.build());
        return PacketSignal.UNHANDLED;
    }
}