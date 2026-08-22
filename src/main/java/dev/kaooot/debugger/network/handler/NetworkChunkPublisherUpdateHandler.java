package dev.kaooot.debugger.network.handler;

import dev.kaooot.debugger.BedrockDebuggerProxy;
import dev.kaooot.debugger.network.PacketHandler;
import org.cloudburstmc.math.vector.Vector3i;
import org.cloudburstmc.protocol.bedrock.packet.NetworkChunkPublisherUpdatePacket;
import org.cloudburstmc.protocol.common.PacketSignal;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
public class NetworkChunkPublisherUpdateHandler
    implements PacketHandler<NetworkChunkPublisherUpdatePacket> {

    @Override
    public PacketSignal handle(NetworkChunkPublisherUpdatePacket packet,
                               BedrockDebuggerProxy proxy) {
        final Vector3i newPositionForView = packet.getNewPositionForView();
        final int newRadiusForView = packet.getNewRadiusForView();
        final int viewDistance = ((newRadiusForView + 15) >> 4) + 1;
        proxy.getPlayer().getPlayerChunkManager().evictChunksOutsideRadius(
            newPositionForView.getX() >> 4,
            newPositionForView.getZ() >> 4,
            viewDistance
        );
        return PacketSignal.UNHANDLED;
    }
}