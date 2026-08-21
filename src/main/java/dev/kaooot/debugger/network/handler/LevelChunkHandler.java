package dev.kaooot.debugger.network.handler;

import dev.kaooot.debugger.BedrockDebuggerProxy;
import dev.kaooot.debugger.level.LevelChunk;
import dev.kaooot.debugger.level.PlayerChunkManager;
import dev.kaooot.debugger.level.storage.SubChunkStorage;
import dev.kaooot.debugger.network.PacketHandler;
import io.netty.buffer.ByteBuf;
import org.cloudburstmc.protocol.bedrock.data.payload.common.DimensionType;
import org.cloudburstmc.protocol.bedrock.packet.LevelChunkPacket;
import org.cloudburstmc.protocol.common.PacketSignal;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
public class LevelChunkHandler implements PacketHandler<LevelChunkPacket> {

    @Override
    public PacketSignal handle(LevelChunkPacket packet, BedrockDebuggerProxy proxy) {
        if (packet.isClientNeedsToRequestSubChunks()) {
            return PacketSignal.UNHANDLED;
        }
        final int chunkX = packet.getChunkX();
        final int chunkZ = packet.getChunkZ();
        final DimensionType dimension = packet.getDimension();
        final int subChunksCount = packet.getSubChunksCount();
        final ByteBuf serializedChunkData = packet.getSerializedChunkData().copy();
        final PlayerChunkManager playerChunkManager = proxy.getPlayer().getPlayerChunkManager();
        final LevelChunk levelChunk = playerChunkManager.getChunk(chunkX, chunkZ);
        this.readLevelChunk(
            proxy, chunkX, chunkZ, dimension, subChunksCount, levelChunk,
            serializedChunkData, playerChunkManager
        );
        return PacketSignal.UNHANDLED;
    }

    private void readLevelChunk(BedrockDebuggerProxy proxy, int chunkX, int chunkZ,
                                DimensionType dimension, int subChunksCount, LevelChunk levelChunk,
                                ByteBuf serializedChunkData, PlayerChunkManager manager) {
        final int heightMinimum = proxy.getPlayer().getDimensionDefinition()
            .getHeightMinimum();
        try {
            for (int i = 0; i < subChunksCount; i++) {
                final int subChunkIndex = i + (heightMinimum >> 4);
                manager.readSubChunk(
                    serializedChunkData,
                    subChunkIndex,
                    chunkX,
                    chunkZ,
                    dimension
                );
            }
            for (int i = 0; i < subChunksCount; i++) {
                final int subChunkIndex = i + (heightMinimum >> 4);
                final SubChunkStorage<Integer> biomeStorage = new SubChunkStorage<>(0);
                biomeStorage.deserializeNetwork(serializedChunkData, biomeId -> biomeId);

                levelChunk.getSubChunk(subChunkIndex).setBiomeStorage(biomeStorage);
            }

            manager.readBorderBlocks(serializedChunkData);
            manager.readBlockActorDataTags(serializedChunkData, levelChunk);
        } finally {
            serializedChunkData.release();
        }
    }
}