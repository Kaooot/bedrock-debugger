package dev.kaooot.debugger.network.handler;

import org.cloudburstmc.protocol.bedrock.packet.SetPlayerGameTypePacket;
import org.cloudburstmc.protocol.bedrock.packet.UpdatePlayerGameTypePacket;
import org.cloudburstmc.protocol.common.PacketSignal;
import dev.kaooot.debugger.BedrockDebuggerProxy;
import dev.kaooot.debugger.network.PacketHandler;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
public class UpdatePlayerGameTypeHandler implements PacketHandler<UpdatePlayerGameTypePacket> {

    @Override
    public PacketSignal handle(UpdatePlayerGameTypePacket packet, BedrockDebuggerProxy proxy) {
        if (proxy.getPlayer().getActorId() == packet.getTargetPlayer()) {
            proxy.getPlayer().setGameType(packet.getPlayerGameType());
            final SetPlayerGameTypePacket setPlayerGameTypePacket = new SetPlayerGameTypePacket();
            setPlayerGameTypePacket.setPlayerGameType(packet.getPlayerGameType());
            proxy.getServer().sendPacket(setPlayerGameTypePacket);
        }
        proxy.getServer().sendPacket(packet);
        return PacketSignal.HANDLED;
    }
}