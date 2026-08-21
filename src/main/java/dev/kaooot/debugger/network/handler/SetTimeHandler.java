package dev.kaooot.debugger.network.handler;

import org.cloudburstmc.protocol.bedrock.packet.SetTimePacket;
import org.cloudburstmc.protocol.common.PacketSignal;
import dev.kaooot.debugger.BedrockDebuggerProxy;
import dev.kaooot.debugger.network.PacketHandler;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
public class SetTimeHandler implements PacketHandler<SetTimePacket> {

    @Override
    public PacketSignal handle(SetTimePacket packet, BedrockDebuggerProxy proxy) {
        proxy.getPlayer().setLevelTime(packet.getTime());
        return proxy.getPlayer().isAlwaysDay() ? PacketSignal.HANDLED : PacketSignal.UNHANDLED;
    }
}