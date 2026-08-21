package dev.kaooot.debugger.network.handler;

import org.cloudburstmc.protocol.bedrock.packet.BlockActorDataPacket;
import org.cloudburstmc.protocol.common.PacketSignal;
import dev.kaooot.debugger.BedrockDebuggerProxy;
import dev.kaooot.debugger.network.PacketHandler;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
public class BlockActorDataHandler implements PacketHandler<BlockActorDataPacket> {

    @Override
    public PacketSignal handle(BlockActorDataPacket packet, BedrockDebuggerProxy proxy) {
        proxy.getPlayer().getPlayerChunkManager().handleBlockActorData(packet);
        return PacketSignal.UNHANDLED;
    }
}