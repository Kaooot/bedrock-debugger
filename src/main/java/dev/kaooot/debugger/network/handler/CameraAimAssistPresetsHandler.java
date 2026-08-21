package dev.kaooot.debugger.network.handler;

import org.cloudburstmc.nbt.NbtMap;
import org.cloudburstmc.nbt.NbtType;
import org.cloudburstmc.protocol.bedrock.packet.CameraAimAssistPresetsPacket;
import org.cloudburstmc.protocol.common.PacketSignal;
import dev.kaooot.debugger.BedrockDebuggerProxy;
import dev.kaooot.debugger.network.PacketHandler;
import dev.kaooot.debugger.util.Util;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
public class CameraAimAssistPresetsHandler implements PacketHandler<CameraAimAssistPresetsPacket> {

    @Override
    public PacketSignal handle(CameraAimAssistPresetsPacket packet, BedrockDebuggerProxy proxy) {
        proxy.getPlayer().setCameraAimAssistPresets(
            NbtMap.builder()
                .putList("categories",
                    NbtType.COMPOUND,
                    Util.convertCameraAimAssistCategoriesToNbt(packet.getCategoryDefinitions())
                )
                .putList("presets",
                    NbtType.COMPOUND,
                    Util.convertCameraAimAssistPresetsToNbt(packet.getPresets())
                )
                .build()
        );
        return PacketSignal.UNHANDLED;
    }
}