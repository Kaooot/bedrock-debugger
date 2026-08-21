package dev.kaooot.debugger.network.handler;

import org.cloudburstmc.protocol.bedrock.packet.AvailableActorIdentifiersPacket;
import org.cloudburstmc.protocol.common.PacketSignal;
import dev.kaooot.debugger.BedrockDebuggerProxy;
import dev.kaooot.debugger.network.PacketHandler;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
public class AvailableActorIdentifiersHandler
    implements PacketHandler<AvailableActorIdentifiersPacket> {

    @Override
    public PacketSignal handle(AvailableActorIdentifiersPacket packet, BedrockDebuggerProxy proxy) {
        proxy.getPlayer().setActorInfoList(packet.getActorInfoList());
        return PacketSignal.UNHANDLED;
    }
}