package dev.kaooot.debugger.network.handler;

import org.cloudburstmc.protocol.bedrock.packet.LevelSoundEventPacket;
import org.cloudburstmc.protocol.common.PacketSignal;
import dev.kaooot.debugger.BedrockDebuggerProxy;
import dev.kaooot.debugger.imgui.renderer.ImGuiMainRenderer;
import dev.kaooot.debugger.network.PacketHandler;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
public class LevelSoundEventHandler implements PacketHandler<LevelSoundEventPacket> {

    @Override
    public PacketSignal handle(LevelSoundEventPacket packet, BedrockDebuggerProxy proxy) {
        if (proxy.getPlayer().getLevelSoundEventPackets().size() >
            ImGuiMainRenderer.SOUND_EVENT_DEBUG_COUNTER) {
            proxy.getPlayer().getLevelSoundEventPackets().clear();
        }
        if (!ImGuiMainRenderer.SOUND_EVENT_DEBUG_LOCKED) {
            proxy.getPlayer().getLevelSoundEventPackets().add(packet);
        }
        return PacketSignal.UNHANDLED;
    }
}