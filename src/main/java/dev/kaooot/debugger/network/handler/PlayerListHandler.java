package dev.kaooot.debugger.network.handler;

import dev.kaooot.debugger.BedrockDebuggerProxy;
import dev.kaooot.debugger.network.PacketHandler;
import org.cloudburstmc.protocol.bedrock.data.payload.list.PlayerListAddEntry;
import org.cloudburstmc.protocol.bedrock.data.payload.list.PlayerListEntry;
import org.cloudburstmc.protocol.bedrock.packet.PlayerListPacket;
import org.cloudburstmc.protocol.common.PacketSignal;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
public class PlayerListHandler implements PacketHandler<PlayerListPacket> {

    @Override
    public PacketSignal handle(PlayerListPacket packet, BedrockDebuggerProxy proxy) {
        for (final PlayerListEntry entry : packet.getEntries()) {
            if (entry instanceof PlayerListAddEntry addEntry &&
                addEntry.getXblXUID().equals(proxy.getPlayer().getXuid())) {
                proxy.getPlayer().setSerializedSkin(addEntry.getSerializedSkin());
                break;
            }
        }
        return PacketSignal.UNHANDLED;
    }
}