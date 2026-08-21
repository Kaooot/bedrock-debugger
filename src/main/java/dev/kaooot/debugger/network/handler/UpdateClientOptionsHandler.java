package dev.kaooot.debugger.network.handler;

import org.cloudburstmc.protocol.bedrock.packet.UpdateClientOptionsPacket;
import org.cloudburstmc.protocol.common.PacketSignal;
import dev.kaooot.debugger.BedrockDebuggerProxy;
import dev.kaooot.debugger.network.PacketHandler;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
public class UpdateClientOptionsHandler implements PacketHandler<UpdateClientOptionsPacket> {

    @Override
    public PacketSignal handle(UpdateClientOptionsPacket packet, BedrockDebuggerProxy proxy) {
        proxy.getPlayer().setGraphicsMode(packet.getGraphicsMode());
        return PacketSignal.UNHANDLED;
    }
}