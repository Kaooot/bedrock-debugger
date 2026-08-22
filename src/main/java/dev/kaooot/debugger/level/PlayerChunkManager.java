package dev.kaooot.debugger.level;

import dev.kaooot.debugger.BedrockDebuggerProxy;
import dev.kaooot.debugger.level.block.Block;
import dev.kaooot.debugger.level.storage.SubChunkStorage;
import dev.kaooot.debugger.player.ProxiedPlayer;
import dev.kaooot.debugger.util.Util;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.io.IOException;
import java.util.Iterator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.cloudburstmc.math.vector.Vector3i;
import org.cloudburstmc.nbt.NBTInputStream;
import org.cloudburstmc.nbt.NbtMap;
import org.cloudburstmc.nbt.NbtUtils;
import org.cloudburstmc.protocol.bedrock.data.BlockChangeEntry;
import org.cloudburstmc.protocol.bedrock.data.definitions.BlockDefinition;
import org.cloudburstmc.protocol.bedrock.data.payload.common.DimensionType;
import org.cloudburstmc.protocol.bedrock.packet.BlockActorDataPacket;
import org.cloudburstmc.protocol.bedrock.packet.UpdateSubChunkBlocksPacket;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@RequiredArgsConstructor
public class PlayerChunkManager {

    private final BedrockDebuggerProxy proxy;
    private final ProxiedPlayer player;

    @Getter
    private final Long2ObjectMap<LevelChunk> chunks = new Long2ObjectOpenHashMap<>();

    public LevelChunk getChunk(int x, int z, DimensionType dimension) {
        return this.chunks.computeIfAbsent(
            Util.hash(x, z),
            l -> new LevelChunk(x, z, dimension, this.proxy)
        );
    }

    public LevelChunk getChunk(int x, int z) {
        return this.getChunk(x, z, this.player.getDimension());
    }

    public void updateSubChunk(int chunkX, int chunkZ, DimensionType dimension, int index,
                               SubChunkStorage<Block>[] storages) {
        final LevelChunk chunk = this.getChunk(chunkX, chunkZ, dimension);
        chunk.setSubChunk(
            new LevelSubChunk(
                index,
                storages,
                this.proxy
            )
        );
        this.player.updateCustomBlockDebugMarkers(chunk);
    }

    public void handleUpdateSubChunkBlocks(UpdateSubChunkBlocksPacket packet) {
        final LevelChunk chunk = this.getChunk(
            packet.getSubChunkBlockPosition().getX() >> 4,
            packet.getSubChunkBlockPosition().getZ() >> 4
        );
        if (chunk == null) {
            this.proxy.getLogger().debug(
                "Received an UpdateSubChunkBlocks for an unrendered chunk: " + packet
            );
            return;
        }
        for (final BlockChangeEntry standardBlock : packet.getStandardBlocks()) {
            final Vector3i blockPos = standardBlock.getPos();
            final int runtimeId = standardBlock.getDefinition().getRuntimeId();
            chunk.setBlock(
                blockPos.getX(),
                blockPos.getY(),
                blockPos.getZ(),
                0,
                runtimeId
            );

            if (Block.AIR.getBlockRuntimeId() == runtimeId) {
                final String pos = blockPos.getX() + "," + blockPos.getY() + "," + blockPos.getZ();
                this.proxy.getDebugShapeRenderer().clearShapes(
                    shapeId -> shapeId.startsWith("debug_marker_") && shapeId.contains(pos)
                );
            }
        }
        for (final BlockChangeEntry extraBlock : packet.getExtraBlocks()) {
            chunk.setBlock(
                extraBlock.getPos().getX(),
                extraBlock.getPos().getY(),
                extraBlock.getPos().getZ(),
                1,
                extraBlock.getDefinition().getRuntimeId()
            );
        }
        this.player.updateCustomBlockDebugMarkers(chunk);
    }

