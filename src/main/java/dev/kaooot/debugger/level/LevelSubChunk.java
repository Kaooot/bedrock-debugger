package dev.kaooot.debugger.level;

import dev.kaooot.debugger.BedrockDebuggerProxy;
import dev.kaooot.debugger.level.block.Block;
import dev.kaooot.debugger.level.storage.SubChunkStorage;
import dev.kaooot.debugger.util.Util;
import java.util.Arrays;
import lombok.Getter;
import lombok.Setter;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
public class LevelSubChunk {

    @Getter
    private final int index;
    private final SubChunkStorage<Block>[] storages = new SubChunkStorage[2];

    @Setter
    private SubChunkStorage<Integer> biomeStorage;

    public LevelSubChunk(int index, BedrockDebuggerProxy proxy) {
        this(index, null, proxy);
    }

    public LevelSubChunk(int index, SubChunkStorage<Block>[] storages, BedrockDebuggerProxy proxy) {
        this.index = index;
        if (storages == null) {
            for (int layer = 0; layer < 2; layer++) {
                this.storages[layer] = new SubChunkStorage<>(Block.AIR);
            }
        } else {
            System.arraycopy(storages, 0, this.storages, 0, storages.length);
        }
        if (this.storages[0] == null) {
            this.storages[0] = new SubChunkStorage<>(Block.AIR);
        }
        if (this.storages[1] == null) {
            this.storages[1] = new SubChunkStorage<>(Block.AIR);
        }
    }

    public void setBlock(int x, int y, int z, int layer, Block block) {
        this.storages[layer].set(Util.indexOf(x, y, z), block);
    }

    public Block getBlock(int x, int y, int z, int layer) {
        return this.storages[layer].get(Util.indexOf(x, y, z));
    }

    public void setBiomeId(int x, int y, int z, int biomeId) {
        this.biomeStorage.set(Util.indexOf(x, y, z), biomeId);
    }

    public int getBiomeId(int x, int y, int z) {
        return this.biomeStorage.get(Util.indexOf(x, y, z));
    }

    public void forEachBlock(int layer, BlockConsumer consumer) {
        final SubChunkStorage<Block> storage = this.storages[layer];
        for (int index = 0; index < 4096; index++) {
            final int paletteIndex = storage.getBitArray().get(index);
            final Block block = storage.getPalette().get(paletteIndex);
            final int blockX = (index >> 8) & 15;
            final int blockZ = (index >> 4) & 15;
            final int blockY = index & 15;
            consumer.accept(blockX, blockY, blockZ, block);
        }
    }

    @FunctionalInterface
    public interface BlockConsumer {
        void accept(int x, int y, int z, Block block);
    }
}