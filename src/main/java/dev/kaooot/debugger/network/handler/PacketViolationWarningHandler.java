package dev.kaooot.debugger.network.handler;

import org.cloudburstmc.protocol.bedrock.packet.PacketViolationWarningPacket;
import org.cloudburstmc.protocol.common.PacketSignal;
import dev.kaooot.debugger.BedrockDebuggerProxy;
import dev.kaooot.debugger.network.PacketHandler;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
public class PacketViolationWarningHandler implements PacketHandler<PacketViolationWarningPacket> {

    @Override
    public PacketSignal handle(PacketViolationWarningPacket packet, BedrockDebuggerProxy proxy) {
        proxy.getLogger().warn("PACKET VIOLATION: {}", packet);
        return PacketSignal.UNHANDLED;
    }
}