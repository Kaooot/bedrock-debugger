package dev.kaooot.debugger.network.handler;

import org.cloudburstmc.protocol.bedrock.packet.UpdateBlockPacket;
import org.cloudburstmc.protocol.common.PacketSignal;
import dev.kaooot.debugger.BedrockDebuggerProxy;
import dev.kaooot.debugger.network.PacketHandler;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
public class UpdateBlockHandler implements PacketHandler<UpdateBlockPacket> {

    @Override
    public PacketSignal handle(UpdateBlockPacket packet, BedrockDebuggerProxy proxy) {
        proxy.getPlayer().getPlayerChunkManager().handleUpdateBlock(
            packet.getDefinition(),
            packet.getBlockPosition(),
            packet.getLayer()
        );
        return PacketSignal.UNHANDLED;
    }
}