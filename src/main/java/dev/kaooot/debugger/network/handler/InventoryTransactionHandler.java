package dev.kaooot.debugger.network.handler;

import org.cloudburstmc.protocol.bedrock.packet.InventoryTransactionPacket;
import org.cloudburstmc.protocol.common.PacketSignal;
import dev.kaooot.debugger.BedrockDebuggerProxy;
import dev.kaooot.debugger.network.PacketHandler;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
public class InventoryTransactionHandler implements PacketHandler<InventoryTransactionPacket> {

    @Override
    public PacketSignal handle(InventoryTransactionPacket packet, BedrockDebuggerProxy proxy) {
        return proxy.getPlayer().getCheatClientAuthority().handleClicksPerSecondOverride(packet) ?
            PacketSignal.HANDLED : PacketSignal.UNHANDLED;
    }
}