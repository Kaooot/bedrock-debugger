package dev.kaooot.debugger.network.handler;

import org.cloudburstmc.protocol.bedrock.packet.DimensionDataPacket;
import org.cloudburstmc.protocol.common.PacketSignal;
import dev.kaooot.debugger.BedrockDebuggerProxy;
import dev.kaooot.debugger.network.PacketHandler;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
public class DimensionDataHandler implements PacketHandler<DimensionDataPacket> {

    @Override
    public PacketSignal handle(DimensionDataPacket packet, BedrockDebuggerProxy proxy) {
        proxy.getPlayer().getDimensionData().addAll(packet.getDefinitions());
        return PacketSignal.UNHANDLED;
    }
}