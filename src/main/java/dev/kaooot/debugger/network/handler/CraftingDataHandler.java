package dev.kaooot.debugger.network.handler;

import org.cloudburstmc.protocol.bedrock.packet.CraftingDataPacket;
import org.cloudburstmc.protocol.common.PacketSignal;
import dev.kaooot.debugger.BedrockDebuggerProxy;
import dev.kaooot.debugger.network.PacketHandler;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
public class CraftingDataHandler implements PacketHandler<CraftingDataPacket> {

    @Override
    public PacketSignal handle(CraftingDataPacket packet, BedrockDebuggerProxy proxy) {
        proxy.getPlayer().setCraftingData(packet);
        return PacketSignal.UNHANDLED;
    }
}