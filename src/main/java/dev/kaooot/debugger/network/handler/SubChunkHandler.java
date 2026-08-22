package dev.kaooot.debugger.network.handler;

import dev.kaooot.debugger.BedrockDebuggerProxy;
import dev.kaooot.debugger.level.LevelChunk;
import dev.kaooot.debugger.level.PlayerChunkManager;
import dev.kaooot.debugger.level.block.Block;
import dev.kaooot.debugger.level.storage.SubChunkStorage;
import dev.kaooot.debugger.network.PacketHandler;
import io.netty.buffer.ByteBuf;
import org.cloudburstmc.math.vector.Vector3i;
import org.cloudburstmc.protocol.bedrock.data.payload.chunk.SubChunkPacketData;
import org.cloudburstmc.protocol.bedrock.data.payload.chunk.SubChunkRequestResult;
import org.cloudburstmc.protocol.bedrock.data.payload.common.DimensionType;
import org.cloudburstmc.protocol.bedrock.packet.SubChunkPacket;
import org.cloudburstmc.protocol.common.PacketSignal;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
public class SubChunkHandler implements PacketHandler<SubChunkPacket> {

    @Override
    public PacketSignal handle(SubChunkPacket packet, BedrockDebuggerProxy proxy) {
        final Vector3i centerPos = packet.getCenterPos();
        final DimensionType dimension = packet.getDimensionType();
        final PlayerChunkManager playerChunkManager = proxy.getPlayer().getPlayerChunkManager();
        for (final SubChunkPacketData subChunkPacketData : packet.getSubChunkData()) {
            if (subChunkPacketData.getBlobId() != null) {
                return PacketSignal.UNHANDLED;
            }
            final SubChunkRequestResult result = subChunkPacketData.getSubChunkRequestResult();

            if (!result.equals(SubChunkRequestResult.SUCCESS) &&
                !result.equals(SubChunkRequestResult.SUCCESS_ALL_AIR)) {
                return PacketSignal.UNHANDLED;
            }

            final Vector3i subChunkPos = subChunkPacketData.getSubChunkPosOffset().add(centerPos);
            final int chunkX = subChunkPos.getX();
            final int chunkZ = subChunkPos.getZ();
            final int subChunkIndex = subChunkPos.getY();

            if (result.equals(SubChunkRequestResult.SUCCESS_ALL_AIR)) {
                final SubChunkStorage<Block>[] storages = new SubChunkStorage[2];
                for (int layer = 0; layer < 2; layer++) {
                    storages[layer] = new SubChunkStorage<>(Block.AIR);
                }

                proxy.getPlayer().getPlayerChunkManager().updateSubChunk(
                    chunkX,
                    chunkZ,
                    dimension,
                    subChunkIndex,
                    storages
                );
            } else {
                final ByteBuf serializedSubChunk = subChunkPacketData.getSerializedSubChunk()
                    .copy();
                try {
                    playerChunkManager.readSubChunk(
                        serializedSubChunk,
                        subChunkIndex,
                        chunkX,
                        chunkZ,
                        dimension
                    );
                    playerChunkManager.readBorderBlocks(serializedSubChunk);

                    final LevelChunk levelChunk = playerChunkManager.getChunk(chunkX, chunkZ);
                    playerChunkManager.readBlockActorDataTags(serializedSubChunk, levelChunk);
                } finally {
                    serializedSubChunk.release();
                }
            }
        }
        return PacketSignal.UNHANDLED;
    }
}