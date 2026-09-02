package dev.kaooot.debugger.network.handler;

import org.cloudburstmc.protocol.bedrock.packet.UpdateAbilitiesPacket;
import org.cloudburstmc.protocol.common.PacketSignal;
import dev.kaooot.debugger.BedrockDebuggerProxy;
import dev.kaooot.debugger.network.PacketHandler;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
public class UpdateAbilitiesHandler implements PacketHandler<UpdateAbilitiesPacket> {

    @Override
    public PacketSignal handle(UpdateAbilitiesPacket packet, BedrockDebuggerProxy proxy) {
        proxy.getPlayer().getCheatClientAuthority().handleEnhancedFlySpeed(packet);
        return PacketSignal.UNHANDLED;
    }
}