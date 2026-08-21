package dev.kaooot.debugger.level;

import dev.kaooot.debugger.BedrockDebuggerProxy;
import dev.kaooot.debugger.level.block.Block;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.cloudburstmc.math.vector.Vector3i;
import org.cloudburstmc.nbt.NbtMap;
import org.cloudburstmc.protocol.bedrock.data.payload.common.DimensionType;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@RequiredArgsConstructor
public class LevelChunk {

    @Getter
    private final int x;
    @Getter
    private final int z;
    @Getter
    private final DimensionType dimension;
    private final BedrockDebuggerProxy proxy;
    private final Int2ObjectMap<LevelSubChunk> subChunks = new Int2ObjectOpenHashMap<>();
    private final Object2ObjectMap<Vector3i, NbtMap> blockActorDataMap =
        new Object2ObjectOpenHashMap<>();

    public void setBlock(int x, int y, int z, int layer, int blockRuntimeId) {
        final LevelSubChunk subChunk = this.getSubChunk(y >> 4);
        final Block block = this.proxy.getBlockPaletteManager().getBlock(blockRuntimeId);
        subChunk.setBlock(x, y, z, layer, block);
    }

    public Block getBlock(int x, int y, int z, int layer) {
        final LevelSubChunk subChunk = this.getSubChunk(y >> 4);
        return subChunk.getBlock(x, y, z, layer);
    }

    public Block getBlock(int x, int y, int z) {
        return this.getBlock(x, y, z, 0);
    }

    public Block getBlock(Vector3i position) {
        return this.getBlock(position.getX(), position.getY(), position.getZ());
    }

    public void setBlockActorData(Vector3i blockPos, NbtMap nbtMap) {
        this.blockActorDataMap.put(blockPos, nbtMap);
    }

    public NbtMap getBlockActorData(Vector3i blockPos) {
        if (!this.blockActorDataMap.containsKey(blockPos)) {
            return null;
        }
        return this.blockActorDataMap.get(blockPos);
    }

    public LevelSubChunk getSubChunk(int index) {
        this.createSubChunkIfNotExists(index);
        return this.subChunks.get(index);
    }

    public void setSubChunk(LevelSubChunk subChunk) {
        this.subChunks.put(subChunk.getIndex(), subChunk);
    }

    public ObjectCollection<LevelSubChunk> getSubChunks() {
        return this.subChunks.values();
    }

    private void createSubChunkIfNotExists(int index) {
        if (!this.subChunks.containsKey(index)) {
            this.subChunks.put(index, new LevelSubChunk(index, this.proxy));
        }
    }
}