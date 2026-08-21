package dev.kaooot.debugger.network.handler;

import org.cloudburstmc.protocol.bedrock.packet.MovePlayerPacket;
import org.cloudburstmc.protocol.common.PacketSignal;
import dev.kaooot.debugger.BedrockDebuggerProxy;
import dev.kaooot.debugger.network.PacketHandler;
import dev.kaooot.debugger.player.ServerPlayer;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
public class MovePlayerHandler implements PacketHandler<MovePlayerPacket> {

    @Override
    public PacketSignal handle(MovePlayerPacket packet, BedrockDebuggerProxy proxy) {
        if (proxy.isTransferring() || proxy.getPlayers().isEmpty() || proxy.getPlayer() == null) {
            return PacketSignal.HANDLED;
        }
        for (final ServerPlayer player : proxy.getPlayers()) {
            if (player.getRuntimeId() != packet.getPlayerRuntimeID()) {
                continue;
            }
            player.setPosition(packet.getPosition());
        }
        return PacketSignal.UNHANDLED;
    }
}