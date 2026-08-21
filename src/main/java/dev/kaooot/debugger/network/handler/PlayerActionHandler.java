package dev.kaooot.debugger.network.handler;

import org.cloudburstmc.protocol.bedrock.packet.PlayerActionPacket;
import org.cloudburstmc.protocol.common.PacketSignal;
import dev.kaooot.debugger.BedrockDebuggerProxy;
import dev.kaooot.debugger.network.PacketHandler;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
public class PlayerActionHandler implements PacketHandler<PlayerActionPacket> {

    @Override
    public PacketSignal handle(PlayerActionPacket packet, BedrockDebuggerProxy proxy) {
        return proxy.getPlayer().getCheatClientAuthority()
            .handleInvalidCreativeActionBypass(packet) ? PacketSignal.HANDLED :
            PacketSignal.UNHANDLED;
    }
}