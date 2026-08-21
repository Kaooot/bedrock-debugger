package dev.kaooot.debugger.network.handler;

import org.cloudburstmc.protocol.bedrock.packet.UpdateSubChunkBlocksPacket;
import org.cloudburstmc.protocol.common.PacketSignal;
import dev.kaooot.debugger.BedrockDebuggerProxy;
import dev.kaooot.debugger.network.PacketHandler;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
public class UpdateSubChunkBlocksHandler implements PacketHandler<UpdateSubChunkBlocksPacket> {

    @Override
    public PacketSignal handle(UpdateSubChunkBlocksPacket packet, BedrockDebuggerProxy proxy) {
        proxy.getPlayer().getPlayerChunkManager().handleUpdateSubChunkBlocks(packet);
        return PacketSignal.UNHANDLED;
    }
}