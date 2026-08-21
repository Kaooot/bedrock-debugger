package dev.kaooot.debugger.network.handler;

import org.cloudburstmc.protocol.bedrock.data.payload.pack.PackInstanceId;
import org.cloudburstmc.protocol.bedrock.packet.ResourcePackStackPacket;
import org.cloudburstmc.protocol.common.PacketSignal;
import dev.kaooot.debugger.BedrockDebuggerProxy;
import dev.kaooot.debugger.network.PacketHandler;
import dev.kaooot.debugger.pack.ServerPack;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
public class ResourcePackStackHandler implements PacketHandler<ResourcePackStackPacket> {

    @Override
    public PacketSignal handle(ResourcePackStackPacket packet, BedrockDebuggerProxy proxy) {
        proxy.getPlayer().getPackStack().addAll(packet.getTexturePackList());
        proxy.getPlayer().setStackTexturePackRequired(packet.isTexturePackRequired());
        proxy.getPlayer().setStackIncludeEditorPacks(packet.isIncludeEditorPacks());
        proxy.getPlayer().setStackBaseGameVersion(packet.getBaseGameVersion());
        for (final ServerPack pack : proxy.getPackManager().getPacks()) {
            final PackInstanceId entry = new PackInstanceId(
                pack.getId().toString(),
                pack.getVersion(),
                ""
            );
            packet.getTexturePackList().add(entry);
        }
        proxy.getServer().sendPacketImmediately(packet);
        return PacketSignal.HANDLED;
    }
}