    public void handleUpdateBlock(BlockDefinition definition, Vector3i blockPos, int layer) {
        final int x = blockPos.getX() >> 4;
        final int z = blockPos.getZ() >> 4;
        final LevelChunk chunk = this.getChunk(x, z);
        chunk.setBlock(
            blockPos.getX(),
            blockPos.getY(),
            blockPos.getZ(),
            layer,
            definition.getRuntimeId()
        );
        if (Block.AIR.getBlockRuntimeId() == definition.getRuntimeId()) {
            final String pos = blockPos.getX() + "," + blockPos.getY() + "," + blockPos.getZ();
            this.proxy.getDebugShapeRenderer().clearShapes(
                shapeId -> shapeId.startsWith("debug_marker_") && shapeId.contains(pos)
            );
        }
        this.player.updateCustomBlockDebugMarkers(chunk);
    }

    public void handleBlockActorData(BlockActorDataPacket packet) {
        final Vector3i blockPos = packet.getBlockPosition();
        this.getChunk(blockPos.getX() >> 4, blockPos.getZ() >> 4)
            .setBlockActorData(packet.getBlockPosition(), packet.getActorDataTags());
    }

    public LevelChunk getChunk() {
        return this.getChunk(this.player.getChunkX(), this.player.getChunkZ());
    }

    public void clearChunks() {
        this.chunks.clear();
    }

    public void evictChunksOutsideRadius(int centerChunkX, int centerChunkZ, int radiusChunks) {
        final Iterator<LevelChunk> iterator = this.chunks.values().iterator();
        while (iterator.hasNext()) {
            final LevelChunk chunk = iterator.next();
            final int distance = Math.max(
                Math.abs(chunk.getX() - centerChunkX),
                Math.abs(chunk.getZ() - centerChunkZ)
            );
            if (distance > radiusChunks) {
                iterator.remove();
            }
        }
    }

    public void readSubChunk(ByteBuf serializedChunkData, int subChunkIndex, int chunkX, int chunkZ,
                             DimensionType dimension) {
        final int version = serializedChunkData.readUnsignedByte();
        final int blockPaletteLength = serializedChunkData.readUnsignedByte();
        if (version >= 9) {
            serializedChunkData.readUnsignedByte();
        }
        final SubChunkStorage<Block>[] storages =
            new SubChunkStorage[blockPaletteLength];
        for (int layer = 0; layer < blockPaletteLength; layer++) {
            storages[layer] = new SubChunkStorage<>(Block.AIR);
        }
        for (final SubChunkStorage<Block> storage : storages) {
            storage.deserializeNetwork(
                serializedChunkData,
                blockRuntimeId -> this.proxy.getBlockPaletteManager().getBlock(blockRuntimeId)
            );
        }
        this.proxy.getPlayer().getPlayerChunkManager().updateSubChunk(
            chunkX,
            chunkZ,
            dimension,
            subChunkIndex,
            storages
        );
    }

    public void readBorderBlocks(ByteBuf serializedChunkData) {
        if (!serializedChunkData.isReadable()) {
            return;
        }
        final int borderBlockCount = serializedChunkData.readUnsignedByte();
        for (int i = 0; i < borderBlockCount; i++) {
            if (!serializedChunkData.isReadable()) {
                break;
            }
            serializedChunkData.readUnsignedByte();
        }
    }

    public void readBlockActorDataTags(ByteBuf serializedChunkData, LevelChunk levelChunk) {
        while (serializedChunkData.isReadable()) {
            try (final ByteBufInputStream inputStream = new ByteBufInputStream(serializedChunkData);
                 final NBTInputStream nbtInputStream = NbtUtils.createNetworkReader(inputStream)) {
                final NbtMap blockActorDataTags;
                try {
                    blockActorDataTags = (NbtMap) nbtInputStream.readTag();
                } catch (Throwable e) {
                    continue;
                }
                if (blockActorDataTags != null && blockActorDataTags.containsKey("x") &&
                    blockActorDataTags.containsKey("y") &&
                    blockActorDataTags.containsKey("z")) {
                    levelChunk.setBlockActorData(
                        Vector3i.from(
                            blockActorDataTags.getInt("x"),
                            blockActorDataTags.getInt("y"),
                            blockActorDataTags.getInt("z")
                        ),
                        blockActorDataTags
                    );
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}