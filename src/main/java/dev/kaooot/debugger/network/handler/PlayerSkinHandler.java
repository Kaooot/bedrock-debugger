package dev.kaooot.debugger.network.handler;

import dev.kaooot.debugger.BedrockDebuggerProxy;
import dev.kaooot.debugger.network.PacketHandler;
import org.cloudburstmc.protocol.bedrock.packet.PlayerSkinPacket;
import org.cloudburstmc.protocol.common.PacketSignal;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
public class PlayerSkinHandler implements PacketHandler<PlayerSkinPacket> {

    @Override
    public PacketSignal handle(PlayerSkinPacket packet, BedrockDebuggerProxy proxy) {
        if (packet.getUuid().equals(proxy.getPlayer().getUniqueId())) {
            proxy.getPlayer().setSerializedSkin(packet.getSerializedSkin());
        }
        return PacketSignal.UNHANDLED;
    }
}