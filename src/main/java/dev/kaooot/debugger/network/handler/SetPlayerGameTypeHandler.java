package dev.kaooot.debugger.network.handler;

import org.cloudburstmc.protocol.bedrock.packet.SetPlayerGameTypePacket;
import org.cloudburstmc.protocol.common.PacketSignal;
import dev.kaooot.debugger.BedrockDebuggerProxy;
import dev.kaooot.debugger.network.PacketHandler;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
public class SetPlayerGameTypeHandler implements PacketHandler<SetPlayerGameTypePacket> {

    @Override
    public PacketSignal handle(SetPlayerGameTypePacket packet, BedrockDebuggerProxy proxy) {
        proxy.getPlayer().setGameType(packet.getPlayerGameType());
        return PacketSignal.UNHANDLED;
    }
}