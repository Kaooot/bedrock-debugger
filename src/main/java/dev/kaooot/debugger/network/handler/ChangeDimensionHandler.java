package dev.kaooot.debugger.network.handler;

import org.cloudburstmc.protocol.bedrock.packet.ChangeDimensionPacket;
import org.cloudburstmc.protocol.common.PacketSignal;
import dev.kaooot.debugger.BedrockDebuggerProxy;
import dev.kaooot.debugger.network.PacketHandler;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
public class ChangeDimensionHandler implements PacketHandler<ChangeDimensionPacket> {

    @Override
    public PacketSignal handle(ChangeDimensionPacket packet, BedrockDebuggerProxy proxy) {
        proxy.getPlayer().updateDimension(packet.getDimension());
        proxy.getPlayer().getPlayerChunkManager().clearChunks();
        return PacketSignal.UNHANDLED;
    }
}