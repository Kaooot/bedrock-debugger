package dev.kaooot.debugger.network.handler;

import org.cloudburstmc.protocol.bedrock.packet.ResourcePackChunkDataPacket;
import org.cloudburstmc.protocol.bedrock.packet.ResourcePackChunkRequestPacket;
import org.cloudburstmc.protocol.common.PacketSignal;
import dev.kaooot.debugger.BedrockDebuggerProxy;
import dev.kaooot.debugger.network.PacketHandler;
import dev.kaooot.debugger.pack.ServerPack;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
public class ResourcePackChunkRequestHandler
    implements PacketHandler<ResourcePackChunkRequestPacket> {

    private final int maxChunkSize = 8192;

    @Override
    public PacketSignal handle(ResourcePackChunkRequestPacket packet, BedrockDebuggerProxy proxy) {
        final ServerPack pack = proxy.getPackManager().getPackIdCache().get(packet.getPackId());
        if (pack != null) {
            final ResourcePackChunkDataPacket resourcePackChunkDataPacket =
                new ResourcePackChunkDataPacket();
            resourcePackChunkDataPacket.setPackId(packet.getPackId());
            resourcePackChunkDataPacket.setPackVersion(packet.getPackVersion());
            resourcePackChunkDataPacket.setChunkID(packet.getChunk());
            resourcePackChunkDataPacket.setByteOffset((long) this.maxChunkSize * packet.getChunk());
            resourcePackChunkDataPacket.setChunkData(
                proxy.getPackManager().getChunkFromPack(
                    pack,
                    this.maxChunkSize * packet.getChunk(),
                    this.maxChunkSize
                )
            );

            proxy.getServer().sendPacket(resourcePackChunkDataPacket);
            return PacketSignal.HANDLED;
        }
        return PacketSignal.UNHANDLED;
    }
}