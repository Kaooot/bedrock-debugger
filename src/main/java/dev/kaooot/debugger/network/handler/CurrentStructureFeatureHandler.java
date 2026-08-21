package dev.kaooot.debugger.network.handler;

import org.cloudburstmc.protocol.bedrock.packet.CurrentStructureFeaturePacket;
import org.cloudburstmc.protocol.common.PacketSignal;
import dev.kaooot.debugger.BedrockDebuggerProxy;
import dev.kaooot.debugger.network.PacketHandler;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
public class CurrentStructureFeatureHandler implements PacketHandler<CurrentStructureFeaturePacket> {

    @Override
    public PacketSignal handle(CurrentStructureFeaturePacket packet, BedrockDebuggerProxy proxy) {
        proxy.getPlayer().setCurrentStructureFeature(packet.getCurrentStructureFeature());
        return PacketSignal.UNHANDLED;
    }
}